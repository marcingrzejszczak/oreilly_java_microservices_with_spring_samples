package com.example.frauddetection;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootTest
class FraudDetectionApplicationTests {

	@Autowired MeterRegistry meterRegistry;

	@Test
	void contextLoads() {
		new TimerDemo(meterRegistry).timer();
		new CounterDemo(meterRegistry).counter();
		new GaugeDemo(meterRegistry).gauge();

		System.out.println(meterRegistry.getMeters().stream().filter(meter -> meter.getId().getName().contains("example")).map(meter -> "Meter <" + meter.getId().getName() + "> with type <" + meter.getId().getType() + "> has measurements \n\t" + StreamSupport.stream(meter.measure().spliterator(), false).map(Measurement::toString).collect(Collectors.joining("\n\t"))).collect(Collectors.joining("\n")));
	}

}

class TimerDemo {

	private final MeterRegistry meterRegistry;

	TimerDemo(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	void timer() {
		Timer.Sample sample = Timer.start(meterRegistry);
		// sth to measure
		sample.stop(Timer.builder("timer.example")
				.tag("region", "west")
				.register(meterRegistry));
	}
}

class CounterDemo {

	private final Counter counter;

	CounterDemo(MeterRegistry meterRegistry) {
		this.counter = meterRegistry
				.counter("counter.example");
	}

	void counter() {
		counter.increment();
	}
}

class GaugeDemo {

	private final AtomicInteger gauge;

	GaugeDemo(MeterRegistry meterRegistry) {
		this.gauge = meterRegistry
				.gauge("gauge.example", new AtomicInteger());
	}

	@Scheduled(fixedRate = 1L)
	void gauge() {
		this.gauge.set(new Random().nextInt());
	}

	@EnableScheduling
	static class Config {

	}
}

