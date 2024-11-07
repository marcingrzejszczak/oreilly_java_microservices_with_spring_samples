package com.example.frauddetection;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
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

	private final int port;

	FraudDetectionController(@Value("${server.port}") int port) {
		this.port = port;
	}

	@GetMapping("/frauds")
	List<String> frauds() {
		System.out.println("\n\nGot fraud request, my port [" + port + "] \n\n");
		return Arrays.asList("josh", "marcin");
	}
}
