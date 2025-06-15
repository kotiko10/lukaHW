package com.example.dao;

import com.example.exceptions.DatabaseWriteException;
import com.example.model.Role;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JdbcRoleDao Comprehensive Tests")
class JdbcRoleDaoTest extends BaseDaoTest {

    private JdbcRoleDao roleDao;

    @BeforeEach
    void setUp() {
        roleDao = new JdbcRoleDao(getTestDatabaseManager());
    }

    @Test
    void shouldCreateRoleWithValidName() {
        Role newRole = new Role("GUEST");

        roleDao.create(newRole);

        assertThat(newRole.getId()).isNotNull();
        assertThat(newRole.getId()).isPositive();

        Role foundRole = roleDao.findByName("GUEST");
        assertThat(foundRole).isNotNull();
        assertThat(foundRole.getName()).isEqualTo("GUEST");
        assertThat(foundRole.getId()).isEqualTo(newRole.getId());
    }
    @Test
    void shouldCreateRoleWithLongName() {
        String longName = "A".repeat(50);
        Role newRole = new Role(longName);


        roleDao.create(newRole);

        assertThat(newRole.getId()).isNotNull();
        Role foundRole = roleDao.findByName(longName);
        assertThat(foundRole.getName()).isEqualTo(longName);
    }

    @Test
    void shouldFailToCreateRoleWithNullName() {
        Role newRole = new Role(null);

        assertThatThrownBy(() -> roleDao.create(newRole))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error creating entity");
    }

    @Test
    void shouldFailToCreateRoleWithEmptyName() {
        Role newRole = new Role("");

        roleDao.create(newRole);

        Role foundRole = roleDao.findByName("");
        assertThat(foundRole).isNotNull();
        assertThat(foundRole.getName()).isEmpty();
    }

    @Test
    @DataSet("dataset/roles.yml")
    void shouldFailToCreateDuplicateRoleName() {
        Role duplicateRole = new Role("ADMIN");

        assertThatThrownBy(() -> roleDao.create(duplicateRole))
                .isInstanceOf(DatabaseWriteException.class)
                .hasMessageContaining("Error creating entity");
    }

    @Test
    void shouldFailToCreateRoleWithTooLongName() {
        String tooLongName = "A".repeat(51);
        Role newRole = new Role(tooLongName);

        assertThatThrownBy(() -> roleDao.create(newRole))
                .isInstanceOf(DatabaseWriteException.class)
                .hasMessageContaining("Error creating entity");
    }

    @Test
    void shouldFailToCreateNullRole() {
        assertThatThrownBy(() -> roleDao.create(null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DataSet("dataset/roles.yml")
    void shouldFindRoleByValidId() {
        List<Role> allRoles = roleDao.findAll();
        Role firstRole = allRoles.get(0);

        Role foundRole = roleDao.findById(firstRole.getId());

        assertThat(foundRole).isNotNull();
        assertThat(foundRole.getId()).isEqualTo(firstRole.getId());
        assertThat(foundRole.getName()).isEqualTo(firstRole.getName());
    }

    @ParameterizedTest
    @DisplayName("Should return null for invalid role IDs")
    @NullSource
    @ValueSource(longs = {999L, -1L, 0L})
    void shouldReturnNullForInvalidRoleIds(Long invalidId) {
        Role role = roleDao.findById(invalidId);
        assertThat(role).isNull();
    }

    @ParameterizedTest
    @DataSet("dataset/roles.yml")
    @CsvSource({
            "ADMIN, true",
            "admin, false",
            "Admin, false",
            "MODERATOR, true"
    })
    void shouldHandleCaseSensitivityWhenFindingByName(String searchName, boolean shouldFind) {
        Role role = roleDao.findByName(searchName);
        if (shouldFind) {
            assertThat(role).isNotNull();
            assertThat(role.getName()).isEqualTo(searchName);
            assertThat(role.getId()).isNotNull();
        } else {
            assertThat(role).isNull();
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"NONEXISTENT", "   ", "\t", "\n"})
    void shouldReturnNullForInvalidRoleNames(String invalidName) {
        Role role = roleDao.findByName(invalidName);
        assertThat(role).isNull();
    }

    @Test
    @DataSet("dataset/roles.yml")
    void shouldFindAllRolesOrderedByName() {
        List<Role> roles = roleDao.findAll();

        assertThat(roles).hasSize(3);
        assertThat(roles).extracting(Role::getName)
                .containsExactly("ADMIN", "MODERATOR", "USER");

        roles.forEach(role -> {
            assertThat(role.getId()).isNotNull();
            assertThat(role.getName()).isNotNull();
        });
    }

    @Test
    void shouldReturnEmptyListWhenNoRolesExist() {
        List<Role> roles = roleDao.findAll();

        assertThat(roles).isNotNull().isEmpty();
    }

    @Test
    @DataSet("dataset/roles.yml")
    void shouldReturnIndependentListInstances() {
        List<Role> roles1 = roleDao.findAll();
        List<Role> roles2 = roleDao.findAll();

        assertThat(roles1).isNotSameAs(roles2);
        assertThat(roles1).hasSize(roles2.size());
    }

    @Test
    @DataSet("dataset/roles.yml")
    void shouldUpdateRoleName() {
        Role role = roleDao.findByName("USER");
        String originalName = role.getName();
        role.setName("BASIC_USER");

        roleDao.update(role);

        Role updatedRole = roleDao.findById(role.getId());
        assertThat(updatedRole.getName()).isEqualTo("BASIC_USER");

        Role oldRole = roleDao.findByName(originalName);
        assertThat(oldRole).isNull();
    }

    @Test
    @DataSet("dataset/roles.yml")
    void shouldUpdateRoleToSameName() {
        Role role = roleDao.findByName("USER");
        role.setName("USER");

        roleDao.update(role);

        Role updatedRole = roleDao.findById(role.getId());
        assertThat(updatedRole.getName()).isEqualTo("USER");
    }

    @Test
    @DataSet("dataset/roles.yml")
    void shouldFailToUpdateRoleWithDuplicateName() {
        Role userRole = roleDao.findByName("USER");
        userRole.setName("ADMIN");

        assertThatThrownBy(() -> roleDao.update(userRole))
                .isInstanceOf(DatabaseWriteException.class)
                .hasMessageContaining("Error updating entity");
    }

    @Test
    void shouldFailToUpdateNonExistentRole() {
        Role nonExistentRole = new Role("GHOST");
        nonExistentRole.setId(999L);

        assertThatThrownBy(() -> roleDao.update(nonExistentRole))
                .isInstanceOf(DatabaseWriteException.class)
                .hasMessageContaining("Error updating entity");
    }

    @Test
    void shouldFailToUpdateRoleWithNullName() {
        Role role = new Role("TEMP");
        roleDao.create(role);

        role.setName(null);

        assertThatThrownBy(() -> roleDao.update(role))
                .isInstanceOf(DatabaseWriteException.class)
                .hasMessageContaining("Error updating entity");
    }

    @Test
    void shouldFailToUpdateNullRole() {
        assertThatThrownBy(() -> roleDao.update(null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldFailToUpdateRoleWithNullId() {
        Role role = new Role("TEMP");
        role.setId(null);

        assertThatThrownBy(() -> roleDao.update(role))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DataSet("dataset/roles.yml")
    void shouldDeleteExistingRole() {
        Role role = roleDao.findByName("MODERATOR");
        assertThat(role).isNotNull();

        roleDao.remove(role);

        Role deletedRole = roleDao.findByName("MODERATOR");
        assertThat(deletedRole).isNull();

        Role deletedById = roleDao.findById(role.getId());
        assertThat(deletedById).isNull();

        assertThat(roleDao.findAll()).hasSize(2);
    }

    @Test
    void shouldFailToDeleteNonExistentRole() {
        Role nonExistentRole = new Role("GHOST");
        nonExistentRole.setId(999L);

        assertThatThrownBy(() -> roleDao.remove(nonExistentRole))
                .isInstanceOf(DatabaseWriteException.class)
                .hasMessageContaining("Error deleting entity");
    }

    @Test
    void shouldFailToDeleteNullRole() {
        assertThatThrownBy(() -> roleDao.remove(null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldFailToDeleteRoleWithNullId() {
        Role role = new Role("TEMP");
        role.setId(null);

        assertThatThrownBy(() -> roleDao.remove(role))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldRollbackTransactionOnCreateFailure() {
        Role validRole = new Role("VALID");
        roleDao.create(validRole);

        int initialCount = roleDao.findAll().size();

        Role invalidRole = new Role("A".repeat(100));

        assertThatThrownBy(() -> roleDao.create(invalidRole))
                .isInstanceOf(DatabaseWriteException.class);

        assertThat(roleDao.findAll()).hasSize(initialCount);
    }

    @Test
    void shouldHandleMultipleSequentialOperations() {
        Role role1 = new Role("TEST1");
        roleDao.create(role1);
        assertThat(role1.getId()).isNotNull();

        Role role2 = new Role("TEST2");
        roleDao.create(role2);
        assertThat(role2.getId()).isNotNull();

        assertThat(roleDao.findAll()).hasSize(2);

        role1.setName("UPDATED1");
        roleDao.update(role1);
        assertThat(roleDao.findByName("UPDATED1")).isNotNull();

        roleDao.remove(role2);
        assertThat(roleDao.findAll()).hasSize(1);
        assertThat(roleDao.findByName("TEST2")).isNull();
    }

    @Test
    void shouldHandleCreationOfManyRoles() {
        int numberOfRoles = 100;

        for (int i = 0; i < numberOfRoles; i++) {
            Role role = new Role("ROLE_" + i);
            roleDao.create(role);
            assertThat(role.getId()).isNotNull();
        }

        List<Role> allRoles = roleDao.findAll();
        assertThat(allRoles).hasSize(numberOfRoles);
    }

    @Test
    void shouldHandleRoleNamesWithSpecialCharacters() {
        String specialName = "ROLE_WITH-SPECIAL.CHARS_123";
        Role role = new Role(specialName);

        roleDao.create(role);

        Role foundRole = roleDao.findByName(specialName);
        assertThat(foundRole).isNotNull();
        assertThat(foundRole.getName()).isEqualTo(specialName);
    }

    @Test
    void shouldHandleConcurrentLikeOperations() {
        Role role = new Role("CONCURRENT_TEST");
        roleDao.create(role);

        Role found1 = roleDao.findById(role.getId());
        Role found2 = roleDao.findByName("CONCURRENT_TEST");
        Role found3 = roleDao.findById(role.getId());

        assertThat(found1.getId()).isEqualTo(found2.getId()).isEqualTo(found3.getId());
        assertThat(found1.getName()).isEqualTo(found2.getName()).isEqualTo(found3.getName());
    }
}