
/*
 * Created on 3 Mar, 2019 by neejha
 */
package com.distributed.kv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.web.client.RestTemplate;

import com.distributed.kv.sync.Synchronizer;

/**
 * The Class KvApplication.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class KvApplication {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(KvApplication.class);

  /** The synchronizer. */
  @Autowired
  private Synchronizer synchronizer;

  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {
    logger.info("Starting Application");
    SpringApplication.run(KvApplication.class, args);
    logger.info("Application started");
  }

  /**
   * Rest template.
   *
   * @param builder the builder
   * @return the rest template
   */
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }

  /**
   * Sync from peer after startup.
   */
  @EventListener(ApplicationStartedEvent.class)
  public void syncFromPeerAfterStartup() {
    logger.info("Configuring Application. Please wait till fully confgured");
    synchronizer.synchronizeToCluster();
    logger.info("Application configured and ready to use.");
  }

}
