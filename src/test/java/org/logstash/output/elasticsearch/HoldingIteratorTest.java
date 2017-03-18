package org.logstash.output.elasticsearch;

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HoldingIteratorTest {
  private Iterator<Boolean> iterator;
  private HoldingIterator<Boolean> holdingIterator;

  @Before
  public void setUp() {
    int itemCount = 3;
    List<Boolean> list = new LinkedList<>();
    for (int i = 0; i < itemCount; i++) {
      list.add(true);
    }
    iterator = list.iterator();
    holdingIterator = new HoldingIterator<>(iterator);
  }

  @Test
  public void holding() {
    assertFalse("Should initially not hold anything", holdingIterator.holding());

    holdingIterator.hold(holdingIterator.next());
    assertTrue("holding() should return true when holding", holdingIterator.holding());

    holdingIterator.next();
    assertFalse("holding() should return false after next() is called.", holdingIterator.holding());
  }

  @Test
  public void hasNext() {
    // Walk to the end of the iterator
    while (iterator.hasNext()) {
      assertTrue("hasNext() should return true when the wrapped iterator's hasNext() returns true", holdingIterator.hasNext());
      holdingIterator.next();
    }

    // Make sure our test is running correctly.
    assertFalse("Wrapped iterator should now be empty (if not, this may be a bug in the test.", iterator.hasNext());

    assertFalse("hasNext() should return false when the wrapped iterator's hasNext() returns false", holdingIterator.hasNext());

    holdingIterator.hold(true);
    assertTrue("hasNext() should return true when holding.", holdingIterator.hasNext());
    holdingIterator.next();
    assertFalse("hasNext() should return false when *not* holding and the wrapped iterator is ended.", holdingIterator.hasNext());
  }

  @Test
  public void next() {
    while (holdingIterator.hasNext()) {
      assertTrue(iterator.hasNext());
      holdingIterator.next();
    }
    assertFalse(iterator.hasNext());

    holdingIterator.hold(true);
    assertTrue("next() should return the hold value", holdingIterator.next());
  }
}