package store.online;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import store.online.db.DBTableMap;
import store.online.db.HashFunction;
import store.online.db.DBTableMap.EntrySerializer;
import store.online.db.FixedSizeSerializer.*;

class DBTableMapTest {

  private Path temp(String prefix) throws IOException {
    return Files.createTempFile(prefix, ".mmap");
  }

  private HashFunction<String> hf() {
    return String::hashCode;
  }

  private DBTableMap<String, Integer> newTable(Path file, int cap) throws Exception {
    var codec = new EntrySerializer<String, Integer>(new StringSerializer(32), new IntSerializer());
    return new DBTableMap<>(file, cap, codec, hf());
  }

  @Test
  void empty_hasSizeZero_andGetNull() throws Exception {
    Path f = temp("empty");
    try (var ht = newTable(f, 8)) {
      assertEquals(0, ht.header().entryCount());
      assertTrue(ht.header().entryCount() == 0);
      assertNull(ht.get("missing"));
    } finally {
      Files.deleteIfExists(f);
    }
  }

  @Test
  void put_and_get() throws Exception {
    Path f = temp("putget");
    try (var ht = newTable(f, 8)) {
      ht.put("A", 1);
      ht.put("B", 2);
      ht.put("C", 3);
      assertEquals(3, ht.header().entryCount());
      assertEquals(1, ht.get("A"));
      assertEquals(2, ht.get("B"));
      assertEquals(3, ht.get("C"));
      assertNull(ht.get("D"));
    } finally {
      Files.deleteIfExists(f);
    }
  }

  @Test
  void overwrite_same_key_updates_value_only() throws Exception {
    Path f = temp("overwrite");
    try (var ht = newTable(f, 8)) {
      ht.put("K", 10);
      int before = ht.header().entryCount();
      ht.put("K", 99); // overwrite
      assertEquals(before, ht.header().entryCount()); // size unchanged
      assertEquals(99, ht.get("K"));
    } finally {
      Files.deleteIfExists(f);
    }
  }

  @Test
  void remove_sets_tombstone_and_get_returns_null() throws Exception {
    Path f = temp("remove");
    try (var ht = newTable(f, 8)) {
      ht.put("A", 1);
      ht.put("B", 2);
      assertEquals(2, ht.header().entryCount());
      assertEquals(2, ht.remove("B"));
      assertNull(ht.get("B"));
      assertEquals(1, ht.header().entryCount());
      assertNull(ht.remove("Z")); // non-existing
      assertEquals(1, ht.header().entryCount());
    } finally {
      Files.deleteIfExists(f);
    }
  }

  @Test
  void wraparound_linear_probe() throws Exception {
    Path f = temp("wrap");
    try (var ht = newTable(f, 5)) {
      ht.put("A", 1);
      ht.put("B", 2);
      ht.put("C", 3);
      ht.put("D", 4);
      assertEquals(1, ht.get("A"));
      assertEquals(2, ht.get("B"));
      assertEquals(3, ht.get("C"));
      assertEquals(4, ht.get("D"));
    } finally {
      Files.deleteIfExists(f);
    }
  }

  @Test
  void loadFactor_rehash_preserves_entries() throws Exception {
    Path f = temp("rehash");
    try (var ht = newTable(f, 4)) { // 0.75 * 4 = 3 → 4th insert triggers rehash
      ht.put("k1", 1);
      ht.put("k2", 2);
      ht.put("k3", 3);
      ht.put("k4", 4); // should cause rehash in your code
      assertEquals(4, ht.header().entryCount());
      assertEquals(1, ht.get("k1"));
      assertEquals(2, ht.get("k2"));
      assertEquals(3, ht.get("k3"));
      assertEquals(4, ht.get("k4"));
    } finally {
      Files.deleteIfExists(f);
    }
  }

  @Test
  void clear_empties_table() throws Exception {
    Path f = temp("clear");
    try (var ht = newTable(f, 8)) {
      ht.put("a", 1);
      ht.put("b", 2);
      ht.put("c", 3);
      assertEquals(3, ht.header().entryCount());
      ht.clear();
      assertEquals(0, ht.header().entryCount());
      assertNull(ht.get("a"));
      assertNull(ht.get("b"));
      assertNull(ht.get("c"));
    } finally {
      Files.deleteIfExists(f);
    }
  }

  @Test
  void getKeys_and_getValues_only_full_slots() throws Exception {
    Path f = temp("kv");
    try (var ht = newTable(f, 8)) {
      ht.put("A", 1);
      ht.put("B", 2);
      ht.put("C", 3);
      ht.remove("B"); // tombstone

      var keys = ht.getKeys().toArray();
      var vals = ht.getValues().toArray();

      assertEquals(2, keys.length);
      assertEquals(2, vals.length);

      var klist = java.util.Arrays.asList(keys);
      var vlist = java.util.Arrays.asList(vals);
      assertTrue(klist.contains("A"));
      assertTrue(klist.contains("C"));
      assertTrue(vlist.contains(1));
      assertTrue(vlist.contains(3));
    } finally {
      Files.deleteIfExists(f);
    }
  }
}
