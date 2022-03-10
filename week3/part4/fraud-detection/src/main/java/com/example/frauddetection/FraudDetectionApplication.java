package com.example.frauddetection;

import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.togglz.core.context.StaticFeatureManagerProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.jdbc.JDBCStateRepository;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;
import org.togglz.spring.proxy.FeatureProxyFactoryBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
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
		bean.setFeature(MyFeatures.STATIC_FRAUD_CHECK_LIST.name());
		bean.setProxyType(FraudCheckingService.class);
		return bean;
	}

	@Bean
	StateRepository myStateRepository(DataSource dataSource) {
		return JDBCStateRepository.newBuilder(dataSource)
				.createTable(true)
				.tableName("features")
				.build();
	}

	// tag::featureconfig[]
	@Configuration(proxyBeanMethods = false)
	static class FeatureManagerConfiguration {

		@Autowired
		FeatureManager featureManager;

		@PostConstruct
		void setupFeatureManager() {
			StaticFeatureManagerProvider.setFeatureManager(featureManager);
		}

	}
	// end::featureconfig[]
}


@Component
class NewUserWithEachRequestUserProvider implements UserProvider {

	private final Random random = new Random();

	@Override
	public FeatureUser getCurrentUser() {
		return new SimpleFeatureUser(String.valueOf(this.random.nextInt()));
	}
}
