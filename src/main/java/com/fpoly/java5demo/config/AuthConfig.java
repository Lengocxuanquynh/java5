package com.fpoly.java5demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fpoly.java5demo.components.AuthInterceptor;

@Configuration
public class AuthConfig implements WebMvcConfigurer {
	@Autowired
	private AuthInterceptor interceptor;

	@org.springframework.context.annotation.Bean
	public org.springframework.web.context.request.RequestContextListener requestContextListener() {
		return new org.springframework.web.context.request.RequestContextListener();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(interceptor)
				.addPathPatterns("/admin", "/admin/**", "/user/**", "/checkout", "/checkout/**", "/order/**", "/cart/**");
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations("file:uploads/");
		registry.addResourceHandler("/static/**")
				.addResourceLocations("classpath:/static/");
	}
}
