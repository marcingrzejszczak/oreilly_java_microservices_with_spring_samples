package com.example.loanissuance;

import java.util.List;

import io.micrometer.tracing.BaggageInScope;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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
	RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

}

@RestController
class LoanIssuanceController {

	private static final Logger log = LoggerFactory.getLogger(LoanIssuanceController.class);

	private final RestTemplate restTemplate;

	LoanIssuanceController(@LoadBalanced RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@GetMapping("/resttemplate")
	@SuppressWarnings("unchecked")
	List<String> restTemplateFrauds() {
		log.info("\n\nGot rest template request\n\n");
		return this.restTemplate.getForObject("http://frauddetection/frauds", List.class);
	}
}

@RestController
class LoanIssuanceBaggageController {

	private static final Logger log = LoggerFactory
			.getLogger(LoanIssuanceBaggageController.class);

	private final RestTemplate restTemplate;

	private final Tracer tracer;

	LoanIssuanceBaggageController(
			@LoadBalanced RestTemplate restTemplate,
			Tracer tracer) {
		this.restTemplate = restTemplate;
		this.tracer = tracer;
	}

	@GetMapping("/baggage")
	String baggage() {
		log.info("\n\nGot baggage request\n\n");
		Span span = this.tracer.nextSpan()
				.name("my-custom-span")
				.tag("loanid", "1")
				.event("got request");
		try (Tracer.SpanInScope ws = this.tracer.withSpan(span.start())) {
			try (BaggageInScope b =
						 this.tracer.createBaggage("mybaggage", "mybaggagevalue").makeCurrent()) {
				return this.restTemplate
						.getForObject("http://frauddetection/baggage", String.class);
			}
		} finally {
			span.end();
		}
	}
}
