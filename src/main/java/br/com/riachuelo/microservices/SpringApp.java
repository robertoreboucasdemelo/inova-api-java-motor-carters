/*
 * Copyright (c) 2017, Riachuelo and/or its affiliates. All rights reserved.
 * RCHLO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.com.riachuelo.microservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("br.com.riachuelo")
public class SpringApp {

	/**
	 * Start via Spring Boot
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(SpringApp.class, args);
	}

}
