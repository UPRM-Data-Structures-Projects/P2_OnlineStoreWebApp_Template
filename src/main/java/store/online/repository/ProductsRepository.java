/*
 * Disk-backed products repository using DiskHashTableLP<Integer, Product>.
 */
package store.online.repository;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import store.online.db.DiskArray;
import store.online.db.DBTableMap;
import store.online.entities.Schema;
import store.online.utils.list.ArrayList;
import store.online.utils.list.List;

/**
 * 
 * @author Alfredo
 */
@Repository
public class ProductsRepository {

	private final int INITIAL_BUCKETS = 16;
	private final Path PRODUCTS_DB = Paths.get("data/products.db");

	public ProductsRepository() {

	}

	/** Fixed-size codec for Schema.Product. */
	public static final class ProductCodec implements DiskArray.FixedCodec<Schema.Product> {
		private static final int I32 = 4;
		private static final int NAME_BYTES = 64;
		private static final int CATG_BYTES = 32;
		private static final int CURR_BYTES = 8;

		private final DiskArray.FixedStringCodec NAME64 = new DiskArray.FixedStringCodec(NAME_BYTES);
		private final DiskArray.FixedStringCodec CATG32 = new DiskArray.FixedStringCodec(CATG_BYTES);
		private final DiskArray.FixedStringCodec CURR8 = new DiskArray.FixedStringCodec(CURR_BYTES);

		@Override
		public int fixedSize() {
			// TODO: Return total product size in bytes

			return -1; // Dummy return
		}

		@Override
		public void write(MappedByteBuffer buf, int pos, Schema.Product p) {
			if (p == null)
				p = new Schema.Product();

			// Store productId [INT]
			// Store category  [STR32]
			// Store price     [Float] HINT: Type pun Float to Int
			// Store name      [STR64]
			// Store currency  [Byte]
		}

		@Override
		public Schema.Product read(MappedByteBuffer buf, int pos) {
			Schema.Product p = new Schema.Product();

			// Read productId [INT]
			// Read category  [STR32]
			// Read price     [Int] HINT: Type pun Int to Float
			// Read name      [STR64]
			// Read currency  [Byte]
			return p;
		}
	}

	private DBTableMap<Integer, Schema.Product> open() throws IOException {
		// TODO: Return a NEW DB Table with a proper EntryCodec and a valid hash function
		return new DBTableMap<>(PRODUCTS_DB, INITIAL_BUCKETS, null, null);
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

		// Hint: New products need unique id's (serialCount) 
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

		return false; //Dummy return
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
}
