package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/*
SCENARIOS:

(PREDICATE)
When a cookie called [spring] with value [cloud] is set
Under gateway/httpbin/ endpoint
	Call https://httpbin.org/headers
		(Filter)
		StripPrefix 1
		Add a response header ([X-Spring-Cloud] with value [Workshops Response])
		Circuit breaker
		Set status 201

 curl -v localhost:6543/httpbin/headers --cookie "spring=cloud"

 */
@SpringBootApplication
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	// @formatter:off
	@Bean
	public RouteLocator routes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("httpbin_route", r ->
						r.cookie("spring", "cloud")
							.and()
							.path("/httpbin/**")
						.filters(f ->
							f.stripPrefix(1)
									.circuitBreaker(c -> c.setName("httpBinCircuitBreaker"))
									.addResponseHeader("X-Spring-Cloud", "Workshops Response")
									.setStatus(201))
						.uri("https://httpbin.org")).build();
	}
	// @formatter:on

}
