
/*
 * Created on 3 Mar, 2019 by neejha
 */
package com.distributed.kv.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.distributed.kv.exception.KvException;
import com.distributed.kv.service.KeyValueService;
import com.distributed.kv.service.SyncService;

/**
 * The Class AppController.
 */
@RestController
public class AppController {

  /** The key value service. */
  @Autowired
  private KeyValueService keyValueService;

  /** The sync service. */
  @Autowired
  private SyncService syncService;

  private static final Logger logger = LoggerFactory.getLogger(AppController.class);

  /**
   * Gets the value from key.
   *
   * @param key the key
   * @return the value from key
   * @throws KvException the kv exception
   */
  @GetMapping("/get/{key}")
  public String getValueFromKey(@PathVariable String key) throws KvException {
    logger.info("Getting value for key " + key);
    String value = keyValueService.get(key);
    logger.info("Value returned " + value);
    return value;
  }

  /**
   * Sets the key value.
   *
   * @param key the key
   * @param value the value
   * @return the string
   * @throws KvException the kv exception
   */
  @PostMapping("/set/{key}")
  public String setKeyValue(@PathVariable String key, @RequestBody String value) throws KvException {
    logger.info("Setting value for key " + key + " as " + value);
    keyValueService.set(key, value);
    logger.info("Value set");
    logger.info("Syncing other nodes");
    syncService.sync(key, value);
    logger.info("Sync complete");
    return "OK";
  }

  /**
   * Sync the instances.
   *
   * @param key the key
   * @param value the value
   * @return the string
   * @throws KvException the kv exception
   */
  @PostMapping("/sync/{key}")
  public String sync(@PathVariable String key, @RequestBody String value) throws KvException {
    logger.info("Syncing from other nodes");
    keyValueService.set(key, value);
    logger.info("Syncing complete");
    return "OK";
  }

  /**
   * Gets the value from key.
   *
   * @param key the key
   * @return the value from key
   * @throws KvException the kv exception
   */
  @GetMapping("/bulkSync")
  public byte[] bulkSync() throws KvException {
    logger.info("Sending full data to other node");
    byte[] zip = syncService.bulkSync();
    logger.info("Data sent");
    return zip;
  }

}
