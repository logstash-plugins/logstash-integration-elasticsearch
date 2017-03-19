package org.logstash.output.elasticsearch;

import org.apache.http.HttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class ElasticsearchOutput {
  private final RestClient client;
  private static final int maxBulkRequestSize = 20 << 20;

  // TODO: 3/18/2017 This should probably be a static final field
  private static final Map<String, String> emptyParams = Collections.emptyMap();

  public ElasticsearchOutput(RestClient client) {
    // new HttpHost("192.168.1.195", 9200, "http"))
    //client = RestClient.builder(hosts)
            //.setDefaultHeaders(new BasicHeader("Content-Type", "application/json"))
            //.build();
    this.client = client;
  }

  /* XXX: should be List<RubyEvent> */
  public void receive(List<Object> events) {
    HoldingIterator<BulkEntry> iterator = new HoldingIterator<>(new BulkEntryGenerator<>(events.iterator()));

    long start = System.nanoTime();
    int requestCount = 0;

    // While there are more BulkEntry objects
    while (iterator.hasNext()) {
      // TODO: 3/18/2017 Move request handling to a different method or separate class.
      ElasticsearchBulkInputStream bulkStream = new ElasticsearchBulkInputStream(iterator, maxBulkRequestSize);
      HttpEntity entity = new InputStreamEntity(bulkStream);
      assert client != null;
      //Response response = client.performRequest("POST", "/_bulk", emptyParams, entity);
      client.performRequest("POST", "/_bulk", emptyParams, entity);

      // TODO: 3/18/2017 Handle the response. 
      //ObjectMapper mapper = new ObjectMapper();
      requestCount++;
    }
    //long duration = System.nanoTime() - start;
  }
}
