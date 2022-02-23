package com.example.frauddetection;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableScheduling
public class FraudDetectionApplication {

	public static void main(String[] args) {
		SpringApplication.run(FraudDetectionApplication.class, args);
	}

}

@RestController
class FraudDetectionController {

	private final AtomicInteger gauge;
	private final Counter counter;

	FraudDetectionController(MeterRegistry meterRegistry) {
		this.gauge = meterRegistry.gauge("frauds_current", new AtomicInteger());
		this.counter = meterRegistry.counter("frauds_counter");
	}

	@GetMapping("/frauds")
	List<String> frauds() {
		System.out.println("\n\nGot fraud request\n\n");
		this.counter.increment();
		return Arrays.asList("josh", "marcin");
	}

	@GetMapping("/frauds/gauge")
	int countFraudsWithGauge() {
		return this.gauge.get();
	}

	@GetMapping("/frauds/counter")
	double countFraudsWithCounter() {
		return this.counter.count();
	}

	@Scheduled(fixedRate = 1L)
	void changeGaugeValue() {
		this.gauge.set(new Random().nextInt(100) + 200);
	}

}