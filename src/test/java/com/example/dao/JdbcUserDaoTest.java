package com.example.dao;

import com.example.exceptions.DatabaseWriteException;
import com.example.model.Role;
import com.example.model.User;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JdbcUserDao Comprehensive Tests")
class JdbcUserDaoTest extends BaseDaoTest {
    private JdbcUserDao userDao;
    private JdbcRoleDao roleDao;

    @BeforeEach
    void setUp() {
        userDao = new JdbcUserDao(getTestDatabaseManager());
        roleDao = new JdbcRoleDao(getTestDatabaseManager());
    }

    @Test
    @DataSet("dataset/roles.yml")
    void shouldCreateUserWithAllFields() {
        Role userRole = roleDao.findByName("USER");
        LocalDate birthday = LocalDate.parse("1990-05-15");

        User newUser = new User("johndoe", "password123", "john@example.com",
                "John", "Doe", birthday, userRole);

        userDao.create(newUser);

        assertThat(newUser.getId()).isNotNull();
        assertThat(newUser.getId()).isPositive();

        User foundUser = userDao.findByLogin("johndoe");
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getLogin()).isEqualTo("johndoe");
        assertThat(foundUser.getEmail()).isEqualTo("john@example.com");
        assertThat(foundUser.getFirstName()).isEqualTo("John");
        assertThat(foundUser.getLastName()).isEqualTo("Doe");
        assertThat(foundUser.getBirthday()).isEqualTo(birthday);
        assertThat(foundUser.getRole().getName()).isEqualTo("USER");
    }

    @Test
    void shouldCreateUserWithMinimalFields() {
        User newUser = new User("minimal", "pass", "minimal@example.com",
                null, null, null, null);

        userDao.create(newUser);

        assertThat(newUser.getId()).isNotNull();

        User foundUser = userDao.findByLogin("minimal");
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getFirstName()).isNull();
        assertThat(foundUser.getLastName()).isNull();
        assertThat(foundUser.getBirthday()).isNull();
        assertThat(foundUser.getRole()).isNull();
    }

    @Test
    void shouldCreateUserWithVeryLongNames() {
        String longName = "A".repeat(50);
        User newUser = new User("longuser", "password", "long@example.com",
                longName, longName, null, null);

        userDao.create(newUser);

        User foundUser = userDao.findByLogin("longuser");
        assertThat(foundUser.getFirstName()).isEqualTo(longName);
        assertThat(foundUser.getLastName()).isEqualTo(longName);
    }

    @Test
    @DataSet(value = {"dataset/roles.yml", "dataset/users.yml"})
    void shouldFailToCreateUserWithDuplicateLogin() {
        User duplicateUser = new User("johndoe", "password", "different@example.com",
                "Different", "User", null, null);

        assertThatThrownBy(() -> userDao.create(duplicateUser))
                .isInstanceOf(DatabaseWriteException.class)
                .hasMessageContaining("Error creating entity");
    }

    @Test
    @DataSet(value = {"dataset/roles.yml", "dataset/users.yml"})
    void shouldFailToCreateUserWithDuplicateEmail() {
        User duplicateUser = new User("differentlogin", "password", "john@example.com",
                "Different", "User", null, null);

        assertThatThrownBy(() -> userDao.create(duplicateUser))
                .isInstanceOf(DatabaseWriteException.class)
                .hasMessageContaining("Error creating entity");
    }

    @Test
    void shouldFailToCreateUserWithNullLogin() {
        User invalidUser = new User(null, "password", "test@example.com",
                "Test", "User", null, null);

        assertThatThrownBy(() -> userDao.create(invalidUser))
                .isInstanceOf(DatabaseWriteException.class)
                .hasMessageContaining("Error creating entity");
    }

    @Test
    void shouldFailToCreateUserWithNullPassword() {
        User invalidUser = new User("testuser", null, "test@example.com",
                "Test", "User", null, null);

        assertThatThrownBy(() -> userDao.create(invalidUser))
                .isInstanceOf(DatabaseWriteException.class)
                .hasMessageContaining("Error creating entity");
    }

    @Test
    void shouldFailToCreateUserWithNullEmail() {
        User invalidUser = new User("testuser", "password", null,
                "Test", "User", null, null);

        assertThatThrownBy(() -> userDao.create(invalidUser))
                .isInstanceOf(DatabaseWriteException.class)
                .hasMessageContaining("Error creating entity");
    }

    @Test
    void shouldFailToCreateNullUser() {
        assertThatThrownBy(() -> userDao.create(null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DataSet(value = {"dataset/roles.yml", "dataset/users.yml"})
    void shouldFindUserByValidIdWithRole() {
        List<User> allUsers = userDao.findAll();
        User firstUser = allUsers.get(0);

        User foundUser = userDao.findById(firstUser.getId());

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(firstUser.getId());
        assertThat(foundUser.getLogin()).isEqualTo(firstUser.getLogin());
        assertThat(foundUser.getRole()).isNotNull();
        assertThat(foundUser.getRole().getName()).isNotNull();
    }

    @Test
    void shouldReturnNullForNonExistentUserId() {
        User user = userDao.findById(999L);
        assertThat(user).isNull();
    }

    @Test
    void shouldReturnNullForNullUserId() {
        User user = userDao.findById(null);
        assertThat(user).isNull();
    }



    @ParameterizedTest
    @DataSet(value = {"dataset/roles.yml", "dataset/users.yml"})
    @DisplayName("Should handle case sensitivity when finding by login")
    @CsvSource({
            "johndoe, true, 'Should find exact match'",
            "JOHNDOE, false, 'Should be case sensitive - uppercase should not match'",
            "JohnDoe, false, 'Should be case sensitive - mixed case should not match'"
    })
    void shouldHandleCaseSensitivityForLogin(String searchLogin, boolean shouldFind) {
        User user = userDao.findByLogin(searchLogin);

        if (shouldFind) {
            assertThat(user).isNotNull();
            assertThat(user.getLogin()).isEqualTo(searchLogin);
        } else {
            assertThat(user).isNull();
        }
    }

    @ParameterizedTest
    @DisplayName("Should return null for invalid login values")
    @NullAndEmptySource
    @ValueSource(strings = {"nonexistent", "   ", "\t"})
    void shouldReturnNullForInvalidLogins(String invalidLogin) {
        User user = userDao.findByLogin(invalidLogin);
        assertThat(user).isNull();
    }

    @ParameterizedTest
    @DataSet(value = {"dataset/roles.yml", "dataset/users.yml"})
    @DisplayName("Should find users with exact email match")
    @CsvSource({
            "jane@example.com, janesmith",
            "john@example.com, johndoe"
    })
    void shouldFindUserByExactEmail(String email, String expectedLogin) {
        User user = userDao.findByEmail(email);

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getLogin()).isEqualTo(expectedLogin);
        assertThat(user.getRole()).isNotNull();
    }

    @ParameterizedTest
    @DataSet(value = {"dataset/roles.yml", "dataset/users.yml"})
    @DisplayName("Should not find users with wrong case email")
    @ValueSource(strings = {
            "JANE@EXAMPLE.COM",
            "JOHN@EXAMPLE.COM",
            "Jane@Example.Com",
            "John@Example.Com"
    })
    void shouldBeCaseSensitiveWhenFindingByEmail(String wrongCaseEmail) {
        User user = userDao.findByEmail(wrongCaseEmail);
        assertThat(user).isNull();
    }

    @ParameterizedTest
    @DisplayName("Should return null for invalid email values")
    @NullSource
    @ValueSource(strings = {"nonexistent@example.com", "", "   "})
    void shouldReturnNullForInvalidEmails(String invalidEmail) {
        User user = userDao.findByEmail(invalidEmail);
        assertThat(user).isNull();
    }

    @Test
    @DataSet(value = {"dataset/roles.yml", "dataset/users.yml"})
    void shouldFindAllUsersOrderedByLogin() {
        List<User> users = userDao.findAll();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getLogin)
                .containsExactly("janesmith", "johndoe");

        users.forEach(user -> {
            assertThat(user.getId()).isNotNull();
            assertThat(user.getLogin()).isNotNull();
            assertThat(user.getEmail()).isNotNull();
            assertThat(user.getRole()).isNotNull();
            assertThat(user.getRole().getName()).isNotNull();
        });
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersExist() {
        List<User> users = userDao.findAll();

        assertThat(users).isNotNull();
        assertThat(users).isEmpty();
    }

    @Test
    @DataSet(value = {"dataset/roles.yml", "dataset/users.yml"})
    void shouldUpdateUserWithAllFields() {
        User user = userDao.findByLogin("johndoe");
        Role adminRole = roleDao.findByName("ADMIN");
        LocalDate newBirthday = LocalDate.parse("1985-12-25");

        user.setLogin("johnsmith");
        user.setEmail("johnsmith@example.com");
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setBirthday(newBirthday);
        user.setRole(adminRole);

        userDao.update(user);

        User updatedUser = userDao.findById(user.getId());
        assertThat(updatedUser.getLogin()).isEqualTo("johnsmith");
        assertThat(updatedUser.getEmail()).isEqualTo("johnsmith@example.com");
        assertThat(updatedUser.getFirstName()).isEqualTo("John");
        assertThat(updatedUser.getLastName()).isEqualTo("Smith");
        assertThat(updatedUser.getBirthday()).isEqualTo(newBirthday);
        assertThat(updatedUser.getRole().getName()).isEqualTo("ADMIN");

        assertThat(userDao.findByLogin("johndoe")).isNull();
    }

    @Test
    @DataSet(value = {"dataset/roles.yml", "dataset/users.yml"})
    void shouldUpdateUserToRemoveOptionalFields() {
        User user = userDao.findByLogin("johndoe");

        user.setFirstName(null);
        user.setLastName(null);
        user.setBirthday(null);
        user.setRole(null);

        userDao.update(user);

        User updatedUser = userDao.findById(user.getId());
        assertThat(updatedUser.getFirstName()).isNull();
        assertThat(updatedUser.getLastName()).isNull();
        assertThat(updatedUser.getBirthday()).isNull();
        assertThat(updatedUser.getRole()).isNull();
    }

    @Test
    @DataSet(value = {"dataset/roles.yml", "dataset/users.yml"})
    void shouldFailToUpdateUserWithDuplicateLogin() {
        User user1 = userDao.findByLogin("johndoe");
        user1.setLogin("janesmith");

        assertThatThrownBy(() -> userDao.update(user1))
                .isInstanceOf(DatabaseWriteException.class)
                .hasMessageContaining("Error updating entity");
    }


    @Test
    @DataSet(value = {"dataset/roles.yml", "dataset/users.yml"})
    void shouldFailToUpdateUserWithDuplicateEmail() {
        User user1 = userDao.findByLogin("johndoe");
        user1.setEmail("jane@example.com");

        assertThatThrownBy(() -> userDao.update(user1))
                .isInstanceOf(DatabaseWriteException.class)
                .hasMessageContaining("Error updating entity");
    }

    @Test
    void shouldFailToUpdateNonExistentUser() {
        User nonExistentUser = new User("ghost", "password", "ghost@example.com",
                "Ghost", "User", null, null);
        nonExistentUser.setId(999L);

        assertThatThrownBy(() -> userDao.update(nonExistentUser))
                .isInstanceOf(DatabaseWriteException.class)
                .hasMessageContaining("Error updating entity");
    }

    @Test
    void shouldFailToUpdateUserWithNullRequiredFields() {
        User user = new User("temp", "password", "temp@example.com",
                null, null, null, null);
        userDao.create(user);

        user.setLogin(null);

        assertThatThrownBy(() -> userDao.update(user))
                .isInstanceOf(DatabaseWriteException.class)
                .hasMessageContaining("Error updating entity");
    }

    @Test
    void shouldFailToUpdateNullUser() {
        assertThatThrownBy(() -> userDao.update(null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DataSet(value = {"dataset/roles.yml", "dataset/users.yml"})
    void shouldDeleteExistingUser() {
        User user = userDao.findByLogin("johndoe");
        assertThat(user).isNotNull();

        userDao.remove(user);

        User deletedUser = userDao.findByLogin("johndoe");
        assertThat(deletedUser).isNull();

        User deletedById = userDao.findById(user.getId());
        assertThat(deletedById).isNull();

        assertThat(userDao.findAll()).hasSize(1);
    }

    @Test
    void shouldFailToDeleteNonExistentUser() {
        User nonExistentUser = new User("ghost", "password", "ghost@example.com",
                "Ghost", "User", null, null);
        nonExistentUser.setId(999L);

        assertThatThrownBy(() -> userDao.remove(nonExistentUser))
                .isInstanceOf(DatabaseWriteException.class)
                .hasMessageContaining("Error deleting entity");
    }

    @Test
    void shouldFailToDeleteNullUser() {
        assertThatThrownBy(() -> userDao.remove(null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldFailToDeleteUserWithNullId() {
        User user = new User("temp", "password", "temp@example.com",
                null, null, null, null);
        user.setId(null);

        assertThatThrownBy(() -> userDao.remove(user))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DataSet("dataset/roles.yml")
    void shouldHandleVariousDateFormats() {
        Role userRole = roleDao.findByName("USER");

        LocalDate[] testDates = {
                LocalDate.parse("1900-01-01"),
                LocalDate.parse("2000-02-29"),
                LocalDate.parse("2023-12-31"),
                LocalDate.now()
        };

        for (int i = 0; i < testDates.length; i++) {
            User user = new User("datetest" + i, "password", "date" + i + "@example.com",
                    "Date", "Test", testDates[i], userRole);
            userDao.create(user);

            User foundUser = userDao.findByLogin("datetest" + i);
            assertThat(foundUser.getBirthday()).isEqualTo(testDates[i]);
        }
    }

    @Test
    @DataSet("dataset/roles.yml")
    void shouldHandleNullBirthday() {
        Role userRole = roleDao.findByName("USER");
        User user = new User("nullbirthday", "password", "null@example.com",
                "Null", "Birthday", null, userRole);

        userDao.create(user);

        User foundUser = userDao.findByLogin("nullbirthday");
        assertThat(foundUser.getBirthday()).isNull();
    }

    @Test
    @DataSet("dataset/roles.yml")
    void shouldCreateUserWithDifferentRoles() {
        Role adminRole = roleDao.findByName("ADMIN");
        Role userRole = roleDao.findByName("USER");
        Role moderatorRole = roleDao.findByName("MODERATOR");

        User admin = new User("admin", "pass", "admin@example.com",
                "Admin", "User", null, adminRole);
        User regularUser = new User("user", "pass", "user@example.com",
                "Regular", "User", null, userRole);
        User moderator = new User("mod", "pass", "mod@example.com",
                "Mod", "User", null, moderatorRole);

        userDao.create(admin);
        userDao.create(regularUser);
        userDao.create(moderator);

        assertThat(userDao.findByLogin("admin").getRole().getName()).isEqualTo("ADMIN");
        assertThat(userDao.findByLogin("user").getRole().getName()).isEqualTo("USER");
        assertThat(userDao.findByLogin("mod").getRole().getName()).isEqualTo("MODERATOR");
    }

    @Test
    void shouldHandleUserWithNullRole() {
        User user = new User("norole", "password", "norole@example.com",
                "No", "Role", null, null);

        userDao.create(user);

        User foundUser = userDao.findByLogin("norole");
        assertThat(foundUser.getRole()).isNull();
    }

    @Test
    @DataSet("dataset/roles.yml")
    void shouldUpdateUserRole() {
        Role userRole = roleDao.findByName("USER");
        Role adminRole = roleDao.findByName("ADMIN");

        User user = new User("rolechange", "password", "change@example.com",
                "Role", "Change", null, userRole);
        userDao.create(user);

        user.setRole(adminRole);
        userDao.update(user);

        User updatedUser = userDao.findByLogin("rolechange");
        assertThat(updatedUser.getRole().getName()).isEqualTo("ADMIN");
    }

    @Test
    void shouldHandleMaximumLengthStrings() {
        String maxLogin = "a".repeat(50);

        String maxEmail = "a".repeat(91) + "@test.com";
        String maxName = "a".repeat(50);

        User user = new User(maxLogin, "password", maxEmail,
                maxName, maxName, null, null);

        userDao.create(user);

        User foundUser = userDao.findByLogin(maxLogin);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getLogin()).hasSize(50);
        assertThat(foundUser.getEmail()).hasSize(100);
        assertThat(foundUser.getFirstName()).hasSize(50);
        assertThat(foundUser.getLastName()).hasSize(50);
    }

    @Test
    void shouldFailWithStringsExceedingMaximumLength() {
        String tooLongLogin = "a".repeat(51);
        User user = new User(tooLongLogin, "password", "test@example.com",
                "Test", "User", null, null);

        assertThatThrownBy(() -> userDao.create(user))
                .isInstanceOf(DatabaseWriteException.class)
                .hasMessageContaining("Error creating entity");
    }

    @Test
    void shouldRollbackTransactionOnCreateFailure() {
        User validUser = new User("valid", "password", "valid@example.com",
                "Valid", "User", null, null);
        userDao.create(validUser);

        int initialCount = userDao.findAll().size();

        User invalidUser = new User("valid", "password", "different@example.com",
                "Invalid", "User", null, null);

        assertThatThrownBy(() -> userDao.create(invalidUser))
                .isInstanceOf(DatabaseWriteException.class);

        assertThat(userDao.findAll()).hasSize(initialCount);
    }

    @Test
    @DataSet("dataset/roles.yml")
    void shouldHandleMultipleSequentialOperations() {
        Role userRole = roleDao.findByName("USER");

        User user1 = new User("test1", "pass1", "test1@example.com",
                "Test", "One", null, userRole);
        userDao.create(user1);

        User user2 = new User("test2", "pass2", "test2@example.com",
                "Test", "Two", null, userRole);
        userDao.create(user2);

        assertThat(userDao.findAll()).hasSize(2);

        user1.setFirstName("Updated");
        userDao.update(user1);
        assertThat(userDao.findByLogin("test1").getFirstName()).isEqualTo("Updated");

        userDao.remove(user2);
        assertThat(userDao.findAll()).hasSize(1);
        assertThat(userDao.findByLogin("test2")).isNull();
    }

    @Test
    @DataSet("dataset/roles.yml")
    void shouldHandleCreationOfManyUsers() {
        Role userRole = roleDao.findByName("USER");
        int numberOfUsers = 50;

        for (int i = 0; i < numberOfUsers; i++) {
            User user = new User("user" + i, "password", "user" + i + "@example.com",
                    "User", String.valueOf(i), null, userRole);
            userDao.create(user);
            assertThat(user.getId()).isNotNull();
        }

        List<User> allUsers = userDao.findAll();
        assertThat(allUsers).hasSize(numberOfUsers);
    }

    @Test
    void shouldHandleSpecialCharactersInUserData() {
        String specialLogin = "user_with-special.chars123";
        String specialEmail = "special+email@test-domain.co.uk";
        String specialName = "José-María O'Connor";

        User user = new User(specialLogin, "password", specialEmail,
                specialName, specialName, null, null);

        userDao.create(user);

        User foundUser = userDao.findByLogin(specialLogin);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo(specialEmail);
        assertThat(foundUser.getFirstName()).isEqualTo(specialName);
    }

    @Test
    @DataSet(value = {"dataset/roles.yml", "dataset/users.yml"})
    void shouldMaintainDataIntegrityAcrossOperations() {
        List<User> initialUsers = userDao.findAll();
        int initialCount = initialUsers.size();

        User user = userDao.findByLogin("johndoe");
        String originalEmail = user.getEmail();

        user.setEmail("newemail@example.com");
        userDao.update(user);

        User updatedUser = userDao.findByLogin("johndoe");
        assertThat(updatedUser.getEmail()).isEqualTo("newemail@example.com");

        assertThat(userDao.findAll()).hasSize(initialCount);

        assertThat(updatedUser.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(updatedUser.getLastName()).isEqualTo(user.getLastName());

        assertThat(userDao.findByEmail(originalEmail)).isNull();

        assertThat(userDao.findByEmail("newemail@example.com")).isNotNull();
    }

    @Test
    void shouldHandleRapidSuccessiveOperations() {
        User user = new User("rapid", "password", "rapid@example.com",
                "Rapid", "Test", null, null);

        userDao.create(user);
        assertThat(userDao.findByLogin("rapid")).isNotNull();

        user.setFirstName("Modified");
        userDao.update(user);
        assertThat(userDao.findByLogin("rapid").getFirstName()).isEqualTo("Modified");

        userDao.remove(user);
        assertThat(userDao.findByLogin("rapid")).isNull();
    }
}