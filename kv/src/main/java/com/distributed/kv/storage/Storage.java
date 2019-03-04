
/*
 * Created on 3 Mar, 2019 by neejha
 */

package com.distributed.kv.storage;

import com.distributed.kv.exception.KvException;

/**
 * The Interface Storage.
 */
public interface Storage {

  /**
   * Store.
   *
   * @param key the key
   * @param value the value
   * @throws KvException the kv exception
   */
  void store(String key, String value) throws KvException;

  /**
   * Retrieve.
   *
   * @param key the key
   * @return the string
   * @throws KvException the kv exception
   */
  String retrieve(String key) throws KvException;

}
