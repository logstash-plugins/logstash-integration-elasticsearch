package org.logstash.output.elasticsearch;

import java.util.Iterator;

class HoldingIterator<T> implements Iterator<T> {
  private final Iterator<T> iterator;
  private T hold = null;

  HoldingIterator(Iterator<T> iterator) {
    this.iterator = iterator;
  }

  public boolean hasNext() {
    return holding() || iterator.hasNext();
  }

  public T next() {
    if (holding()) {
      T value = hold;
      hold = null;
      return value;
    }

    return iterator.next();
  }

  boolean holding() {
    return hold != null;
  }

  void hold(T value) {
    hold = value;
  }
}
