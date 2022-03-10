package com.example.frauddetection;

import org.togglz.core.Feature;
import org.togglz.core.activation.UsernameActivationStrategy;
import org.togglz.core.annotation.ActivationParameter;
import org.togglz.core.annotation.DefaultActivationStrategy;
import org.togglz.core.annotation.FeatureGroup;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum MyFeatures implements Feature {

	@Label("Static fraud check list")
	STATIC_FRAUD_CHECK_LIST,

	@FeatureGroup("Performance")
	@Label("Feature for performance no 1")
	PERFORMANCE_FEATURE_1,

	@FeatureGroup("Performance")
	@Label("Feature for performance no 2")
	PERFORMANCE_FEATURE_2,

	@DefaultActivationStrategy(
			id = UsernameActivationStrategy.ID,
			parameters = {
					@ActivationParameter(name = UsernameActivationStrategy.PARAM_USERS, value = "allowed user")
			}
	)
	@Label("Some feature with activation strategy")
	YET_ANOTHER_FEATURE;

	public boolean isActive() {
		return FeatureContext.getFeatureManager().isActive(this);
	}

}
