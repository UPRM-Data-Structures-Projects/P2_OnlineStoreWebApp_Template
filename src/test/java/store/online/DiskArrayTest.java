package store.online;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import store.online.db.DiskArray;
import store.online.db.FixedSizeSerializer.*;

class DiskArrayTest {

  // ---------------- test helpers ----------------
  private Path tmp;

  private Path temp(String prefix) throws IOException {
    tmp = Files.createTempFile(prefix, ".mmap");
    return tmp;
  }

  @AfterEach
  void cleanup() throws IOException {
    if (tmp != null)
      Files.deleteIfExists(tmp);
  }

  // -------- header model & codec for tests -------
  static final class MyHeader {
    int version;
    int dirty;

    MyHeader() {
    }

    MyHeader(int v, int d) {
      this.version = v;
      this.dirty = d;
    }
  }

  static final class MyHeaderCodec implements FixedElementSerializer<MyHeader> {
    @Override
    public int fixedSize() {
      return 8;
    } // two ints

    @Override
    public void write(MappedByteBuffer buf, int pos, MyHeader h) {
      if (h == null)
        h = new MyHeader();
      buf.putInt(pos, h.version);
      buf.putInt(pos + 4, h.dirty);
    }

    @Override
    public MyHeader read(MappedByteBuffer buf, int pos) {
      MyHeader h = new MyHeader();
      h.version = buf.getInt(pos);
      h.dirty = buf.getInt(pos + 4);
      return h;
    }
  }

  // shorthand codecs
  private static final IntSerializer I32 = new IntSerializer();
  private static final StringSerializer STR16 = new StringSerializer(16);

  // -------------------- TESTS --------------------

  @Test
  void create_withHeader_capacityAndHeaderWriteRead() throws Exception {
    Path f = temp("da_header");
    try (var da = new DiskArray<MyHeader, Integer>(f, 8, new MyHeaderCodec(), I32)) {
      assertEquals(8, da.capacity());
      // header initially zeros (since initFile doesn’t write header payload)
      var h0 = da.header();
      assertEquals(0, h0.version);
      assertEquals(0, h0.dirty);

      da.setHeader(new MyHeader(1, 5));
      var h1 = da.header();
      assertEquals(1, h1.version);
      assertEquals(5, h1.dirty);
    }
  }

  @Test
  void element_set_get_withinBounds() throws Exception {
    Path f = temp("da_int_rw");
    try (var da = new DiskArray<MyHeader, Integer>(f, 4, new MyHeaderCodec(), I32)) {
      da.setHeader(new MyHeader(1, 0));
      // write
      da.set(0, 42);
      da.set(1, 99);
      da.set(3, -7);
      // read
      assertEquals(42, da.get(0));
      assertEquals(99, da.get(1));
      assertEquals(-7, da.get(3));
    }
  }

  @Test
  void bounds_check_throws() throws Exception {
    Path f = temp("da_bounds");
    try (var da = new DiskArray<MyHeader, Integer>(f, 2, new MyHeaderCodec(), I32)) {
      assertThrows(IndexOutOfBoundsException.class, () -> da.get(-1));
      assertThrows(IndexOutOfBoundsException.class, () -> da.get(2));
      assertThrows(IndexOutOfBoundsException.class, () -> da.set(-1, 0));
      assertThrows(IndexOutOfBoundsException.class, () -> da.set(2, 0));
    }
  }

  @Test
  void grow_preservesExistingData_andUpdatesCapacity() throws Exception {
    Path f = temp("da_grow");
    try (var da = new DiskArray<MyHeader, Integer>(f, 3, new MyHeaderCodec(), I32)) {
      da.set(0, 10);
      da.set(1, 20);
      da.set(2, 30);
      assertEquals(3, da.capacity());

      da.grow(6); // double
      assertEquals(6, da.capacity());

      // existing still there
      assertEquals(10, da.get(0));
      assertEquals(20, da.get(1));
      assertEquals(30, da.get(2));

      // new slots writable
      da.set(3, 40);
      da.set(4, 50);
      da.set(5, 60);
      assertEquals(40, da.get(3));
      assertEquals(50, da.get(4));
      assertEquals(60, da.get(5));
    }
  }

  @Test
  void persistence_acrossReopen_headerAndData() throws Exception {
    Path f = temp("da_reopen");
    // first open: write
    try (var da = new DiskArray<MyHeader, Integer>(f, 5, new MyHeaderCodec(), I32)) {
      da.setHeader(new MyHeader(7, 99));
      da.set(0, 111);
      da.set(4, 222);
    }
    // reopen (same initial capacity to avoid mismatch)
    try (var da2 = new DiskArray<MyHeader, Integer>(f, 5, new MyHeaderCodec(), I32)) {
      var h = da2.header();
      assertEquals(7, h.version);
      assertEquals(99, h.dirty);
      assertEquals(111, da2.get(0));
      assertEquals(222, da2.get(4));
    }
  }

  @Test
  void fixedString_truncatesAndPads() throws Exception {
    Path f = temp("da_str");
    try (var da = new DiskArray<MyHeader, String>(f, 3, new MyHeaderCodec(), STR16)) {
      da.setHeader(new MyHeader(1, 0));

      da.set(0, "hi"); // short (pads)
      da.set(1, "1234567890abcd"); // exactly 14 (fits)
      da.set(2, "abcdefghijklmnopqr"); // 18 chars → truncated to 16

      assertEquals("hi", da.get(0));
      assertEquals("1234567890abcd", da.get(1));
      assertEquals("abcdefghijklmnop", da.get(2)); // truncated
    }
  }

  @Test
  void noHeaderMode_headerIsNull_andSetHeaderThrows() throws Exception {
    Path f = temp("da_nohdr");
    // pass null header codec → header length = 0
    try (var da = new DiskArray<Void, Integer>(f, 4, null, I32)) {
      assertNull(da.header());
      assertThrows(IllegalStateException.class, () -> da.setHeader(null));
      da.set(0, 1);
      assertEquals(1, da.get(0));
    }
  }

  @Test
  void elementSet_returnsPreviousValue() throws Exception {
    Path f = temp("da_prev");
    try (var da = new DiskArray<MyHeader, Integer>(f, 2, new MyHeaderCodec(), I32)) {
      Integer prev0 = da.set(0, 5); // uninitialized → codec will read default 0
      assertEquals(0, prev0);
      Integer prev1 = da.set(0, 9);
      assertEquals(5, prev1);
      assertEquals(9, da.get(0));
    }
  }

  @Test
  void largeHeader_doesNotGetOverwrittenByData() throws Exception {
    // Use a larger header (64 bytes) and verify it remains intact after element
    // writes.
    var bigHeaderCodec = new FixedElementSerializer<MyHeader>() {
      @Override
      public int fixedSize() {
        return 64;
      }

      @Override
      public void write(MappedByteBuffer buf, int pos, MyHeader h) {
        if (h == null)
          h = new MyHeader();
        buf.putInt(pos, h.version);
        buf.putInt(pos + 4, h.dirty);
        // zero-fill rest
        for (int i = 8; i < 64; i++)
          buf.put(pos + i, (byte) 0);
      }

      @Override
      public MyHeader read(MappedByteBuffer buf, int pos) {
        MyHeader h = new MyHeader();
        h.version = buf.getInt(pos);
        h.dirty = buf.getInt(pos + 4);
        return h;
      }
    };

    Path f = temp("da_bigHeader");
    try (var da = new DiskArray<MyHeader, Integer>(f, 4, bigHeaderCodec, I32)) {
      da.setHeader(new MyHeader(3, 33));
      da.set(0, 100);
      da.set(1, 200);
      var h = da.header();
      assertEquals(3, h.version);
      assertEquals(33, h.dirty);
      assertEquals(100, da.get(0));
      assertEquals(200, da.get(1));
    }
  }

  @Test
  void grow_multipleTimes_andReadBack() throws Exception {
    Path f = temp("da_grow_multi");
    try (var da = new DiskArray<MyHeader, Integer>(f, 2, new MyHeaderCodec(), I32)) {
      da.set(0, 7);
      da.set(1, 8);

      da.grow(3);
      da.set(2, 9);

      da.grow(6);
      da.set(3, 10);
      da.set(4, 11);
      da.set(5, 12);

      assertEquals(6, da.capacity());
      assertEquals(7, da.get(0));
      assertEquals(8, da.get(1));
      assertEquals(9, da.get(2));
      assertEquals(10, da.get(3));
      assertEquals(11, da.get(4));
      assertEquals(12, da.get(5));
    }
  }
}
