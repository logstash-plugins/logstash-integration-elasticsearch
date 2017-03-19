package org.logstash.output.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

class BulkEntryWriter {
  private static final ObjectMapper mapper = new ObjectMapper();

  private static void write(OutputStream out, BulkEntry entry) throws IOException {
    // XXX: Move this to some kind of serialize method in org.logstash.output.elasticsearch.BulkEntry?
    mapper.writeValue(out, entry.getAction());
    out.write('\n');
    if (entry.hasSource()) {
      mapper.writeValue(out, entry.getSource());
      out.write('\n');
    }
  }

  /*
   * ByteArrayOutputStream has a protected 'buf' that I'd like to access, so this
   * subclass is the way to get at it.
   *
   * XXX: ByteArrayOutputStream grows the byte[] buf by doubling the size.
   *      This growth model may be undesirable at larger sizes.
   */
  private static class OpenByteArrayOutputStream extends ByteArrayOutputStream {
    byte[] getBytes() {
      return buf;
    }
  }

  static ByteBuffer serialize(BulkEntry entry) {
    OpenByteArrayOutputStream out = new OpenByteArrayOutputStream();
    try {
      BulkEntryWriter.write(out, entry);
    } catch (IOException e) {
      // This should never happen because ByteArrayOutputStream doesn't throw IOException
      // XXX: Wrap it up in a runtime exception to signal a bug?
    }
    return ByteBuffer.wrap(out.getBytes(), 0, out.size());
  }
}
