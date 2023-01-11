package com.example.frauddetection;

import java.util.Arrays;
import java.util.List;

import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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

	@GetMapping("/frauds")
	List<String> frauds() {
		log.info("\n\nGot fraud request\n\n");
		return Arrays.asList("josh", "marcin");
	}
}

@RestController
class BaggageController {

	private static final Logger log =
			LoggerFactory.getLogger(BaggageController.class);

	private final Tracer tracer;

	BaggageController(Tracer tracer) {
		this.tracer = tracer;
	}

	@GetMapping("/baggage")
	String baggage() {
		String mybaggage = tracer.getBaggage("mybaggage").get();
		tracer.currentSpan().tag("mybaggage-tag", mybaggage);
		log.info("\n\nGot baggage request, baggage equals {}\n\n", mybaggage);
		return mybaggage;
	}
}
