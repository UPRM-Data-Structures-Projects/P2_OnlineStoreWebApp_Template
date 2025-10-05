package store.online.db;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;

import store.online.db.FixedSizeSerializer.*;

/**
 * 
 * @author Alfredo
 * 
 * @param <E> element type
 */
public final class DiskArray<H, E> implements AutoCloseable {

  private static final int PRELUDE = 4; // [capacity : 4 bytes]

  private MappedByteBuffer buf;
  private final FileChannel ch;
  private final FixedElementSerializer<E> blockSerializer;
  private final FixedElementSerializer<H> headerSerializer;

  public DiskArray(Path path,
      int initialCapacity,
      FixedElementSerializer<H> headerSerializer,
      FixedElementSerializer<E> blockSerializer) throws IOException {
    if (initialCapacity < 1)
      throw new IllegalArgumentException("Capacity must be >= 1");
    if (blockSerializer == null)
      throw new IllegalArgumentException("Element codec is required");

    // Initialize and open file channel to map memory
    this.blockSerializer = blockSerializer;
    this.headerSerializer = headerSerializer;

    final boolean existed = Files.exists(path);

    this.ch = FileChannel.open(
        path,
        StandardOpenOption.CREATE,
        StandardOpenOption.READ,
        StandardOpenOption.WRITE);

    if (!existed || ch.size() < PRELUDE) {
      long bytes = dataStart() + (long) initialCapacity * blockSerializer.fixedSize();
      ch.truncate(bytes);
      buf = ch.map(FileChannel.MapMode.READ_WRITE, 0, bytes);
      buf.order(ByteOrder.LITTLE_ENDIAN);
      buf.putInt(0, initialCapacity);
      return;
    }

    buf = ch.map(FileChannel.MapMode.READ_WRITE, 0, dataStart());
    buf.order(ByteOrder.LITTLE_ENDIAN);

    long bytes = dataStart() + (long) buf.getInt(0) * blockSerializer.fixedSize();
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
    if (headerSerializer == null)
      return null;
    return headerSerializer.read(buf, PRELUDE);
  }

  /**
   * Overwrite the header (if present).
   * 
   * @param hdr
   */
  public void setHeader(H hdr) {
    if (headerSerializer == null)
      throw new IllegalStateException("No header codec configured for this DiskArray");
    headerSerializer.write(buf, PRELUDE, hdr);
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
    int pos = dataStart() + index * blockSerializer.fixedSize();
    return blockSerializer.read(buf, pos);
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
    int pos = dataStart() + index * blockSerializer.fixedSize();
    E prev = blockSerializer.read(buf, pos);
    blockSerializer.write(buf, pos, value);
    return prev;
  }

  /**
   * Grows the array by a given capacity
   * 
   * @param capacity new capacity to expand the array to
   * @throws IOException If fails reallocation
   */
  public void grow(int capacity) throws IOException {
    long newBytes = dataStart() + (long) capacity * blockSerializer.fixedSize();
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
    return PRELUDE + (headerSerializer != null ? headerSerializer.fixedSize() : 0);
  }

  private void check(int index) {
    if (index < 0 || index >= capacity())
      throw new IndexOutOfBoundsException(index + " of " + capacity());
  }

}
