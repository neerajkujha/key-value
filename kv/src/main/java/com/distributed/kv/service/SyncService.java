
/*
 * Created on 3 Mar, 2019 by neejha
 */

package com.distributed.kv.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.distributed.kv.sync.Synchronizer;

/**
 * The Class SyncService.
 */
@Service
public class SyncService {

  /** The synchronizer. */
  @Autowired
  private Synchronizer synchronizer;

  /**
   * Instantiates a new sync service.
   */
  public SyncService() {

  }

  /**
   * Sync.
   *
   * @param key the key
   * @param value the value
   */
  public void sync(String key, String value) {
    synchronizer.sync(key, value);
  }

  /**
   * Bulk sync.
   *
   * @return the byte[]
   */
  public byte[] bulkSync() {
    return synchronizer.bulkSync();
  }

}
