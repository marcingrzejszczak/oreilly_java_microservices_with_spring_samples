package com.example.frauddetection;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class EmptyFraudService implements FraudCheckingService {

	@Override
	public List<String> frauds() {
		return Collections.emptyList();
	}
}
