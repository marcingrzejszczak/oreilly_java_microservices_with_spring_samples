package com.example.frauddetection;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class StaticFraudService implements FraudCheckingService {

	@Override
	public List<String> frauds() {
		return Arrays.asList("josh", "marcin");
	}
}
