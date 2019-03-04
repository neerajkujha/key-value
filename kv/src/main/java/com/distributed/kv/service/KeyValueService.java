
/*
 * Created on 3 Mar, 2019 by neejha
 */
package com.distributed.kv.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.distributed.kv.exception.KvException;
import com.distributed.kv.storage.Storage;

/**
 * The Class KeyValueService.
 */
@Service
public class KeyValueService {

  /** The storage. */
  @Autowired
  private Storage storage;

  /**
   * Gets the.
   *
   * @param key the key
   * @return the string
   * @throws KvException the kv exception
   */
  public String get(String key) throws KvException {
    return storage.retrieve(key);
  }

  /**
   * Sets the.
   *
   * @param key the key
   * @param value the value
   * @throws KvException the kv exception
   */
  public void set(String key, String value) throws KvException {
    storage.store(key, value);
  }

}
