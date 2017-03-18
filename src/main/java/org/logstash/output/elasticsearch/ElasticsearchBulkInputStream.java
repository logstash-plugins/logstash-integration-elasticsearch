package org.logstash.output.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Created by jls on 3/16/2017.
 */
class ElasticsearchBulkInputStream extends InputStream {

  // The source of BulkEntry objects.
  private final HoldingIterator<BulkEntry> iterator;

  // Maximum size, in bytes, for this stream to produce. One exception is that if the very first BulkEntry
  // would exceed this byte size, we will write it anyway.
  private final int maxBulkRequestSize;

  // The total bytes length of data serialized so far.
  private int totalBytes = 0;

  // How many bytes are available in all internal buffers.
  private int bytesAvailable = 0;

  // Are we at EOF? (EOF is when we will stop serializing new BulkEntry objects)
  private boolean eof;

  private final LinkedList<ByteBuffer> buffers = new LinkedList<>();

  public ElasticsearchBulkInputStream(HoldingIterator<BulkEntry> iterator, int maxBulkRequestSize) {
    this.iterator = iterator;
    this.maxBulkRequestSize = maxBulkRequestSize;
  }

  public int available() {
    return 0;
  }

  @Override
  public int read() throws IOException {
    byte[] b = new byte[1];
    int value = read(b);
    if (value == -1) {
      return value;
    }
    return b[0];
  }

  public int read(byte[] bytes) {
    return read(bytes, 0, bytes.length);
  }

  public int read(byte[] bytes, int offset, int length) {
    int count = 0;

    // Make sure we  have enough serialized data.
    get(length);

    //System.err.printf("read(..., %d, %d)\n", offset, length);
    //System.err.printf(" . bytesAvailable: %d\n", bytesAvailable);
    //System.err.printf(" . eof: %s\n", eof);
    if (eof && bytesAvailable == 0) {
      return -1;
    }

    while (count < length && bytesAvailable > 0) {
      // Remove a buffer. Copy it into `bytes`
      ByteBuffer buffer = buffers.getFirst();

      int bufferRead = Math.min(buffer.remaining(), length - count);
      //System.err.printf("%d: Buffer get. limit: %d, remaining: %d, asked length: %d\n", count, buffer.remaining(), buffer.remaining(), length);
      buffer.get(bytes, offset, bufferRead);
      offset += bufferRead;
      count += bufferRead;
      bytesAvailable -= bufferRead;

      if (!buffer.hasRemaining()) {
        // Remove the buffer if it's been fully read.
        buffers.removeFirst();
      }
    }

    return count;
  }

  private void push(ByteBuffer buffer) {
    buffers.addLast(buffer);
    totalBytes += buffer.remaining();
    bytesAvailable += buffer.remaining();
  }

  private void setEOF() {
    eof = true;
  }

  private void get(int requestedLength) {
    if (eof || bytesAvailable >= requestedLength) {
      // We don't need to do a read.
      return;
    }

    if (!iterator.hasNext()) {
      setEOF();
      return;
    }

    while (iterator.hasNext() && requestedLength > 0) {
      BulkEntry entry = iterator.next();
      ByteBuffer entrySerialized = BulkEntryWriter.serialize(entry);

      if (entrySerialized.remaining() + totalBytes > maxBulkRequestSize) {
        setEOF();

        if (totalBytes == 0) {
          // Nothing written yet, and this org.logstash.output.elasticsearch.BulkEntry exceeds maxBulkRequestSize, so we should write it now.
          push(entrySerialized);
        } else {
          /* We have enough data written for this HttpEntity and do not want to exceed maxBulkRequestSize.
           * So let's hold the current entry and conclude writing.
           * The idea is that the current entry is pushed back (hold) onto the iterator so the next
           * HttpEntity will include it.
           */
          // XXX: It'd be nice to save the serialized result of the held org.logstash.output.elasticsearch.BulkEntry so we don't have to serialize it again.
          iterator.hold(entry);
        }

        // XXX: Reject future calls to next()
        // Stop trying to write since we want to avoid exceeding maxBulkRequestSize.
        return;
      }

      push(entrySerialized);
    }
  }

}
