package com.ibm.psd2.api.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class GlobalSecurityConfig extends GlobalMethodSecurityConfiguration
{
	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler()
	{
		OAuth2MethodSecurityExpressionHandler omseh = new OAuth2MethodSecurityExpressionHandler();
		return omseh;
	}
}
