package store.online.db;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.MappedByteBuffer;
import java.nio.file.Path;

import store.online.utils.list.LinkedList;
import store.online.utils.list.List;

/**
 * 
 * @author Alfredo
 */
public class DBTableMap<K, V> implements DBTable<K, V>, AutoCloseable {

  /**
   * Basic table header record
   */
  public static final record TableHeader(int entryCount, int serialCount) {
  }

  /**
   * HashTable header 
   */
  public static final class TableHeaderCodec implements DiskArray.FixedCodec<TableHeader> {
    public int fixedSize() {
      return 4 + 4; // [entryCount: 4 bytes][serialCount: 4 bytes]
    }
    
    public void write(MappedByteBuffer buf, int pos, TableHeader header) {
      // Header mapping:
      // At position 0: entryCount [4 bytes]
      // At position 0 + 4: serialCount [4 bytes]
      buf.putInt(pos, header.entryCount);
      buf.putInt(pos + 4, header.serialCount);
    }

    public TableHeader read(MappedByteBuffer buf, int pos) {
      return new TableHeader(buf.getInt(pos), buf.getInt(pos + 4));
    }
  }

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

  public static final class EntryCodec<K, V>
      implements DiskArray.FixedCodec<DBTableMap.Entry<K, V>> {

    private final DiskArray.FixedCodec<K> keyCodec;
    private final DiskArray.FixedCodec<V> valCodec;

    public EntryCodec(DiskArray.FixedCodec<K> keyCodec, DiskArray.FixedCodec<V> valCodec) {
      if (keyCodec == null || valCodec == null)
        throw new IllegalArgumentException("Codecs cannot be null");
      this.keyCodec = keyCodec;
      this.valCodec = valCodec;
    }

    @Override
    public int fixedSize() {
      // [ state:int ][ key:fixedSize ][ value:fixedSize ]
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

  private static final int EMPTY = 0;
  private static final int FULL = 1;
  private static final int USED = 2;
  private static final double loadFactor = 0.75;
  private static final TableHeaderCodec header = new TableHeaderCodec();

  private HashFunction<K> hashFunction;
  private DiskArray<TableHeader, Entry<K, V>> entries;

  public DBTableMap(Path path,
      int initialCapacity,
      EntryCodec<K, V> entryCodec,
      HashFunction<K> hashFunction) throws IOException {
    if (initialCapacity < 1)
      throw new IllegalArgumentException("Capacity must be at least 1");
    if (hashFunction == null)
      throw new IllegalArgumentException("Hash function cannot be null");

    // Initialize and create disk array
    this.hashFunction = hashFunction;
    this.entries = new DiskArray<>(path, initialCapacity, header, entryCodec);
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
    return get(key) != null;
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

  @Override
  public void print(PrintStream out) {
    out.printf("[\n");
    for (int i = 0; i < entries.capacity(); i++) {
      Entry<K, V> entry = entries.get(i);
      if (entry.state == FULL) {
        Entry<K, V> e = entries.get(i);
        out.printf("(%s, %s),", i, String.valueOf(e.getKey()), String.valueOf(e.getValue()));
      }
    }
    out.printf("]");
  }

  private void rehash() {
    // TODO: IMPLEMENT REHASH HERE
  }
}