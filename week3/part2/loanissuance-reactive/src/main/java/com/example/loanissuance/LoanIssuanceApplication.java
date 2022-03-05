package com.example.loanissuance;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.sleuth.BaggageInScope;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanAndScope;
import org.springframework.cloud.sleuth.Tracer;
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

@RestController
class LoanIssuanceController {

	private static final Logger log = LoggerFactory.getLogger(LoanIssuanceController.class);

	private final WebClient webClient;

	LoanIssuanceController(@LoadBalanced WebClient.Builder builder) {
		this.webClient = builder.build();
	}

	@GetMapping("/webclient")
	@SuppressWarnings("unchecked")
	Mono<List> webclientFrauds() {
		return Mono.fromRunnable(() -> log.info("\n\nGot webclient request\n\n"))
				.then(this.webClient.get().uri("http://frauddetection/frauds").retrieve().bodyToMono(List.class));
	}

}

@RestController
class LoanIssuanceBaggageController {

	private static final Logger log = LoggerFactory.getLogger(LoanIssuanceBaggageController.class);

	private final WebClient webClient;

	private final Tracer tracer;

	LoanIssuanceBaggageController(@LoadBalanced WebClient.Builder builder, Tracer tracer) {
		this.webClient = builder.build();
		this.tracer = tracer;
	}

	@GetMapping("/baggage")
	Mono<String> baggage() {
		log.info("\n\nGot baggage request\n\n");
		AtomicReference<BaggageInScope> baggageInScope = new AtomicReference<>();
		AtomicReference<Span> childSpan = new AtomicReference<>();
		AtomicReference<Tracer.SpanInScope> childSpanScope = new AtomicReference<>();
		return Mono.just(this.tracer.nextSpan()
					.name("my-custom-span")
					.tag("loanid", "1")
					.event("got request"))
				.doOnNext(childSpan::set)
				.map(span -> this.tracer.createBaggage("mybaggage", "mybaggagevalue"))
				.doOnNext(baggageInScope::set)
				.then(this.webClient.get().uri("http://frauddetection/baggage").retrieve().bodyToMono(String.class)
						.doFirst(() -> childSpanScope.set(tracer.withSpan(childSpan.get().start())))
						.doFinally(signalType -> childSpanScope.get().close()))
				.doFinally(sig -> baggageInScope.get().close())
				.doFinally(sig -> childSpan.get().end());
	}

}
