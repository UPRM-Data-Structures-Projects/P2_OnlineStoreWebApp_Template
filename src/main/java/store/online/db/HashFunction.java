package store.online.db;

@FunctionalInterface
public interface HashFunction<K> {
  /**
   * Hashes the given key
   * 
   * @param key
   * @return Hashed key as integer
   */
  int hashCode(K key);
}