/*
 * Authentication Controller
 */
package store.online.controller;

import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import store.online.entities.Schema.User;
import store.online.service.UserService;
import store.online.utils.exceptions.UserFailException;

@RestController
@RequestMapping("/api/users")
public class UsersController {

	private final UserService userService;

	public UsersController(UserService userService) {
		this.userService = userService;
	}

	public static final class LoginRequest {
		public String username;
		public String password;
	}

	public static final class SignupRequest {
		public String username;
		public String password;
	}

	/** What we return to clients (no password hash!) */
	public static final class UserResponse {
		public String username;

		public UserResponse(User u) {
			this.username = u.username;
		}
	}

	public static final class ErrorResponse {
		public String message;

		public ErrorResponse(String m) {
			this.message = m;
		}
	}

	// -------- Endpoints --------

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest req) {
		char[] pwd = req.password.toCharArray();
		try {
			User u = userService.login(req.username, pwd).orElseThrow();
			return ResponseEntity.ok(new UserResponse(u));
		} catch (UserFailException e) {
			// wrong password or user not found → 401
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
		} finally {
			Arrays.fill(pwd, '\0'); // scrub
		}
	}

	@PostMapping("/signup")
	public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
		char[] pwd = req.password.toCharArray();
		try {
			User u = userService.signup(req.username, pwd).orElseThrow();
			return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponse(u));
		} catch (UserFailException e) {
			// user already exists or creation failed → 400
			return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
		} finally {
			Arrays.fill(pwd, '\0'); // scrub
		}
	}

	// -------- Optional centralized error mapping --------
	@ExceptionHandler
	public ResponseEntity<ErrorResponse> onOtherErrors(Exception e) {
		// Fallback for unexpected errors
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponse("Internal error"));
	}
}
