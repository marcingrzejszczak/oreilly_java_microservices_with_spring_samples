package com.example.loanissuance;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.http.GET;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.square.retrofit.EnableRetrofitClients;
import org.springframework.cloud.square.retrofit.core.RetrofitClient;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

@Configuration(proxyBeanMethods = false)
@EnableRetrofitClients
class SquareConfig {

	@Bean
	@LoadBalanced
	public OkHttpClient.Builder okHttpClientBuilder() {
		return new OkHttpClient.Builder();
	}
}

@RetrofitClient("frauddetection")
interface RetrofitFrauds {
	@GET("/frauds")
	Call<List<String>> frauds();
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

@Configuration(proxyBeanMethods = false)
class CircuitConfig {
	@Bean
	public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
		return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
				.circuitBreakerConfig(CircuitBreakerConfig.custom()
						.minimumNumberOfCalls(5).build())
				.build());
	}
}

@RestController
class LoanIssuanceController {

	private final RestTemplate restTemplate;

	private final FeignFrauds feignFrauds;

	private final RetrofitFrauds retrofitFrauds;

	private final CircuitBreakerFactory factory;

	LoanIssuanceController(@LoadBalanced RestTemplate restTemplate,
			FeignFrauds feignFrauds, RetrofitFrauds retrofitFrauds,
			CircuitBreakerFactory factory) {
		this.restTemplate = restTemplate;
		this.feignFrauds = feignFrauds;
		this.retrofitFrauds = retrofitFrauds;
		this.factory = factory;
	}

	@GetMapping("/resttemplate")
	@SuppressWarnings("unchecked")
	List<String> restTemplateFrauds() {
		System.out.println("\n\nGot rest template request\n\n");
		return this.factory.create("rest-template")
				.run(() -> this.restTemplate.getForObject("http://frauddetection/frauds", List.class));
	}

	@GetMapping("/resttemplatewithwrongurl")
	@SuppressWarnings("unchecked")
	List<String> restTemplateFraudsWrongUrl() {
		System.out.println("\n\nGot rest template request\n\n");
		return this.factory.create("rest-template")
				.run(() -> {
					try {
						Thread.sleep(500);
					}
					catch (InterruptedException e) {
					}
					return this.restTemplate.getForObject("http://frauddetection/wrong", List.class);
				});
	}

	@GetMapping("/resttemplatewithfallback")
	@SuppressWarnings("unchecked")
	List<String> restTemplateFraudsWithFallback() {
		System.out.println("\n\nGot rest template request\n\n");
		return this.factory.create("fallback")
				.run(() -> {
							try {
								Thread.sleep(500);
							}
							catch (InterruptedException e) {
							}
							return this.restTemplate.getForObject("http://frauddetection/wrong", List.class);
						},
						throwable -> Arrays.asList("foo", "bar"));
	}

	@GetMapping("/feign")
	List<String> feignFrauds() {
		System.out.println("\n\nGot feign request\n\n");
		return this.feignFrauds.frauds();
	}

	@GetMapping("/retrofit")
	List<String> retrofitFrauds() throws IOException {
		System.out.println("\n\nGot retrofit request\n\n");
		return this.retrofitFrauds.frauds().execute().body();
	}
}

@Configuration(proxyBeanMethods = false)
class LoanIssuanceStreamConfig {

	private static final Logger log = LoggerFactory.getLogger(LoanIssuanceStreamConfig.class);

	@Bean
	Supplier<String> fraudsSupplier() {
		return () -> {
			String body = "hello from supplier";
			log.info("Sending a message from supplier [{}]", body);
			return body;
		};
	}

	@Bean
	Function<String, String> fraudsFunction() {
		return input -> {
			String body = input + "with appended text [hello from function]";
			log.info("Sending a message from function [{}]", body);
			return body;
		};
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
	void endpointPresent(@RequestBody String body) {
		log.info("Sending a message from controller [{}]", body);
		this.bridge.send("fraudsFromStreamBridge-out-0", body);
	}
}
