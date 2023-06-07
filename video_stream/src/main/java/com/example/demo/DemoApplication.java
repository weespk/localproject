package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.support.ErrorPageFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

//	@Bean
//	public ErrorPageFilter errorPageFilter() {
//		return new ErrorPageFilter();
//	}

//	@Bean
//	public FilterRegistrationBean disableSpringBootErrorFilter(ErrorPageFilter filter) {
//		FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
//		filterRegistrationBean.setFilter(filter);
//		filterRegistrationBean.setEnabled(false);
//		return filterRegistrationBean;
//	}

}
