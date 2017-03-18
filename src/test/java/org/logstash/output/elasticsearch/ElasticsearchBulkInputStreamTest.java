package org.logstash.output.elasticsearch;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by jls on 3/17/2017.
 */
public class ElasticsearchBulkInputStreamTest {
  private static final Random random = new Random();
  private List<BulkEntry> entries;

  private int maxBulkRequestSize;

  @Before
  public void setUp() {
    maxBulkRequestSize = random.nextInt(1 << 20) + 1;

    entries = new ArrayList<>();

    for (int i = 0; i < 500000; i++) {
      Map<String, Map> action = new HashMap<>();
      Map<String, Object> actionArgs = new HashMap<>();
      Map<String, Object> source = new HashMap<>();
      action.put("index", actionArgs);
      actionArgs.put("_index", "test");
      actionArgs.put("_type", "type1");
      source.put("key", "value");

      entries.add(new BulkEntry(action, source));
    }
  }

  @Test
  public void read() throws Exception {
    HoldingIterator<BulkEntry> iterator = new HoldingIterator<>(entries.iterator());

    int count = 0;
    while (iterator.hasNext()) {
      byte[] buf = new byte[16384];
      ElasticsearchBulkInputStream stream = new ElasticsearchBulkInputStream(iterator, maxBulkRequestSize);
      //noinspection StatementWithEmptyBody
      while (stream.read(buf) > 0) {
        // Nothing to do. We just want to read it all.
      }
      count += 1;
    }

    assertTrue("Should split into multiple independent streams.", count > 1);

  }
}