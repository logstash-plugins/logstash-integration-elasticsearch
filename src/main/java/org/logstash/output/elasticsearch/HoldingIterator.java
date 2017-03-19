package org.logstash.output.elasticsearch;

import java.util.Iterator;

/**
 * A wrapper for an iterator that allows you to put an item back onto the iterator.
 *
 * This is useful because Iterator class doesn't allow you to peek at the next value.
 *
 * @param <T>
 */
class HoldingIterator<T> implements Iterator<T> {
  private final Iterator<T> iterator;
  private T hold = null;

  HoldingIterator(Iterator<T> iterator) {
    this.iterator = iterator;
  }

  /**
   * @return True, if the underlying iterator's hasNext() returns true or if there is an value being held. False, otherwise.
   */
  public boolean hasNext() {
    return holding() || iterator.hasNext();
  }

  /**
   * Get the next value.
   *
   * If there is an value being held, that value is returned and the hold is cleared.
   *
   * If no value is being held, the underlying iterator's next() value is used as the return value.
   *
   * @return The hold value if a value is being held. Otherwise, returns the next value from the iterator.
   */
  public T next() {
    if (holding()) {
      T value = hold;
      hold = null;
      return value;
    }

    return iterator.next();
  }

  /**
   * Is there a value currently being held?
   */
  boolean holding() {
    return hold != null;
  }

  /**
   * Holds a value.
   *
   * Any previously held value is forgotten.
   * @param value The object to be held.
   */
  void hold(T value) {
    hold = value;
  }
}
