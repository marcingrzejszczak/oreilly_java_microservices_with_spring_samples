package com.example.frauddetection;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

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
	private final Timer timer;

	FraudDetectionController(MeterRegistry meterRegistry) {
		this.gauge = meterRegistry.gauge("frauds_current", new AtomicInteger());
		this.counter = meterRegistry.counter("frauds_counter");
		this.timer = meterRegistry.timer("frauds_time");
	}

	@GetMapping("/frauds")
	List<String> frauds() {
		return this.timer.record(() -> {
			System.out.println("\n\nGot fraud request\n\n");
			this.counter.increment();
			return Arrays.asList("josh", "marcin");
		});
	}

	@Scheduled(fixedRate = 1L)
	void changeGaugeValue() {
		this.gauge.set(new Random().nextInt(100) + 200);
	}

}
