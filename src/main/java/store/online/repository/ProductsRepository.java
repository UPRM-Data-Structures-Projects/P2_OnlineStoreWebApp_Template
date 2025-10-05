/*
 * Disk-backed products repository using DiskHashTableLP<Integer, Product>.
 */
package store.online.repository;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import store.online.db.DBTableMap;
import store.online.entities.Schema;
import store.online.entities.Schema.Product;
import store.online.utils.list.ArrayList;
import store.online.utils.list.List;

/**
 * 
 * @author Alfredo
 */
@Repository
public class ProductsRepository {

	private final int INITIAL_BUCKETS = 4;
	private final Path PRODUCTS_DB = Paths.get("data/products.db");

	public ProductsRepository() {

	}

	private DBTableMap<Integer, Schema.Product> open() throws IOException {
		// TODO: Return a NEW DB Table with a proper EntrySerializer for Products
		// and a valid hash function
		return null; // Dummy Return
	}

	/**
	 * Get one product by id
	 * 
	 * @param id
	 * @return Optional<Product>
	 */
	public Optional<Schema.Product> getProduct(int id) {
		// TODO: Implement get Product by Id

		return Optional.empty();
	}

	/**
	 * Return all products (simple scan via keys).
	 * 
	 * @return List<Product>
	 */
	public List<Schema.Product> getProducts() {
		// TODO: Implement get Products

		return new ArrayList<>(); // Dummy return
	}

	/**
	 * Insert new product; returns false if id already exists.
	 * 
	 * @param p
	 * @return true if inserted
	 */
	public boolean insertProduct(Schema.Product p) {
		// TODO: Implement insert product
		// Hint: New products need a unique id's
		return false; // Dummy return
	}

	/**
	 * Update existing product; returns false if product does not exist.
	 * 
	 * @param p
	 * @return true if updated
	 */
	public boolean updateProduct(Schema.Product p) {
		// TODO: Implement update product
		return false; // Dummy return
	}

	/**
	 * Delete by id; returns true if something was removed.
	 * 
	 * @param id
	 * @return true if deleted
	 */
	public boolean deleteProduct(int id) {
		try (var db = open()) {
			return db.remove(id) != null;
		} catch (IOException ioe) {
			return false;
		}
	}

	/**
	 * Gets all the products of a given category
	 * 
	 * @param category
	 * @return List<Product>
	 */
	public List<Product> getProductsByCategory(String category) {
		// TODO: Implement products by category
		return new ArrayList<>(); // Dummy Return
	}

	/**
	 * Searches the products that have similar name
	 * 
	 * @param name
	 * @return List<Product>
	 */
	public List<Product> searchByName(String name) {
		// TODO: Implement search product by name
		return new ArrayList<>();
	}
}
