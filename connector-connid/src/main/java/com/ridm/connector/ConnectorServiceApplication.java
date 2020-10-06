package com.ridm.connector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ridm.connid.connector.ConnectorService;


@SpringBootApplication(scanBasePackages={
"com.ridm.connector", "com.ridm.connid.connector"})
public class ConnectorServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConnectorService.class, args);
	}

	

}
