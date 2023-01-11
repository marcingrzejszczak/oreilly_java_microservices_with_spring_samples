package com.example.frauddetection;

import java.util.List;

import com.example.frauddetection.toggles.FeatureProxyFactoryBean;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class FraudDetectionApplication {

	public static void main(String[] args) {
		SpringApplication.run(FraudDetectionApplication.class, args);
	}

}

@RestController
class FraudDetectionController {

	private final FraudCheckingService fraudCheckingService;

	FraudDetectionController(FraudCheckingService fraudCheckingService) {
		this.fraudCheckingService = fraudCheckingService;
	}

	@GetMapping("/frauds")
	List<String> frauds() {
		return this.fraudCheckingService.frauds();
	}
}

@Configuration(proxyBeanMethods = false)
class FeatureToggleConfiguration {

	@Bean
	@Primary
	FeatureProxyFactoryBean fraudCheckingProxy(StaticFraudService staticFraudService,
			EmptyFraudService emptyFraudService) {
		FeatureProxyFactoryBean bean = new FeatureProxyFactoryBean();
		bean.setActive(staticFraudService);
		bean.setInactive(emptyFraudService);
		bean.setFeature(MyFeatures.STATIC_FRAUD_CHECK_LIST);
		bean.setProxyType(FraudCheckingService.class);
		return bean;
	}

}
