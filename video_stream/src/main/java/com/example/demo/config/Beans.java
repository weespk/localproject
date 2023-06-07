package com.example.demo.config;

import org.springframework.context.annotation.PropertySource;

//@Configuration
//@EnableWebMvc
@PropertySource("classpath:config.properties")
public class Beans {
	public static String movieDir= "D:\\dn";
}
