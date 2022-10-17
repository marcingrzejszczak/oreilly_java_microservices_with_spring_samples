package com.example.loanissuance;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
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
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

}

@RestController
class LoanIssuanceController {

	private static final Logger log = LoggerFactory.getLogger(LoanIssuanceController.class);

	private final FraudDetectionCallingService fraudDetectionCallingService;

	private final CircuitBreakerFactory factory;

	LoanIssuanceController(FraudDetectionCallingService fraudDetectionCallingService, CircuitBreakerFactory factory) {
		this.fraudDetectionCallingService = fraudDetectionCallingService;
		this.factory = factory;
	}

	@GetMapping("/resttemplate")
	@SuppressWarnings("unchecked")
	List<String> restTemplateFrauds() {
		log.info("\n\nGot rest template request\n\n");
		return this.factory.create("rest-template").run(this.fraudDetectionCallingService::restTemplateFrauds, throwable -> Collections.emptyList());
	}
}

@Service
class FraudDetectionCallingService {

	private final RestTemplate restTemplate;

	FraudDetectionCallingService(@LoadBalanced RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@SuppressWarnings("unchecked")
	List<String> restTemplateFrauds() {
		return this.restTemplate.getForObject("http://frauddetection/frauds", List.class);
	}
}
