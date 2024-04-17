package com.example.loanissuance;

import java.io.IOException;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

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

	@Bean
	@LoadBalanced
	WebClient.Builder webClient() {
		return WebClient.builder();
	}

	@Bean
	HttpServiceProxyFactory proxyFactory(WebClient.Builder webClientBuilder) {
		return HttpServiceProxyFactory.builder()
				.exchangeAdapter(WebClientAdapter.create(webClientBuilder
								.baseUrl("http://frauddetection")
						.build()))
				.build();
	}

	@Bean
	DeclarativeFrauds declarativeFrauds(HttpServiceProxyFactory httpServiceProxyFactory) {
		return httpServiceProxyFactory.createClient(DeclarativeFrauds.class);
	}

}

interface DeclarativeFrauds {
	@GetExchange("/frauds")
	List<String> frauds();
}

@Configuration(proxyBeanMethods = false)
@EnableFeignClients
class OpenFeignConfig {

}

@FeignClient("frauddetection")
interface FeignFrauds {
	@GetMapping("/frauds")
	List<String> frauds();
}


@RestController
class LoanIssuanceController {

	private final RestTemplate restTemplate;

	private final FeignFrauds feignFrauds;

	private final DeclarativeFrauds declarativeFrauds;

	LoanIssuanceController(@LoadBalanced RestTemplate restTemplate,
			FeignFrauds feignFrauds, DeclarativeFrauds declarativeFrauds) {
		this.restTemplate = restTemplate;
		this.feignFrauds = feignFrauds;
		this.declarativeFrauds = declarativeFrauds;
	}

	@GetMapping("/resttemplate")
	@SuppressWarnings("unchecked")
	List<String> restTemplateFrauds() {
		System.out.println("\n\nGot rest template request\n\n");
		return this.restTemplate.getForObject("http://frauddetection/frauds", List.class);
	}

	@GetMapping("/feign")
	List<String> feignFrauds() {
		System.out.println("\n\nGot feign request\n\n");
		return this.feignFrauds.frauds();
	}

	@GetMapping("/declarative")
	List<String> declarativeFrauds() throws IOException {
		System.out.println("\n\nGot declarative client request\n\n");
		return this.declarativeFrauds.frauds();
	}
}
