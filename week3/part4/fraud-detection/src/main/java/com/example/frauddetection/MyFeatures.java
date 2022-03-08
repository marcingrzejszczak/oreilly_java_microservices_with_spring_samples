package com.example.frauddetection;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum MyFeatures implements Feature {

	@EnabledByDefault
	@Label("Static fraud check list")
	STATIC_FRAUD_CHECK_LIST;

	public boolean isActive() {
		return FeatureContext.getFeatureManager().isActive(this);
	}

}
