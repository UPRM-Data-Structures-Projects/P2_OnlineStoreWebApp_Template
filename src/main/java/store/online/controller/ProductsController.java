/*
 * Products Controller
 */
package store.online.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import store.online.entities.Schema.Product;
import store.online.service.ProductsService;
import store.online.utils.list.List;

/**
 * @author Alfredo
 */
@RestController
@RequestMapping("/api/products")
public class ProductsController {

  private final ProductsService productsService;

  public ProductsController(ProductsService productsService) {
    this.productsService = productsService;
  }

  @GetMapping
  public ResponseEntity<List<Product>> list(
      @RequestParam(value = "category", required = false) String category,
      @RequestParam(value = "q", required = false) String q) {

    if (q != null && !q.isBlank()) {
      return ResponseEntity.ok(productsService.searchByName(q));
    }
    if (category != null && !category.isBlank()) {
      return ResponseEntity.ok(productsService.getProductsByCategory(category));
    }
    return ResponseEntity.ok(productsService.getAllProducts());
  }

  @PostMapping
  public ResponseEntity<?> create(@RequestBody Product p) {
    var err = validateForCreate(p);
    if (err != null) {
      return ResponseEntity.badRequest().body(new ErrorResponse(err));
    }
    boolean ok = productsService.insert(p);
    if (!ok) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ErrorResponse("Product with same id already exists or insert failed."));
    }
    return ResponseEntity.status(HttpStatus.CREATED).body(p);
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(@PathVariable int id, @RequestBody Product p) {
    if (p == null) {
      return ResponseEntity.badRequest().body(new ErrorResponse("Body required."));
    }
    var err = validateForUpdate(p);
    if (err != null) {
      return ResponseEntity.badRequest().body(new ErrorResponse(err));
    }
    p.id = id;
    boolean ok = productsService.update(p);
    if (!ok) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ErrorResponse("Product not found or update failed."));
    }
    return ResponseEntity.ok(p);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable int id) {
    boolean ok = productsService.delete(id);
    if (!ok) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ErrorResponse("Product not found or delete failed."));
    }
    return ResponseEntity.noContent().build();
  }

  public static final class ErrorResponse {
    public String message;

    public ErrorResponse(String m) {
      this.message = m;
    }
  }

  private String validateForCreate(Product p) {
    if (p == null)
      return "Body required.";
    if (p.name == null || p.name.isBlank())
      return "name is required.";
    if (p.price < 0)
      return "price must be >= 0.";
    if (p.currency == null || p.currency.isBlank())
      return "currency is required.";
    // categoryId can be 0 if you want “uncategorized”; adjust as needed.
    return null;
  }

  private String validateForUpdate(Product p) {
    // For update, keep the same checks (you can relax as needed):
    if (p.name == null || p.name.isBlank())
      return "name is required.";
    if (p.price < 0)
      return "price must be >= 0.";
    if (p.currency == null || p.currency.isBlank())
      return "currency is required.";
    return null;
  }
}
