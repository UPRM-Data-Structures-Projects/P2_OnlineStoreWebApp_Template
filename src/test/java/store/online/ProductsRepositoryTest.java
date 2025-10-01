package store.online;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import store.online.entities.Schema;
import store.online.entities.Schema.Product;
import store.online.repository.ProductsRepository;
import store.online.service.ProductsService;
import store.online.utils.list.List;

/**
 * @author Alfredo
 */
public class ProductsRepositoryTest {

  @TempDir
  Path tmp;

  private Path dbFile;
  private ProductsRepository repo;
  private ProductsService service;

  // -------- helpers --------

  private void newRepoAndService(String dbName) throws Exception {
    dbFile = tmp.resolve(dbName);
    repo = new ProductsRepository();

    // Reflection: point PRODUCTS_DB to the temp file
    Field f = ProductsRepository.class.getDeclaredField("PRODUCTS_DB");
    f.setAccessible(true);
    f.set(repo, dbFile);

    service = new ProductsService(repo);
  }

  private static Product p(String name, String category, float price, String currency) {
    Product pr = new Schema.Product();
    pr.name = name;
    pr.category = category;
    pr.price = price;
    pr.currency = currency;
    // id is assigned by repository.insertProduct via header().serialCount()
    return pr;
  }

  /** Count items in custom List by iteration (no assumption about size()). */
  private static int count(List<Product> xs) {
    int n = 0;
    for (Product ignored : xs)
      n++;
    return n;
  }

  private static Product first(List<Product> xs) {
    for (Product v : xs)
      return v;
    return null;
  }

  // -------- tests --------

  @Test
  void insert_and_get_roundtrip_persists_to_file() throws Exception {
    newRepoAndService("products_roundtrip.db");

    assertFalse(Files.exists(dbFile), "DB file should not exist at start");

    // Insert two products
    Product a = p("Laptop Pro 15", "Electronics", 1200.0f, "USD");
    Product b = p("Clean Code", "Books", 38.0f, "USD");

    assertTrue(repo.insertProduct(a));
    assertTrue(repo.insertProduct(b));

    // DB file should now exist and be non-empty
    assertTrue(Files.exists(dbFile), "DB file should be created");
    assertTrue(Files.size(dbFile) > 0, "DB file should not be empty after writes");

    // IDs should be assigned (sequential). Expect first id = 0 and second = 1.
    assertEquals(0, a.id);
    assertEquals(1, b.id);

    // getProduct works
    assertTrue(repo.getProduct(a.id).isPresent());
    assertEquals("Laptop Pro 15", repo.getProduct(a.id).get().name);
    assertTrue(repo.getProduct(b.id).isPresent());
    assertEquals("Clean Code", repo.getProduct(b.id).get().name);

    // getProducts returns both
    List<Product> all = repo.getProducts();
    assertEquals(2, count(all));
  }

  @Test
  void update_existing_product_writes_new_values() throws Exception {
    newRepoAndService("products_update.db");

    Product a = p("4K Monitor 27\"", "Electronics", 329.0f, "USD");
    assertTrue(repo.insertProduct(a));
    int id = a.id;

    // Change fields and update
    a.name = "4K Monitor 27\" HDR";
    a.price = 349.0f;
    assertTrue(repo.updateProduct(a));

    // Read back
    Product stored = repo.getProduct(id).orElseThrow();
    assertEquals("4K Monitor 27\" HDR", stored.name);
    assertEquals(349.0f, stored.price, 0.0001f);
    assertEquals("Electronics", stored.category);
    assertEquals("USD", stored.currency);
  }

  @Test
  void update_nonexistent_product_returns_false() throws Exception {
    newRepoAndService("products_update_missing.db");

    Product ghost = p("Ghost", "None", 0f, "USD");
    ghost.id = 9999; // never inserted
    assertFalse(repo.updateProduct(ghost));
  }

  @Test
  void delete_existing_and_missing_product() throws Exception {
    newRepoAndService("products_delete.db");

    Product a = p("Mechanical Keyboard", "Electronics", 99.0f, "USD");
    Product b = p("Board Game: Strategy", "Toys", 35.0f, "USD");
    assertTrue(repo.insertProduct(a));
    assertTrue(repo.insertProduct(b));

    // Delete existing
    assertTrue(repo.deleteProduct(a.id));
    assertTrue(repo.getProduct(a.id).isEmpty());

    // Remaining should be only b
    List<Product> all = repo.getProducts();
    assertEquals(1, count(all));
    Product only = first(all);
    assertNotNull(only);
    assertEquals(b.id, only.id);

    // Delete missing
    assertFalse(repo.deleteProduct(123456));
  }

  @Test
  void persistence_across_new_repository_instance_same_file() throws Exception {
    newRepoAndService("products_persist.db");

    Product a = p("USB-C Hub 8-in-1", "Electronics", 45.0f, "USD");
    Product b = p("Intro to Algorithms", "Books", 92.0f, "USD");
    assertTrue(repo.insertProduct(a));
    assertTrue(repo.insertProduct(b));

    // New repository instance pointing to the SAME file
    ProductsRepository repo2 = new ProductsRepository();
    Field f = ProductsRepository.class.getDeclaredField("PRODUCTS_DB");
    f.setAccessible(true);
    f.set(repo2, dbFile);
    ProductsService service2 = new ProductsService(repo2);

    // Should see 2 products
    List<Product> all = service2.getAllProducts();
    assertEquals(2, count(all));
  }

  @Test
  void service_filters_by_category_and_search_by_name() throws Exception {
    newRepoAndService("products_filters.db");

    assertTrue(repo.insertProduct(p("Wireless Headphones", "Electronics", 80.0f, "USD")));
    assertTrue(repo.insertProduct(p("Bluetooth Speaker", "Electronics", 25.0f, "USD")));
    assertTrue(repo.insertProduct(p("Clean Code", "Books", 38.0f, "USD")));
    assertTrue(repo.insertProduct(p("Coffee Table", "Furniture", 150.0f, "USD")));

    // By category
    List<Product> electronics = service.getProductsByCategory("Electronics");
    assertEquals(2, count(electronics));

    // Search by name (case-insensitive substring)
    List<Product> searchClean = service.searchByName("clean");
    assertEquals(1, count(searchClean));
    Product found = first(searchClean);
    assertNotNull(found);
    assertEquals("Clean Code", found.name);

    // Empty/blank query returns empty
    assertEquals(0, count(service.searchByName(" ")));
    assertEquals(0, count(service.searchByName(null)));
  }
}
