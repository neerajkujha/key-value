/*
 * Created on 3 Mar, 2019 by neejha
 */

package com.distributed.kv.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.distributed.kv.exception.KvException;
import com.distributed.kv.storage.imp.FileSystemStorage;

/**
 * The Class FileSystemStorageTest.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Storage.class, FileSystemStorage.class })
@EnableConfigurationProperties
public class FileSystemStorageTest {

  /** The file system storage. */
  @Autowired
  private Storage fileSystemStorage;

  /** The data directory. */
  @Value("${data.directory}")
  private String dataDirectory;

  /** The delimeter. */
  @Value("${data.delimeter}")
  private String delimeter;

  /**
   * Instantiates a new file system storage test.
   */
  public FileSystemStorageTest() {

  }

  /**
   * Init.
   * 
   * @throws IOException
   */
  @Before
  public void init() throws IOException {
    Path pathToBeDeleted = Paths.get(dataDirectory);
    Files.walk(pathToBeDeleted).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    Files.createDirectories(pathToBeDeleted);
  }

  /**
   * Test store and get new value.
   *
   * @throws KvException the kv exception
   */
  @Test
  public void testStoreAndGetNewValue() throws KvException {
    fileSystemStorage.store("neeraj", "jha");
    Path path = Paths.get(dataDirectory + "/" + "neeraj".hashCode());
    assertTrue(Files.exists(path));
    List<String> list = new ArrayList<>();
    try (Stream<String> stream = Files.lines(path)) {
      list = stream.collect(Collectors.toList());
    } catch (IOException e) {
      e.printStackTrace();
    }
    assertEquals(1, list.size());
    assertEquals("neeraj~@@@~jha", list.get(0));
    assertEquals("jha", fileSystemStorage.retrieve("neeraj"));
  }

  /**
   * Test update and get value.
   *
   * @throws KvException the kv exception
   */
  @Test
  public void testUpdateAndGetValue() throws KvException {
    fileSystemStorage.store("neeraj1", "jha");
    Path path = Paths.get(dataDirectory + "/" + "neeraj1".hashCode());
    assertTrue(Files.exists(path));
    List<String> list = new ArrayList<>();
    try (Stream<String> stream = Files.lines(path)) {
      list = stream.collect(Collectors.toList());
    } catch (IOException e) {
      e.printStackTrace();
    }
    assertEquals(1, list.size());
    assertEquals("neeraj1~@@@~jha", list.get(0));
    fileSystemStorage.store("neeraj1", "jha1");
    assertTrue(Files.exists(path));
    list = new ArrayList<>();
    try (Stream<String> stream = Files.lines(path)) {
      list = stream.collect(Collectors.toList());
    } catch (IOException e) {
      e.printStackTrace();
    }
    assertEquals(1, list.size());
    assertEquals("neeraj1~@@@~jha1", list.get(0));
    assertEquals("jha1", fileSystemStorage.retrieve("neeraj1"));
  }

  /**
   * Test store and get hash code collision.
   *
   * @throws KvException the kv exception
   */
  @Test
  public void testStoreAndGetHashCodeCollision() throws KvException {
    fileSystemStorage.store("FB", "jha");
    fileSystemStorage.store("Ea", "jha1");
    assertEquals("FB".hashCode(), "Ea".hashCode());
    Path path = Paths.get(dataDirectory + "/" + "FB".hashCode());
    assertTrue(Files.exists(path));
    List<String> list = new ArrayList<>();
    try (Stream<String> stream = Files.lines(path)) {
      list = stream.collect(Collectors.toList());
    } catch (IOException e) {
      e.printStackTrace();
    }
    assertEquals(2, list.size());
    assertEquals("FB~@@@~jha", list.get(0));
    assertEquals("Ea~@@@~jha1", list.get(1));
    assertEquals("jha1", fileSystemStorage.retrieve("Ea"));
    assertEquals("jha", fileSystemStorage.retrieve("FB"));
  }

  /**
   * Test store and get null key.
   *
   * @throws KvException the kv exception
   */
  @Test
  public void testStoreAndGetNullKey() throws KvException {
    try {
      fileSystemStorage.store(null, null);
      fail("null value should throw exception");
    } catch (Exception e) {
    }
  }

  /**
   * Test store and get null value.
   *
   * @throws KvException the kv exception
   */
  @Test
  public void testStoreAndGetNullValue() throws KvException {
    fileSystemStorage.store("Key", null);
    assertEquals(null, fileSystemStorage.retrieve("key"));

  }
}
