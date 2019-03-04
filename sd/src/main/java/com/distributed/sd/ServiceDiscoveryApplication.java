
/*
 * Created on 3 Mar, 2019 by neejha
 */
package com.distributed.sd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * The Class ServiceDiscoveryApplication.
 */
@SpringBootApplication
@EnableEurekaServer
public class ServiceDiscoveryApplication {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(ServiceDiscoveryApplication.class, args);
	}

}
