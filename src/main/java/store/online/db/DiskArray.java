package store.online.db;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;

/**
 * 
 * @author Alfredo
 * 
 * @param <E> element type
 */
public final class DiskArray<H, E> implements AutoCloseable {

  /**
   * Fixed-size codec for element type {@code E}.
   * Encodes/decodes one element at a fixed byte width so the structure remains
   * truly array-like (O(1) random access).
   * 
   * @author Alfredo
   */
  public interface FixedCodec<E> {
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

  private static final int PRELUDE = 4; // [capacity : 4 bytes]

  private MappedByteBuffer buf;
  private final FileChannel ch;
  private final FixedCodec<E> blockCodec;
  private final FixedCodec<H> headerCodec;

  public DiskArray(Path path,
      int initialCapacity,
      FixedCodec<H> headerCodec,
      FixedCodec<E> blockCodec) throws IOException {
    if (initialCapacity < 1)
      throw new IllegalArgumentException("Capacity must be >= 1");
    if (blockCodec == null)
      throw new IllegalArgumentException("Element codec is required");

    // Initialize and open file channel to map memory
    this.blockCodec = blockCodec;
    this.headerCodec = headerCodec;

    final boolean existed = Files.exists(path);

    this.ch = FileChannel.open(
        path,
        StandardOpenOption.CREATE,
        StandardOpenOption.READ,
        StandardOpenOption.WRITE);

    if (!existed || ch.size() < PRELUDE) {
      long bytes = dataStart() + (long) initialCapacity * blockCodec.fixedSize();
      ch.truncate(bytes);
      buf = ch.map(FileChannel.MapMode.READ_WRITE, 0, bytes);
      buf.order(ByteOrder.LITTLE_ENDIAN);
      buf.putInt(0, initialCapacity);
      return;
    }

    buf = ch.map(FileChannel.MapMode.READ_WRITE, 0, dataStart());
    buf.order(ByteOrder.LITTLE_ENDIAN);

    long bytes = dataStart() + (long) buf.getInt(0) * blockCodec.fixedSize();
    buf = ch.map(FileChannel.MapMode.READ_WRITE, 0, bytes);
    buf.order(ByteOrder.LITTLE_ENDIAN);
  }

  /**
   * @return current capacity (how many elements fit without growing)
   */
  public int capacity() {
    return buf.getInt(0);
  }

  /**
   * Read the header (if present).
   * 
   * @return header
   */
  public H header() {
    if (headerCodec == null)
      return null;
    return headerCodec.read(buf, PRELUDE);
  }

  /**
   * Overwrite the header (if present).
   * 
   * @param hdr
   */
  public void setHeader(H hdr) {
    if (headerCodec == null)
      throw new IllegalStateException("No header codec configured for this DiskArray");
    headerCodec.write(buf, PRELUDE, hdr);
  }

  /**
   * Random access read.
   *
   * @param index position in [0, size())
   * @return element at {@code index}
   * @throws IndexOutOfBoundsException if {@code index} is out of bounds
   */
  public E get(int index) {
    check(index);
    int pos = dataStart() + index * blockCodec.fixedSize();
    return blockCodec.read(buf, pos);
  }

  /**
   * Random access write (in-place).
   *
   * @param index position in [0, size())
   * @param value new value to store
   * @return the previous value at {@code index}
   * @throws IndexOutOfBoundsException if {@code index} is out of bounds
   */
  public E set(int index, E value) {
    check(index);
    int pos = dataStart() + index * blockCodec.fixedSize();
    E prev = blockCodec.read(buf, pos);
    blockCodec.write(buf, pos, value);
    return prev;
  }

  /**
   * Grows the array by a given capacity
   * 
   * @param capacity new capacity to expand the array to
   * @throws IOException If fails reallocation
   */
  public void grow(int capacity) throws IOException {
    long newBytes = dataStart() + (long) capacity * blockCodec.fixedSize();
    ch.truncate(newBytes);
    ch.force(true);
    buf = ch.map(FileChannel.MapMode.READ_WRITE, 0, newBytes);
    buf.order(ByteOrder.LITTLE_ENDIAN);
    buf.putInt(0, capacity);
  }

  /**
   * Closes the underlying channel/mapping resources.
   *
   * @throws IOException if closing the backing file/channel fails
   */
  @Override
  public void close() throws IOException {
    ch.close();
  }

  // ---- internals ----
  private int dataStart() {
    return PRELUDE + (headerCodec != null ? headerCodec.fixedSize() : 0);
  }

  private void check(int index) {
    if (index < 0 || index >= capacity())
      throw new IndexOutOfBoundsException(index + " of " + capacity());
  }

  // ---------- ready-made codecs ----------
  public static final class IntCodec implements FixedCodec<Integer> {
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

  public static final class LongCodec implements FixedCodec<Long> {
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

  /** Fixed-size UTF-8 string padded/truncated to N bytes. */
  public static final class FixedStringCodec implements FixedCodec<String> {
    private final int width;

    public FixedStringCodec(int width) {
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
