/*
 * Created on 3 Mar, 2019 by neejha
 */

package com.distributed.kv.sync.imp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.zeroturnaround.zip.ZipUtil;

import com.distributed.kv.sync.Synchronizer;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;

/**
 * The Class HttpSynchronizer.
 */
@Component
public class HttpSynchronizer implements Synchronizer {

  /** The client. */
  @Autowired
  private RestTemplate client;

  /** The sd client. */
  @Autowired
  private EurekaClient sdClient;

  /** The application name. */
  @Value("${spring.application.name}")
  private String applicationName;

  /** The port. */
  @Value("${server.port}")
  private String port;

  /** The data directory. */
  @Value("${data.directory}")
  private String dataDirectory;

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(HttpSynchronizer.class);

  /*
   * (non-Javadoc)
   * @see com.distributed.kv.sync.Synchronizer#sync()
   */
  @Override
  public void sync(String key, String value) {
    Application application = sdClient.getApplication(applicationName);
    if (application != null) {
      List<InstanceInfo> instances = application.getInstances();
      String currentUrl = getCurrentAddress();
      for (InstanceInfo instanceInfo : instances) {
        String url = instanceInfo.getHomePageUrl();
        if (url != null && !url.isEmpty() && !url.contains(currentUrl)) {
          logger.debug("Peer Application found " + url);
          try {
            client.postForEntity(url + "sync/" + key, value, String.class);
          } catch (HttpClientErrorException e) {
            logger.error("Error while posting data to peer " + e.getMessage());
            e.printStackTrace();
          }
        }
      }
    } else {
      logger.error("No peer Application found");
    }
  }

  /*
   * (non-Javadoc)
   * @see com.distributed.kv.sync.Synchronizer#bulkSunc()
   */
  @Override
  public byte[] bulkSync() {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    logger.debug("Compressing data");
    ZipUtil.pack(new File(dataDirectory), output);
    return output.toByteArray();
  }

  /*
   * (non-Javadoc)
   * @see com.distributed.kv.sync.Synchronizer#synchronizeToCluster()
   */
  @Override
  public void synchronizeToCluster() {
    try {
      logger.debug("Getting oldest instance");
      InstanceInfo syncInstance = getOldestInstance();
      if (syncInstance != null) {
        ResponseEntity<byte[]> response = client.getForEntity(syncInstance.getHomePageUrl() + "bulkSync", byte[].class);
        if (response.getStatusCode().equals(HttpStatus.OK))
          processResponse(response.getBody());
      }
    } catch (Exception e) {
      logger.error("Error occured while sync " + e.getMessage());
      e.printStackTrace();
    }

  }

  /**
   * Process response.
   *
   * @param response the response
   */
  private void processResponse(byte[] response) {
    logger.debug("Unpacking response");
    InputStream is = new ByteArrayInputStream(response);
    ZipUtil.unpack(is, new File(dataDirectory));
  }

  /**
   * Gets the oldest instance.
   *
   * @return the oldest instance
   */
  private InstanceInfo getOldestInstance() {
    Application application = sdClient.getApplication(applicationName);
    InstanceInfo syncInstance = null;
    if (application != null) {
      List<InstanceInfo> instances = application.getInstances();
      String currentUrl = getCurrentAddress();
      long uptime = 0L;
      for (InstanceInfo instanceInfo : instances) {
        String url = instanceInfo.getHomePageUrl();
        if (url != null && !url.isEmpty() && !url.contains(currentUrl)) {
          if (uptime < instanceInfo.getLeaseInfo().getServiceUpTimestamp()) {
            uptime = instanceInfo.getLeaseInfo().getServiceUpTimestamp();
            syncInstance = instanceInfo;
          }

        }
      }
    } else {
      logger.error("No peer application found");
    }
    return syncInstance;
  }

  /**
   * Gets the current address.
   *
   * @return the current address
   */
  private String getCurrentAddress() {
    String currentUrl = null;
    InetAddress ipAddr;
    try {
      ipAddr = InetAddress.getLocalHost();
      currentUrl = ipAddr.getHostAddress() + ":" + port;
    } catch (UnknownHostException exception) {
      exception.printStackTrace();
    }
    return currentUrl;
  }

}
