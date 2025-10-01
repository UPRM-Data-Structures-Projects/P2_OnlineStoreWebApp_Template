package store.online;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import store.online.entities.Schema.User;
import store.online.repository.UserRepository;

/**
 * @author Alfredo
 */
class UserRepositoryTest {

  @TempDir
  Path tmp;

  private Path dbFile;
  private UserRepository repo;

  /**
   * Build a fresh repository instance pointing to a unique file under @TempDir.
   */
  private void newRepo(String dbName) throws Exception {
    dbFile = tmp.resolve(dbName);
    Files.createDirectories(dbFile.getParent());

    repo = new UserRepository();
    Field f = UserRepository.class.getDeclaredField("USERS_DATABASE");
    f.setAccessible(true);
    f.set(repo, dbFile);
  }

  /** Same hash as UserService/UserRepository uses for passwords. */
  private static int hash(char[] key) {
    int h = 7;
    for (char c : key)
      h = 31 * h + c;
    return h;
  }

  private static User user(String username, int passwordHash) {
    User u = new User();
    u.username = username;
    u.passwordHash = passwordHash;
    return u;
  }

  @Test
  void createUser_then_getUser_roundtrip_persists_to_file() throws Exception {
    newRepo("users_repo_roundtrip.db");

    assertFalse(Files.exists(dbFile), "DB file should not exist before first write");

    User u = user("alice", hash("secret123".toCharArray()));
    assertTrue(repo.createUser(u), "createUser should succeed for new user");

    // file created and non-empty
    assertTrue(Files.exists(dbFile), "DB file should be created");
    assertTrue(Files.size(dbFile) > 0, "DB file should be non-empty after write");

    Optional<User> got = repo.getUser("alice");
    assertTrue(got.isPresent());
    assertEquals("alice", got.get().username);
    assertEquals(u.passwordHash, got.get().passwordHash);
  }

  @Test
  void createUser_duplicate_returns_false_and_does_not_overwrite() throws Exception {
    newRepo("users_repo_duplicate.db");

    int firstHash = hash("first".toCharArray());
    int secondHash = hash("second".toCharArray());

    assertTrue(repo.createUser(user("bob", firstHash)));
    assertFalse(repo.createUser(user("bob", secondHash)), "duplicate create should return false");

    // Ensure the stored hash is still the first one
    User stored = repo.getUser("bob").orElseThrow();
    assertEquals(firstHash, stored.passwordHash);
  }

  @Test
  void updatePassword_existing_user_updates_hash() throws Exception {
    newRepo("users_repo_update.db");

    int oldHash = hash("old".toCharArray());
    int newHash = hash("new".toCharArray());

    assertTrue(repo.createUser(user("carol", oldHash)));
    assertTrue(repo.updatePassword("carol", newHash));

    User stored = repo.getUser("carol").orElseThrow();
    assertEquals(newHash, stored.passwordHash);
  }

  @Test
  void updatePassword_missing_user_returns_false() throws Exception {
    newRepo("users_repo_update_missing.db");

    assertFalse(repo.updatePassword("nobody", 12345));
    assertTrue(repo.getUser("nobody").isEmpty());
  }

  @Test
  void deleteUser_existing_then_missing_behaves_as_expected() throws Exception {
    newRepo("users_repo_delete.db");

    assertTrue(repo.createUser(user("dave", hash("x".toCharArray()))));

    assertTrue(repo.deleteUser("dave"), "delete should return true for existing user");
    assertTrue(repo.getUser("dave").isEmpty(), "user should be gone after delete");

    assertFalse(repo.deleteUser("dave"), "deleting again should return false");
  }

  @Test
  void getUser_null_or_blank_returns_empty() throws Exception {
    newRepo("users_repo_blank.db");

    assertTrue(repo.getUser(null).isEmpty());
    assertTrue(repo.getUser("").isEmpty());
    assertTrue(repo.getUser("   ").isEmpty());
  }

  @Test
  void persistence_across_new_repository_instance_same_file() throws Exception {
    newRepo("users_repo_persist.db");

    assertTrue(repo.createUser(user("erin", hash("pw".toCharArray()))));

    // New repository pointing to the SAME file
    UserRepository repo2 = new UserRepository();
    Field f = UserRepository.class.getDeclaredField("USERS_DATABASE");
    f.setAccessible(true);
    f.set(repo2, dbFile);

    Optional<User> got = repo2.getUser("erin");
    assertTrue(got.isPresent());
    assertEquals("erin", got.get().username);
  }

  @AfterEach
  void afterEach() throws Exception {
    // @TempDir cleans up automatically. If you want explicit deletion:
    // if (dbFile != null && Files.exists(dbFile)) Files.delete(dbFile);
  }
}
