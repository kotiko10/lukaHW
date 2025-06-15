package com.example.service;

import com.example.dao.HibernateRoleDao;
import com.example.dao.HibernateUserDao;
import com.example.dao.RoleDao;
import com.example.dao.UserDao;
import com.example.model.Role;
import com.example.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Set;

import com.example.exceptions.ValidationException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao = new HibernateUserDao();
    private final RoleDao roleDao = new HibernateRoleDao();
    private final Validator validator;

    public UserService() {
        try(ValidatorFactory factory = Validation.buildDefaultValidatorFactory()){
            this.validator = factory.getValidator();
        }
    }

    public List<User> getAllUsers() {
        try {
            return userDao.findAll();
        } catch (Exception e) {
            logger.error("Error retrieving all users", e);
            throw new RuntimeException("Error retrieving users", e);
        }
    }

    public User getUserById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        try {
            return userDao.findById(id);
        } catch (Exception e) {
            logger.error("Error retrieving user by id: {}", id, e);
            throw new RuntimeException("Error retrieving user", e);
        }
    }

    public void createUser(User user) {
        validateUser(user, true);

        try {
            if (userDao.findByLogin(user.getLogin()) != null) {
                throw new IllegalArgumentException("Login already exists: " + user.getLogin());
            }

            if (userDao.findByEmail(user.getEmail()) != null) {
                throw new IllegalArgumentException("Email already exists: " + user.getEmail());
            }

            userDao.create(user);
            logger.info("User created successfully: {}", user.getLogin());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating user: {}", user.getLogin(), e);
            throw new RuntimeException("Error creating user", e);
        }
    }

    public void updateUser(User user, boolean updatePassword) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID is required for update");
        }

        try {
            User existingUser = userDao.findById(user.getId());
            if (existingUser == null) {
                throw new IllegalArgumentException("User not found with ID: " + user.getId());
            }

            if (!updatePassword || user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                user.setPassword(existingUser.getPassword());
            }

            validateUser(user, updatePassword);

            User userWithLogin = userDao.findByLogin(user.getLogin());
            if (userWithLogin != null && !userWithLogin.getId().equals(user.getId())) {
                throw new IllegalArgumentException("Login already exists: " + user.getLogin());
            }
            User userWithEmail = userDao.findByEmail(user.getEmail());
            if (userWithEmail != null && !userWithEmail.getId().equals(user.getId())) {
                throw new IllegalArgumentException("Email already exists: " + user.getEmail());
            }

            userDao.update(user);
            logger.info("User updated successfully: {}", user.getLogin());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating user: {}", user.getLogin(), e);
            throw new RuntimeException("Error updating user", e);
        }
    }

    public void deleteUser(Long userId, User curUser) throws Exception {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if(curUser.getId() == userId)
        {
            throw new Exception("can't delete yoursefl");
        }

        try {
            User user = userDao.findById(userId);
            if (user != null) {
                userDao.remove(user);
                logger.info("User deleted successfully: {}", user.getLogin());
            } else {
                logger.warn("Attempted to delete non-existent user with ID: {}", userId);
            }
        } catch (Exception e) {
            logger.error("Error deleting user with ID: {}", userId, e);
            throw new RuntimeException("Error deleting user", e);
        }
    }

    public List<Role> getAllRoles() {
        try {
            return roleDao.findAll();
        } catch (Exception e) {
            logger.error("Error retrieving all roles", e);
            throw new RuntimeException("Error retrieving roles", e);
        }
    }

    public User getUserByLogin(String login) {
        if (login == null || login.trim().isEmpty()) {
            return null;
        }

        try {
            return userDao.findByLogin(login.trim());
        } catch (Exception e) {
            logger.error("Error retrieving user by login: {}", login, e);
            throw new RuntimeException("Error retrieving user", e);
        }
    }

    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        try {
            return userDao.findByEmail(email.trim());
        } catch (Exception e) {
            logger.error("Error retrieving user by email: {}", email, e);
            throw new RuntimeException("Error retrieving user", e);
        }
    }

    public Role getRoleById(Long roleId) {
        if (roleId == null) {
            return null;
        }

        try {
            return roleDao.findById(roleId);
        } catch (Exception e) {
            logger.error("Error retrieving role by id: {}", roleId, e);
            throw new RuntimeException("Error retrieving role", e);
        }
    }

    private void validateUser(User user, boolean validatePassword) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder("Validation errors: ");
            for (ConstraintViolation<User> violation : violations) {
                sb.append(violation.getMessage()).append("; ");
            }
            throw new IllegalArgumentException(sb.toString());
        }

        if (validatePassword && (user.getPassword() == null || user.getPassword().trim().isEmpty())) {
            throw new IllegalArgumentException("Password is required");
        }
    }
}
