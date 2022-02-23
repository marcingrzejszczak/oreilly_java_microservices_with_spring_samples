package com.example.loanissuance;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.function.context.PollableBean;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class LoanIssuanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoanIssuanceApplication.class, args);
	}

}

@Configuration(proxyBeanMethods = false)
class Config {

	@Bean
	@LoadBalanced
	WebClient.Builder webClient() {
		return WebClient.builder();
	}

}


@Configuration(proxyBeanMethods = false)
class CircuitConfig {
	@Bean
	public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
		return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
				.circuitBreakerConfig(CircuitBreakerConfig.custom()
						.minimumNumberOfCalls(5).build())
				.build());
	}
}

@RestController
class LoanIssuanceController {

	private final WebClient webClient;

	private final ReactiveCircuitBreakerFactory factory;

	LoanIssuanceController(@LoadBalanced WebClient.Builder builder,
			ReactiveCircuitBreakerFactory factory) {
		this.webClient = builder.build();
		this.factory = factory;
	}

	@GetMapping("/webclient")
	@SuppressWarnings("unchecked")
	Mono<List> webclientFrauds() {
		return Mono.fromRunnable(() -> System.out.println("\n\nGot webclient request\n\n"))
				.then(factory.create("webclient")
						.run(this.webClient.get().uri("http://frauddetection/frauds").retrieve().bodyToMono(List.class)));
	}

	@GetMapping("/webclientwithwrongurl")
	@SuppressWarnings("unchecked")
	Mono<List> webclientFraudsWrongUrl() {
		return Mono.fromRunnable(() -> System.out.println("\n\nGot webclient request\n\n"))
				.then(factory.create("webclient").run(
						Mono.delay(Duration.ofMillis(500)).then(this.webClient.get().uri("http://frauddetection/wrong").retrieve().bodyToMono(List.class))
				));
	}

	@GetMapping("/webclientwithfallback")
	@SuppressWarnings("unchecked")
	Mono<List> webclientFraudsFallback() {
		return Mono.fromRunnable(() -> System.out.println("\n\nGot webclient request\n\n"))
				.then(factory.create("webclient")
						.run(Mono.delay(Duration.ofMillis(500)).then(this.webClient.get().uri("http://frauddetection/wrong").retrieve().bodyToMono(List.class)),
								throwable -> Mono.just(Arrays.asList("foo", "bar"))));
	}

}


@Configuration(proxyBeanMethods = false)
class LoanIssuanceStreamConfig {

	private static final Logger log = LoggerFactory.getLogger(LoanIssuanceStreamConfig.class);

	@PollableBean
	Supplier<Flux<String>> fraudsSupplier() {
		return () -> Flux.just("hello from reactive supplier")
				.doOnNext(s -> log.info("Sending a message from reactive supplier [{}]", s));
	}

	@Bean
	Function<Flux<String>, Flux<String>> fraudsFunction() {
		return input -> input.map(s -> s + " with appended text [hello from reactive function]")
				.doOnNext(s -> log.info("Sending a message from reactive function [{}]", s));
	}

}

@RestController
class LoanIssuanceStreamController {

	private static final Logger log = LoggerFactory.getLogger(LoanIssuanceStreamController.class);

	private final StreamBridge bridge;

	LoanIssuanceStreamController(StreamBridge bridge) {
		this.bridge = bridge;
	}

	@PostMapping("/stream")
	Mono<Void> endpointPresent(@RequestBody String body) {
		return Mono.just(body)
				.doOnNext(s -> log.info("Sending a message from controller [{}]", body))
				.doOnNext(s -> this.bridge.send("fraudsFromStreamBridge-out-0", s))
				.then();
	}
}
