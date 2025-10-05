/*
 * 
 */
package store.online.repository;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import store.online.db.DBTableMap;
import store.online.entities.Schema.User;

/**
 * @author Alfredo
 */
@Repository
public class UserRepository {

	private final int INITIAL_BUCKETS = 4;
	private final Path USERS_DATABASE = Paths.get("data/users.db");

	public UserRepository() {

	}

	/**
	 * Open the disk-backed hash table (try-with-resources per operation).
	 * Key: fixed 32-byte UTF-8 string
	 * Val: 4-byte int (passwordHash)
	 */
	private DBTableMap<String, Integer> open() throws IOException {
		// TODO: Return a NEW DB Table with a proper EntrySerializer for user
		// and a valid hash function
		return null; // Dummy Return
	}

	/**
	 * Gets a user by a given username.
	 * 
	 * @return Optional<User>
	 */
	public Optional<User> getUser(String username) {
		// TODO: Get the user by username
		return Optional.empty(); // Dummy return
	}

	/**
	 * Creates a new user if username is not already in the table.
	 * 
	 * @return true on success, false if username exists or IO error.
	 */
	public boolean createUser(User user) {
		// TODO: Create a new user
		return false; // Dummy return
	}

	/**
	 * Deletes a user by a given username
	 * 
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteUser(String username) {
		// TODO: Delete user by username
		return false; // Dummy return
	}

	/**
	 * Updates a user's password hash
	 * 
	 * @return true if updated, false otherwise
	 */
	public boolean updatePassword(String username, int newHash) {
		// TODO: Update password by username
		return false; // Dummy return
	}
}
