package store.online.db;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.MappedByteBuffer;
import java.nio.file.Path;

import store.online.db.FixedSizeSerializer.*;
import store.online.utils.list.LinkedList;
import store.online.utils.list.List;

/**
 * 
 * @author Alfredo
 */
public class DBTableMap<K, V> implements DBTable<K, V>, AutoCloseable {

  private static class Entry<K, V> {
    private K key;
    private V value;
    private int state;

    public Entry() {
      this(null, null, EMPTY);
    }

    public Entry(K key, V value, int state) {
      this.key = key;
      this.value = value;
      this.state = state;
    }

    public K getKey() {
      return key;
    }

    public V getValue() {
      return value;
    }
  }

  public static final class EntrySerializer<K, V> implements FixedElementSerializer<DBTableMap.Entry<K, V>> {

    private final FixedElementSerializer<K> keyCodec;
    private final FixedElementSerializer<V> valCodec;

    public EntrySerializer(FixedElementSerializer<K> keyCodec, FixedElementSerializer<V> valCodec) {
      if (keyCodec == null || valCodec == null)
        throw new IllegalArgumentException("Codecs cannot be null");
      this.keyCodec = keyCodec;
      this.valCodec = valCodec;
    }

    @Override
    public int fixedSize() {
      // TODO: Return serialized size of entries
      return -1; // Dummy return
    }

    @Override
    public void write(MappedByteBuffer buf, int pos, DBTableMap.Entry<K, V> e) {
      // TODO: Write onto buffer
    }

    @Override
    public DBTableMap.Entry<K, V> read(MappedByteBuffer buf, int pos) {
      // TODO: Read entry from buffer
      return new DBTableMap.Entry<>(null, null, EMPTY); // Dummy return
    }
  }

  public static final record TableHeader(int entryCount, int serialCount) {
  }

  /**
   * TODO: Implement table Header Serializer HERE
   */

  private static final int EMPTY = 0;
  private static final int FULL = 1;
  private static final int USED = 2;

  private HashFunction<K> hashFunction;
  private DiskArray<TableHeader, Entry<K, V>> entries;

  public DBTableMap(Path path,
      int initialCapacity,
      EntrySerializer<K, V> entrySerializer,
      HashFunction<K> hashFunction) throws IOException {
    if (initialCapacity < 1)
      throw new IllegalArgumentException("Capacity must be at least 1");
    if (hashFunction == null)
      throw new IllegalArgumentException("Hash function cannot be null");

    // Initialize and create disk array
    this.hashFunction = hashFunction;
    this.entries = new DiskArray<>(path, initialCapacity, null /* header */, entrySerializer);
  }

  @Override
  public V get(K key) {
    // TODO: IMPLEMENT GET HERE
    return null; // Dummy return
  }

  @Override
  public void put(K key, V value) {
    // TODO: IMPLEMENT PUT HERE
  }

  @Override
  public V remove(K key) {
    // TODO: IMPLEMENT REMOVE HERE
    return null; // Dummy return
  }

  @Override
  public boolean containsKey(K key) {
    // TODO: IMPLEMENT CONTAINS KEY
    return false; // Dummy return
  }

  @Override
  public List<K> getKeys() {
    // TODO: IMPLEMENT GET_KEYS HERE
    return new LinkedList<K>(); // Dummy Return
  }

  @Override
  public List<V> getValues() {
    // TODO: IMPLEMENT GET_KEYS HERE
    return new LinkedList<V>(); // Dummy Return
  }

  @Override
  public TableHeader header() {
    return entries.header();
  }

  @Override
  public void clear() {
    // TODO: IMPLEMENT CLEAR HERE
  }

  /**
   * Closes the underlying channel/mapping resources.
   *
   * @throws IOException if closing the backing file/channel fails
   */
  @Override
  public void close() throws IOException {
    entries.close();
  }
}