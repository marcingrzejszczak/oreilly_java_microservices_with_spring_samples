package com.example.frauddetection;

import java.util.Arrays;
import java.util.List;

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
	@GetMapping("/frauds")
	List<String> frauds() {
		System.out.println("\n\nGot fraud request\n\n");
		return Arrays.asList("josh", "marcin");
	}
}