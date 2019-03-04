

/*
 * Created on 3 Mar, 2019 by neejha
 */

package com.distributed.kv.sync;

/**
 * The Interface Synchronizer.
 */
public interface Synchronizer {

  /**
   * Sync.
   *
   * @param key the key
   * @param value the value
   */
  void sync(String key, String value);

  /**
   * Bulk sync.
   *
   * @return the byte[]
   */
  byte[] bulkSync();

  /**
   * Synchronize to cluster.
   */
  void synchronizeToCluster();

}
