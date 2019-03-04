
/*
 * Created on 4 Mar, 2019 by neejha
 */

package com.distributed.kv.controller;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.zeroturnaround.zip.ZipUtil;

import com.distributed.kv.KvApplication;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.LeaseInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;

/**
 * The Class AppControllerTest.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = KvApplication.class)
@PropertySource("classpath:test.yml")
public class AppControllerTest {

  /** The port. */
  @LocalServerPort
  protected int port;

  /** The rest template. */
  @MockBean
  private RestTemplate restTemplate;

  /** The test rest template. */
  protected TestRestTemplate testRestTemplate;

  /** The eureka client. */
  @MockBean
  private EurekaClient eurekaClient;

  /** The data directory. */
  @Value("${data.directory}")
  private String dataDirectory;

  /**
   * Instantiates a new app controller test.
   */
  public AppControllerTest() {

  }

  /**
   * Inits.
   *
   * @throws IOException Signals that an I/O exception has occurred.
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
    testRestTemplate = new TestRestTemplate();
  }

  /**
   * Test set and get.
   */
  @Test
  public void testSetAndGet() {
    ResponseEntity<String> responseEntity = new ResponseEntity<String>("OK", HttpStatus.OK);
    doReturn(responseEntity).when(restTemplate).postForEntity("http://localhost:8080/sync/neeraj", "jha", String.class);
    ResponseEntity<String> response =
        testRestTemplate.postForEntity("http://localhost:" + port + "/set/neeraj", "jha", String.class);
    assertEquals("OK", response.getBody());
    response = testRestTemplate.getForEntity("http://localhost:" + port + "/get/neeraj", String.class);
    assertEquals("jha", response.getBody());
    response = testRestTemplate.getForEntity("http://localhost:" + port + "/get/neeraj1", String.class);
    assertEquals(null, response.getBody());
  }

  /**
   * Test sync and get.
   */
  @Test
  public void testSyncAndGet() {
    ResponseEntity<String> response =
        testRestTemplate.postForEntity("http://localhost:" + port + "/sync/neeraj1", "jha1", String.class);
    assertEquals("OK", response.getBody());
    response = testRestTemplate.getForEntity("http://localhost:" + port + "/get/neeraj1", String.class);
    assertEquals("jha1", response.getBody());
  }

  /**
   * Test bulk sync.
   */
  @Test
  public void testBulkSync() {
    ResponseEntity<String> responseEntity = new ResponseEntity<String>("OK", HttpStatus.OK);
    doReturn(responseEntity).when(restTemplate).postForEntity("http://localhost:8080/sync/key", "value", String.class);
    ResponseEntity<String> response =
        testRestTemplate.postForEntity("http://localhost:" + port + "/set/key", "value", String.class);
    assertEquals("OK", response.getBody());
    ResponseEntity<byte[]> bResponse =
        testRestTemplate.getForEntity("http://localhost:" + port + "/bulkSync", byte[].class);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ZipUtil.pack(new File(dataDirectory), output);
    assertArrayEquals(output.toByteArray(), bResponse.getBody());
  }

}
