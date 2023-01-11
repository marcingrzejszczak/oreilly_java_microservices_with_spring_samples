package com.example.frauddetection.toggles;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * Based on <a href="https://github.com/togglz/togglz/blob/3.3.3/spring-core/src/main/java/org/togglz/spring/proxy/FeatureProxyFactoryBean.java">Togglz</a>
 *
 * @author Christian Kaltepoth
 */
public class FeatureProxyFactoryBean implements FactoryBean<Object>, InitializingBean, EnvironmentAware {

	private Enum feature;

	private Object active;

	private Object inactive;

	private Class<?> proxyType;

	private boolean initialized = false;

	private Environment environment;

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(feature, "The 'feature' property is required");
		Assert.notNull(active, "The 'active' property is required");
		Assert.notNull(inactive, "The 'inactive' property is required");
		Assert.notNull(environment, "The 'environment' bean is required");
		if (proxyType != null && !proxyType.isInterface()) {
			throw new IllegalArgumentException(proxyType.getClass().getName() + " is not an interface");
		}
		initialized = true;
	}

	@Override
	public Object getObject() throws Exception {

		// make sure the factory is fully initialized
		if (!initialized || environment == null) {
			throw new FactoryBeanNotInitializedException();
		}

		// create the invocation handler that switches between implementations
		FeatureProxyInvocationHandler proxy = new FeatureProxyInvocationHandler(feature, active, inactive, this.environment);

		// obtain the interface for which to create the proxy
		Class<?> proxyType = getEffectiveProxyType();

		// create the proxy
		return Proxy.newProxyInstance(getSuitableClassLoader(), new Class<?>[] {proxyType}, proxy);

	}

	private Class<?> getEffectiveProxyType() {

		// prefer the business interface manually set by the user
		if (proxyType != null) {
			return proxyType;
		}

		// check which interfaces the both delegates implements
		HashSet<Class<?>> activeInterfaces = new HashSet<Class<?>>(Arrays.asList(active.getClass().getInterfaces()));
		HashSet<Class<?>> inactiveInterfaces = new HashSet<Class<?>>(Arrays.asList(inactive.getClass().getInterfaces()));

		// build the intersection
		activeInterfaces.retainAll(inactiveInterfaces);

		// we need exactly one interface to share
		if (activeInterfaces.size() != 1) {
			throw new IllegalArgumentException("The active and the inactive class must share exactly one interface");
		}

		return activeInterfaces.iterator().next();

	}

	private ClassLoader getSuitableClassLoader() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader == null) {
			classLoader = this.getClass().getClassLoader();
		}
		return classLoader;
	}

	@Override
	public Class<?> getObjectType() {
		if (initialized) {
			return getEffectiveProxyType();
		}
		else {
			return null;
		}
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public Enum getFeature() {
		return feature;
	}

	public void setFeature(Enum feature) {
		this.feature = feature;
	}

	public Object getActive() {
		return active;
	}

	public void setActive(Object active) {
		this.active = active;
	}

	public Object getInactive() {
		return inactive;
	}

	public void setInactive(Object inactive) {
		this.inactive = inactive;
	}

	public Class<?> getProxyType() {
		return proxyType;
	}

	public void setProxyType(Class<?> proxyType) {
		this.proxyType = proxyType;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
}
