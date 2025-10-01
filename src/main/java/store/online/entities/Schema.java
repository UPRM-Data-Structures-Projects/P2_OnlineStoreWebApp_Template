package store.online.entities;

public class Schema {

  public static final class User {
    public String username;
    public int passwordHash;
  }

  public static final class Product {
    public int id;
    public float price;
    public String name;
    public String category;
    public String currency; // USD | EURO
  }
}
