package store.online.db;

@FunctionalInterface
public interface HashFunction<K> {
  int hashCode(K key);
}