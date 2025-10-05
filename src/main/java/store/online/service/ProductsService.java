/*
 * High-level product use-cases.
 */
package store.online.service;

import org.springframework.stereotype.Service;

import store.online.entities.Schema.Product;
import store.online.repository.ProductsRepository;
import store.online.utils.list.ArrayList;
import store.online.utils.list.List;

/**
 * 
 * @author Alfredo
 */
@Service
public class ProductsService {

  private final ProductsRepository productsRepo;

  public ProductsService(ProductsRepository productsRepo) {
    this.productsRepo = productsRepo;
  }

  /**
   * Return every product in the catalog (as stored).
   * 
   * @return
   */
  public List<Product> getAllProducts() {
    return productsRepo.getProducts();
  }

  /**
   * Return products filtered by category.
   * 
   * @param category
   * @return List of products
   */
  public List<Product> getProductsByCategory(String category) {
    return productsRepo.getProductsByCategory(category);
  }

  /**
   * Return products by name
   * 
   * @param q
   * @return List of products
   */
  public List<Product> searchByName(String q) {
    if (q == null || q.isBlank())
      return new ArrayList<>();
    String needle = q.toLowerCase();
    return productsRepo.searchByName(needle);
  }

  /**
   * Inserts a new product
   * 
   * @param p
   * @return true if successfull
   */
  public boolean insert(Product p) {
    return productsRepo.insertProduct(p);
  }

  /**
   * Updates a product
   * 
   * @param p
   * @return true if successfull
   */
  public boolean update(Product p) {
    return productsRepo.updateProduct(p);
  }

  /**
   * Deletes a product
   * 
   * @param id
   * @return true if successfull
   */
  public boolean delete(int id) {
    return productsRepo.deleteProduct(id);
  }
}
