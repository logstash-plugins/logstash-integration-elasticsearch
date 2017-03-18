package org.logstash.output.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import static org.junit.Assert.*;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by jls on 3/16/2017.
 */
public class ElasticsearchClientIntegrationTest {
  private static final Random random = new Random();
  private List<BulkEntry> entries;
  private int maxBulkRequestSize;
  private int entryCount;
  private RestClient client;
  private String indexName;

  private static final char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
  private static String randomAlphabet(int length) {
    return IntStream.range(0,length).boxed().map(i -> String.valueOf(alphabet[random.nextInt(alphabet.length)]))
            .collect(Collectors.joining());
  }

  @Before
  public void setUp() {
    indexName = "testindex-" + randomAlphabet(10);
    maxBulkRequestSize = random.nextInt(1 << 20) + 1;
    entryCount = random.nextInt(100000) + 1;

    entries = new ArrayList<>();

    for (int i = 0; i < entryCount; i++) {
      Map<String, Map> action = new HashMap<>();
      Map<String, Object> actionArgs = new HashMap<>();
      Map<String, Object> source = new HashMap<>();
      action.put("index", actionArgs);
      actionArgs.put("_index", indexName);
      actionArgs.put("_type", "typeabc1");
      source.put("key", "value");

      entries.add(new BulkEntry(action, source));
    }

    client = RestClient.builder(
            new HttpHost("192.168.1.195", 9200, "http")
    ).build();
  }

  @After
  public void tearDown() throws IOException {
    client.performRequest("DELETE", "/" + indexName);
    client.close();
  }

  @Test
  public void thing() throws IOException {
    HoldingIterator<BulkEntry> iterator = new HoldingIterator<>(entries.iterator());

    Map<String, String> emptyParams = Collections.emptyMap();

    long start = System.nanoTime();
    // While there are more BulkEntry objects
    int requestCount = 0;
    while (iterator.hasNext()) {
      ElasticsearchBulkInputStream bulkStream = new ElasticsearchBulkInputStream(iterator, maxBulkRequestSize);
      HttpEntity entity = new InputStreamEntity(bulkStream);
      Response response = client.performRequest("POST", "/_bulk", emptyParams, entity);

      // XXX: Does the entity need to be consumed?
      EntityUtils.consume(response.getEntity());
      requestCount++;
    }

    long duration = System.nanoTime() - start;
    System.err.printf("%d bulk entries split across %d requests took a total of %dus\n", entryCount, requestCount, duration / 1000);

    client.performRequest("POST", "/" + indexName + "/_refresh");

    // Ask Elasticsearch for a document count.
    Response response = client.performRequest("GET", "/" + indexName + "/_count");
    ObjectMapper mapper = new ObjectMapper();
    Map result = mapper.readValue(response.getEntity().getContent(), Map.class);
    int searchCount = (Integer) result.get("count");

    assertEquals("Wrote " + entryCount + " documents. Expected this many in Elasticsearch, but got " + searchCount + " instead.", entryCount, searchCount);
  }
}

