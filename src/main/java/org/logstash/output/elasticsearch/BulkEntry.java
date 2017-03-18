package org.logstash.output.elasticsearch;

import java.util.Map;

class BulkEntry {
  private final Map<String, Map> action; // XXX: Refactor this into specific Action class types
  private final Map<String, Object> source;

  public BulkEntry(Map<String, Map> action, Map<String, Object> source) {
    this.action = action;
    this.source = source;
  }

  public Map<String, Map> getAction() {
    return action;
  }

  public Map<String, Object> getSource() {
    return source;
  }

  public boolean hasSource() {
    return source != null;
  }
}
