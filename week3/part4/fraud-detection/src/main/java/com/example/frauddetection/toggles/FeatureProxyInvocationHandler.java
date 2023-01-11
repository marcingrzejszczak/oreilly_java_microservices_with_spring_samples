package com.example.frauddetection.toggles;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.core.env.Environment;

/**
 * Based on <a href="https://github.com/togglz/togglz/blob/3.3.3/core/src/main/java/org/togglz/core/proxy/FeatureProxyInvocationHandler.java">Togglz</a>
 *
 * @author Christian Kaltepoth
 */
public class FeatureProxyInvocationHandler implements InvocationHandler {

	// Basing on https://www.togglz.org/documentation/spring-boot-starter.html
	private static final String PROPERTY_PATTERN = "toggles.features.%s.enabled";

	private final Enum feature;

	private final Object active;

	private final Object inactive;

	private final Environment environment;

	public FeatureProxyInvocationHandler(Enum feature, Object active, Object inactive, Environment environment) {
		this.feature = feature;
		this.active = active;
		this.inactive = inactive;
		this.environment = environment;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object target = isActive() ? active : inactive;
		try {
			return method.invoke(target, args);
		}
		catch (InvocationTargetException ex) {
			throw ex.getCause();
		}

	}

	private boolean isActive() {
		return this.environment.getProperty(String.format(PROPERTY_PATTERN, feature.name()), Boolean.class, false);
	}

}
