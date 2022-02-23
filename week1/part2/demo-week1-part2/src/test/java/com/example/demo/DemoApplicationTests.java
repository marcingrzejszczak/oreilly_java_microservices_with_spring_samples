package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}

@Configuration(proxyBeanMethods = false)
class Config {

	@Bean
	@Primary
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	@LoadBalanced
	RestTemplate loadBalancedRestTemplate() {
		return new RestTemplate();
	}
}

class MyClass {

	private final RestTemplate loadBalanced;

	private final RestTemplate nonLoadBalanced;

	MyClass(@LoadBalanced RestTemplate restTemplate,
			RestTemplate nonLoadBalanced) {
		this.loadBalanced = restTemplate;
		this.nonLoadBalanced = nonLoadBalanced;
	}

	void foo() {
		this.loadBalanced.delete(null);
		this.nonLoadBalanced.delete(null);
	}
}


