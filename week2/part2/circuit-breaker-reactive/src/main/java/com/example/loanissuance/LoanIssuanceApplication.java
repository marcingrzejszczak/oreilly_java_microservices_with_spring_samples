package com.example.loanissuance;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import reactor.core.publisher.Mono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
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
