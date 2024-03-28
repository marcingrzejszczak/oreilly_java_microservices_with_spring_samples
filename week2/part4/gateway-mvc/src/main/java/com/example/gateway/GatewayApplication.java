package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

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
	public RouterFunction<ServerResponse> httpBin() {
		return GatewayRouterFunctions.route("httpbin_route")
				.route(
						GatewayRequestPredicates.path("/httpbin/**")
								.and(GatewayRequestPredicates.cookie("spring", "cloud")), http("http://localhost:12345"))
				.filter(FilterFunctions.stripPrefix(1)
						.andThen(CircuitBreakerFilterFunctions.circuitBreaker("httpBinCircuitBreaker"))
						.andThen(FilterFunctions.addResponseHeader("X-Spring-Cloud", "Workshops Response"))
						.andThen(FilterFunctions.setStatus(201)))
				.build();
	}
	// @formatter:on

}
