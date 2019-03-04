/*
 * Created on 3 Mar, 2019 by neejha
 */

package com.distributed.kv.storage.imp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.distributed.kv.exception.KvException;
import com.distributed.kv.storage.Storage;

/**
 * The Class FileSystemStorage.
 */
@Component
public class FileSystemStorage implements Storage {

  /** The data directory. */
  @Value("${data.directory}")
  private String dataDirectory;

  /** The delimeter. */
  @Value("${data.delimeter}")
  private String delimeter;

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(FileSystemStorage.class);

  /**
   * Inits the.
   */
  @PostConstruct
  private void init() {
    logger.debug("Creating data directory");
    File file = new File(dataDirectory);
    if (!file.exists()) {
      file.mkdirs();
    }
    logger.debug("Data directory creation complete");
  }

  /*
   * (non-Javadoc)
   * @see com.distributed.kv.storage.Storage#store(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void store(String key, String value) throws KvException {
    int hash = key.hashCode();
    String fileName = dataDirectory + "/" + hash;
    logger.debug("file path = " + fileName);
    Path path = Paths.get(fileName);
    StringJoiner joiner = new StringJoiner("");
    joiner.add(key).add(delimeter).add(value);
    logger.debug("Data to be saved = " + joiner.toString());
    try {
      if (!Files.exists(path)) {
        createNewHashEntry(path, joiner);
        logger.debug("New file created");
      } else {
        updateExistingHash(path, fileName, key, joiner);
        logger.debug("Updated existing file");
      }
    } catch (IOException e) {
      logger.error("Error while creating Entry " + e.getMessage());
      throw new KvException("Error while creating Entry", e);
    }
  }

  /**
   * Creates the new hash entry.
   *
   * @param path the path
   * @param joiner the joiner
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void createNewHashEntry(Path path, StringJoiner joiner) throws IOException {
    path = Files.createFile(path);
    String content = joiner.toString();
    Files.write(path, content.getBytes());
  }

  /**
   * Update existing hash.
   *
   * @param path the path
   * @param fileName the file name
   * @param key the key
   * @param joiner the joiner
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void updateExistingHash(Path path, String fileName, String key, StringJoiner joiner) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(fileName));
    List<String> lines = new ArrayList<String>();
    boolean found = updateAlreadyExistingKey(reader, key, joiner, lines);
    if (!found) {
      logger.debug("Creating new entry in existing file");
      lines.add(joiner.toString());
    }
    reader.close();
    Files.write(path, lines);
  }

  /**
   * Update already existing key.
   *
   * @param reader the reader
   * @param key the key
   * @param joiner the joiner
   * @param lines the lines
   * @return true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private boolean updateAlreadyExistingKey(BufferedReader reader, String key, StringJoiner joiner, List<String> lines)
      throws IOException {
    String line;
    boolean found = false;
    while ((line = reader.readLine()) != null) {
      if (line.startsWith(key)) {
        found = true;
        logger.debug("Updating existing key");
        lines.add(joiner.toString());
      } else {
        lines.add(line);
      }
    }
    return found;
  }

  /*
   * (non-Javadoc)
   * @see com.distributed.kv.storage.Storage#retrieve(java.lang.String)
   */
  @Override
  public String retrieve(String key) throws KvException {
    int hash = key.hashCode();
    String fileName = dataDirectory + "/" + hash;
    Path path = Paths.get(fileName);
    logger.debug("file path = " + fileName);
    String value = null;
    try {
      if (Files.exists(path)) {
        Stream<String> lines = Files.lines(path);
        Optional<String> optional = lines.filter(line -> line.startsWith(key)).findFirst();
        if (optional.isPresent()) {
          value = optional.get().split(delimeter)[1];
        }
        lines.close();
      }
    } catch (IOException e) {
      logger.error("Error while creating Entry " + e.getMessage());
      throw new KvException("Error while reading entry", e);
    }
    return value;
  }

}
