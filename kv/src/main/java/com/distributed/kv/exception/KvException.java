
/*
 * Created on 3 Mar, 2019 by neejha
 */
package com.distributed.kv.exception;

/**
 * The Class KvException.java.
 *
 * @author neejha
 */
public class KvException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -5755604107953812340L;

  /**
   * Instantiates a new KvException.java.
   *
   */
  public KvException() {
    super();
  }

  /**
   * Instantiates a new KvException.java.
   *
   * @param message
   */
  public KvException(String message) {
    super(message);
  }

  /**
   * Instantiates a new KvException.java.
   *
   * @param cause
   */
  public KvException(Throwable cause) {
    super(cause);
  }

  /**
   * Instantiates a new KvException.java.
   *
   * @param message
   * @param cause
   */
  public KvException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new KvException.java.
   *
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public KvException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
