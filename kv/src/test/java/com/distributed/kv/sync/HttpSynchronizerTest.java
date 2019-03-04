/*
 * Created on 4 Mar, 2019 by neejha
 */

package com.distributed.kv.sync;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.zeroturnaround.zip.ZipUtil;

import com.distributed.kv.exception.KvException;
import com.distributed.kv.storage.Storage;
import com.distributed.kv.storage.imp.FileSystemStorage;
import com.distributed.kv.sync.imp.HttpSynchronizer;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.LeaseInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;

/**
 * The Class HttpSynchronizerTest.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Synchronizer.class, HttpSynchronizer.class, Storage.class, FileSystemStorage.class })
@EnableConfigurationProperties
public class HttpSynchronizerTest {

  /** The synchronizer. */
  @Autowired
  private Synchronizer synchronizer;

  /** The rest template. */
  @MockBean
  private RestTemplate restTemplate;

  /** The eureka client. */
  @MockBean
  private EurekaClient eurekaClient;

  /** The file system storage. */
  @Autowired
  private Storage fileSystemStorage;

  /** The data directory. */
  @Value("${data.directory}")
  private String dataDirectory;

  /**
   * Init.
   * 
   * @throws IOException
   */
  @Before
  public void init() throws IOException {
    List<InstanceInfo> instanceList = new ArrayList<>();
    InstanceInfo instanceInfo = new InstanceInfo("1", "KV-SERVICE", "KV", "localhost", "1", null, null,
        "http://localhost:8080/", "http://localhost:8080/", "http://localhost:8080/", "http://localhost:8080/", null,
        null, 1, null, "localhost", null, null, null, new LeaseInfo(5000, 5000, 5000L, 5000L, 5000L, 5000L, 5000L),
        null, null, 5000L, 5000L, null, null);
    instanceList.add(instanceInfo);
    Application application = new Application("KV-SERVICE", instanceList);
    doReturn(application).when(eurekaClient).getApplication("kv-service");
    Path pathToBeDeleted = Paths.get(dataDirectory);
    Files.walk(pathToBeDeleted).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    Files.createDirectories(pathToBeDeleted);
  }

  /**
   * Instantiates a new http synchronizer test.
   */
  public HttpSynchronizerTest() {

  }

  /**
   * Test sync.
   */
  @Test
  public void testSync() {
    ResponseEntity<String> responseEntity = new ResponseEntity<String>("OK", HttpStatus.OK);
    doReturn(responseEntity).when(restTemplate).postForEntity("http://localhost:8080/sync/key", "value", String.class);
    synchronizer.sync("key", "value");
    reset(restTemplate);
  }

  /**
   * Test bulk sync.
   *
   * @throws KvException the kv exception
   */
  @Test
  public void testBulkSync() throws KvException {
    fileSystemStorage.store("abc", "abc");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ZipUtil.pack(new File(dataDirectory), output);
    assertArrayEquals(output.toByteArray(), synchronizer.bulkSync());
  }

  /**
   * Test synchronize to cluster.
   *
   * @throws KvException the kv exception
   */
  @Test
  public void testSynchronizeToCluster() throws KvException {
    byte[] array = { 80, 75, 3, 4, 20, 0, 8, 8, 8, 0, 34, -86, 99, 78, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 0, 18, 0,
        45, 49, 48, 52, 57, 51, 54, 51, 53, 48, 55, 110, 117, 14, 0, -47, -42, 23, 50, -92, -127, 0, 0, 0, 0, 0, 0, 0,
        0, -53, 75, 77, 45, 74, -52, -86, 115, 112, 112, -88, -53, -54, 72, -28, 2, 0, 80, 75, 7, 8, 68, -47, -13, 110,
        17, 0, 0, 0, 15, 0, 0, 0, 80, 75, 3, 4, 20, 0, 8, 8, 8, 0, -99, 125, 100, 78, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 6, 0, 18, 0, 49, 48, 54, 48, 55, 57, 110, 117, 14, 0, -47, -42, 23, 50, -92, -127, 0, 0, 0, 0, 0, 0, 0, 0,
        -53, 78, -83, -84, 115, 112, 112, -88, 43, 75, -52, 41, 77, -27, 2, 0, 80, 75, 7, 8, 63, 47, 107, 99, 16, 0, 0,
        0, 14, 0, 0, 0, 80, 75, 3, 4, 20, 0, 8, 8, 8, 0, -54, -103, 99, 78, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0,
        18, 0, 57, 55, 51, 52, 52, 110, 117, 14, 0, -47, -42, 23, 50, -92, -127, 0, 0, 0, 0, 0, 0, 0, 0, 75, 74, 78,
        -84, 115, 112, 112, -88, -85, -88, -84, 2, 0, 80, 75, 7, 8, -91, -92, -94, 39, 13, 0, 0, 0, 11, 0, 0, 0, 80, 75,
        3, 4, 20, 0, 8, 8, 8, 0, 34, -86, 99, 78, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 18, 0, 55, 53, 51, 50, 55,
        110, 117, 14, 0, -47, -42, 23, 50, -92, -127, 0, 0, 0, 0, 0, 0, 0, 0, -13, 78, -83, -84, 115, 112, 112, -88,
        -53, 43, -51, -55, -31, 2, 0, 80, 75, 7, 8, -98, 20, -79, 24, 15, 0, 0, 0, 13, 0, 0, 0, 80, 75, 3, 4, 20, 0, 8,
        8, 8, 0, 34, -86, 99, 78, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 18, 0, 49, 56, 50, 57, 52, 54, 57, 55, 48,
        48, 110, 117, 14, 0, -47, -42, 23, 50, -92, -127, 0, 0, 0, 0, 0, 0, 0, 0, -53, 75, 77, 45, 74, -52, 50, -84,
        115, 112, 112, -88, -53, -54, 72, 52, -28, 2, 0, 80, 75, 7, 8, -24, 3, 75, 18, 19, 0, 0, 0, 17, 0, 0, 0, 80, 75,
        3, 4, 20, 0, 8, 8, 8, 0, 34, -86, 99, 78, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 18, 0, 50, 50, 51, 54, 110,
        117, 14, 0, -47, -42, 23, 50, -92, -127, 0, 0, 0, 0, 0, 0, 0, 0, 115, 77, -84, 115, 112, 112, -88, -53, -54, 72,
        52, -28, 114, 115, -126, -79, -71, 0, 80, 75, 7, 8, 69, 18, -113, -14, 18, 0, 0, 0, 23, 0, 0, 0, 80, 75, 1, 2,
        20, 0, 20, 0, 8, 8, 8, 0, 34, -86, 99, 78, 68, -47, -13, 110, 17, 0, 0, 0, 15, 0, 0, 0, 11, 0, 18, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 45, 49, 48, 52, 57, 51, 54, 51, 53, 48, 55, 110, 117, 14, 0, -47, -42, 23, 50,
        -92, -127, 0, 0, 0, 0, 0, 0, 0, 0, 80, 75, 1, 2, 20, 0, 20, 0, 8, 8, 8, 0, -99, 125, 100, 78, 63, 47, 107, 99,
        16, 0, 0, 0, 14, 0, 0, 0, 6, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 92, 0, 0, 0, 49, 48, 54, 48, 55, 57, 110,
        117, 14, 0, -47, -42, 23, 50, -92, -127, 0, 0, 0, 0, 0, 0, 0, 0, 80, 75, 1, 2, 20, 0, 20, 0, 8, 8, 8, 0, -54,
        -103, 99, 78, -91, -92, -94, 39, 13, 0, 0, 0, 11, 0, 0, 0, 5, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -78, 0, 0,
        0, 57, 55, 51, 52, 52, 110, 117, 14, 0, -47, -42, 23, 50, -92, -127, 0, 0, 0, 0, 0, 0, 0, 0, 80, 75, 1, 2, 20,
        0, 20, 0, 8, 8, 8, 0, 34, -86, 99, 78, -98, 20, -79, 24, 15, 0, 0, 0, 13, 0, 0, 0, 5, 0, 18, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 4, 1, 0, 0, 55, 53, 51, 50, 55, 110, 117, 14, 0, -47, -42, 23, 50, -92, -127, 0, 0, 0, 0, 0, 0,
        0, 0, 80, 75, 1, 2, 20, 0, 20, 0, 8, 8, 8, 0, 34, -86, 99, 78, -24, 3, 75, 18, 19, 0, 0, 0, 17, 0, 0, 0, 10, 0,
        18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 88, 1, 0, 0, 49, 56, 50, 57, 52, 54, 57, 55, 48, 48, 110, 117, 14, 0, -47,
        -42, 23, 50, -92, -127, 0, 0, 0, 0, 0, 0, 0, 0, 80, 75, 1, 2, 20, 0, 20, 0, 8, 8, 8, 0, 34, -86, 99, 78, 69, 18,
        -113, -14, 18, 0, 0, 0, 23, 0, 0, 0, 4, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -75, 1, 0, 0, 50, 50, 51, 54,
        110, 117, 14, 0, -47, -42, 23, 50, -92, -127, 0, 0, 0, 0, 0, 0, 0, 0, 80, 75, 5, 6, 0, 0, 0, 0, 6, 0, 6, 0, -87,
        1, 0, 0, 11, 2, 0, 0, 0, 0 };
    ResponseEntity<byte[]> responseEntity = new ResponseEntity<byte[]>(array, HttpStatus.OK);
    doReturn(responseEntity).when(restTemplate).getForEntity("http://localhost:8080/bulkSync", byte[].class);
    synchronizer.synchronizeToCluster();
    String val = fileSystemStorage.retrieve("key");
    assertEquals("value", val);
  }
}
