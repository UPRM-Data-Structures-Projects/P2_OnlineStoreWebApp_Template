package store.online.db;

import java.nio.MappedByteBuffer;

public class FixedSizeSerializer {
  /**
   * Fixed-Serializable object used to encode and decode an element type
   * {@code E}.
   * Encodes/decodes one element at a fixed byte width so the structure remains
   * truly array-like (O(1) random access).
   * 
   * @author Alfredo
   */
  public interface FixedElementSerializer<E> {
    /**
     * @return the exact number of bytes used per element
     */
    public int fixedSize();

    /**
     * Writes one element at absolute position {@code pos} in the mapped buffer.
     *
     * @param buf   mapped buffer (absolute writes recommended)
     * @param pos   absolute byte offset where the element starts
     * @param value element to encode
     */
    public void write(MappedByteBuffer buf, int pos, E value);

    /**
     * Reads one element from absolute position {@code pos} in the mapped buffer.
     *
     * @param buf mapped buffer
     * @param pos absolute byte offset where the element starts
     * @return decoded element
     */
    public E read(MappedByteBuffer buf, int pos);
  }

  /**
   * 
   * @author Alfredo
   */
  public static final class IntSerializer implements FixedElementSerializer<Integer> {
    public int fixedSize() {
      return 4;
    }

    public void write(MappedByteBuffer buf, int pos, Integer v) {
      buf.putInt(pos, v);
    }

    public Integer read(MappedByteBuffer buf, int pos) {
      return buf.getInt(pos);
    }
  }

  /**
   * 
   * @author Alfredo
   */
  public static final class LongSerializer implements FixedElementSerializer<Long> {
    public int fixedSize() {
      return 8;
    }

    public void write(MappedByteBuffer buf, int pos, Long v) {
      buf.putLong(pos, v);
    }

    public Long read(MappedByteBuffer buf, int pos) {
      return buf.getLong(pos);
    }
  }

  /**
   * 
   * @author Alfredo
   */
  public static final class StringSerializer implements FixedElementSerializer<String> {
    private final int width;

    public StringSerializer(int width) {
      this.width = width;
    }

    public int fixedSize() {
      return width;
    }

    public void write(MappedByteBuffer buf, int pos, String s) {
      byte[] b = s.getBytes(java.nio.charset.StandardCharsets.UTF_8);
      int n = Math.min(b.length, width);
      for (int i = 0; i < n; i++)
        buf.put(pos + i, b[i]);
      for (int i = n; i < width; i++)
        buf.put(pos + i, (byte) 0);
    }

    public String read(MappedByteBuffer buf, int pos) {
      byte[] b = new byte[width];
      for (int i = 0; i < width; i++)
        b[i] = buf.get(pos + i);
      int n = 0;
      while (n < width && b[n] != 0)
        n++;
      return new String(b, 0, n, java.nio.charset.StandardCharsets.UTF_8);
    }
  }
}
