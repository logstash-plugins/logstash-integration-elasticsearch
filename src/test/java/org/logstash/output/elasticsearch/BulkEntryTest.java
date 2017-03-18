package org.logstash.output.elasticsearch;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jls on 3/15/2017.
 */
public class BulkEntryTest {
  private Map<String, Map> action = null;
  private Map<String, Object> source = null;

  @Before
  public void setUp() {
    action = new HashMap<>();
    Map<String, Object> actionArgs = new HashMap<>();
    source = new HashMap<>();

    action.put("index", actionArgs);
    actionArgs.put("_index", "test");
    actionArgs.put("_type", "type1");

    source.put("key", "value");
  }

  @Test
  public void foo() throws IOException {
    BulkEntry entry = new BulkEntry(action, source);
    ByteBuffer s = BulkEntryWriter.serialize(entry);
    System.err.println("---");
    System.err.printf("Count: %d\n", s.limit());
    System.err.println("---");
    byte[] b = new byte[s.limit()];
    s.get(b);
    System.err.write(b);
    System.err.println();

  }

}