package store.online.entities;

public class Schema {

  public static final class User {
    public String username; // [32 bytes]
    public int passwordHash; // [4 bytes]
  }

  public static final class Product {
    public int id; // [4 bytes] Unique ID
    public float price; // [4 bytes]
    public String name; // [64 bytes]
    public String image; // [255 bytes]
    public String category; // [32 bytes]
    public String currency; // [8 bytes] USD | EURO
  }
}
