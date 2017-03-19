package org.logstash.output.elasticsearch;

import java.util.Iterator;

/**
 * Created by jls on 3/18/2017.
 */
class BulkEntryGenerator<T> implements Iterator<T> {
  // TODO: 3/18/2017 iterator should be of type Iterator<RubyEvent>
  private final Iterator<Object> iterator;

  BulkEntryGenerator(Iterator<Object> iterator) {
    this.iterator = iterator;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public T next() {
    // TODO: 3/18/2017 Generate BulkEntry from the next RubyEvent
    return null;
  }
}
