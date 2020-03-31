package com.pw.demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class DemoApp {

	public static void main(String[] args) {
		new SpringApplicationBuilder()
				.sources(DemoApp.class)
				.run(args);
	}
}
