package org.logstash.output.elasticsearch;

import java.util.Map;

class BulkEntry {
  private final Map<String, Map> action; // XXX: Refactor this into specific Action class types
  private final Map<String, Object> source;

  BulkEntry(Map<String, Map> action, Map<String, Object> source) {
    this.action = action;
    this.source = source;
  }

  Map<String, Map> getAction() {
    return action;
  }

  Map<String, Object> getSource() {
    return source;
  }

  boolean hasSource() {
    return source != null;
  }
}
