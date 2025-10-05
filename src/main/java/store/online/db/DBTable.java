package store.online.db;

import java.io.PrintStream;

import store.online.db.DBTableMap.TableHeader;
import store.online.utils.list.List;

/**
 * @author Alfredo
 */
public interface DBTable<K, V> {

  /**
   * Retrieves the value associated with the given key.
   *
   * @param key the key to look up
   * @return the value mapped to {@code key}, or {@code null} if not present
   */
  V get(K key);

  /**
   * Inserts or replaces the value associated with the given key.
   *
   * @param key   the key to insert or update
   * @param value the value to associate with {@code key}
   */
  void put(K key, V value);

  /**
   * Removes the mapping for the given key, if present.
   *
   * @param key the key whose mapping should be removed
   * @return the previous value associated with {@code key}, or {@code null} if
   *         none
   */
  V remove(K key);

  /**
   * Checks whether a mapping exists for the given key.
   *
   * @param key the key to test
   * @return {@code true} if a value is mapped to {@code key}; {@code false}
   *         otherwise
   */
  boolean containsKey(K key);

  /**
   * Returns a snapshot view of all keys currently stored.
   * <p>
   * The returned list's mutability and live-ness are implementation-defined.
   * </p>
   *
   * @return a list of all keys in the table (may be empty, never {@code null})
   */
  List<K> getKeys();

  /**
   * Returns a snapshot view of all values currently stored.
   * <p>
   * The returned list's mutability and live-ness are implementation-defined.
   * </p>
   *
   * @return a list of all values in the table (may be empty, never {@code null})
   */
  List<V> getValues();

  /**
   * Returns the table header (schema/metadata) descriptor.
   *
   * @return the {@link TableHeader} describing this table
   */
  TableHeader header();

  /**
   * Removes all entries from the table.
   */
  void clear();
}
