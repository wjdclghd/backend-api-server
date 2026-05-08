package com.jch.backendapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BackendApiServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApiServerApplication.class, args);
	}

}
