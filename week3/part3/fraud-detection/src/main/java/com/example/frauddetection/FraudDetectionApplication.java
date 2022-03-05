package com.example.frauddetection;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
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

	private static final Logger log =
			LoggerFactory.getLogger(FraudDetectionController.class);

	private final MyEntityRepository myEntityRepository;

	private final CircuitBreakerFactory factory;

	FraudDetectionController(MyEntityRepository myEntityRepository, CircuitBreakerFactory factory) {
		this.myEntityRepository = myEntityRepository;
		this.factory = factory;
	}

	@GetMapping("/frauds")
	List<String> frauds() {
		log.info("\n\nGot fraud request\n\n");
		return this.factory.create("frauds")
				.run(() -> this.myEntityRepository.findAllNames().collect(Collectors.toList()), throwable -> Collections.emptyList());
	}
}

@Entity
class Frauds {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(nullable = false)
	private String name;

	public Frauds() {

	}

	public Frauds(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Frauds{" +
				"id=" + id +
				", name='" + name + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Frauds frauds = (Frauds) o;
		return Objects.equals(id, frauds.id) && Objects.equals(name, frauds.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name);
	}
}

@Repository
class MyEntityRepository {

	private final JdbcTemplate jdbcTemplate;

	MyEntityRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	Stream<String> findAllNames() {
		return this.jdbcTemplate.queryForStream("SELECT NAME FROM FRAUDS", (rs, rowNum) -> rs.getString("NAME"));
	}
}
