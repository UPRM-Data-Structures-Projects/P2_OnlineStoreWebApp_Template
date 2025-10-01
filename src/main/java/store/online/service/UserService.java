/*
 * 
 */
package store.online.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import store.online.db.HashFunction;
import store.online.entities.Schema.User;
import store.online.repository.UserRepository;
import store.online.utils.exceptions.UserFailException;

/**
 * @author Alfredo
 */
@Service
public class UserService {

	private final UserRepository repository;

	private static final HashFunction<char[]> passwordHashFunction = (key) -> {
		int hash = 7;
		for (char c : key) {
			hash = 31 * hash + c;
		}
		return hash;
	};

	public UserService(UserRepository repository) {
		this.repository = repository;
	}

	public Optional<User> login(String username, char[] password) {
		// Hash password
		final int hashedPassword = passwordHashFunction.hashCode(password);

		// Get user by username and hashed password
		Optional<User> userOpt = repository.getUser(username);

		// Check if the password matches
		userOpt.ifPresentOrElse(user -> {
			if (user.passwordHash != hashedPassword) {
				throw new UserFailException("Incorrect password");
			}
		}, () -> {
			throw new UserFailException("User does not exist");
		});

		// Confirm authentication
		return userOpt;
	}

	public Optional<User> signup(String username, char[] password) {
		// Create a new hashed password
		final int hashedPassword = passwordHashFunction.hashCode(password);

		// Get user by username and hashed password
		Optional<User> userOpt = repository.getUser(username);

		if (userOpt.isPresent()) {
			throw new UserFailException("User already exists");
		}

		// Create a new user object
		User user = new User();
		user.username = username;
		user.passwordHash = hashedPassword;

		// Create the user
		if (!repository.createUser(user)) {
			throw new UserFailException("Failed to create user");
		}

		// Return created user
		return Optional.of(user);
	}
}
