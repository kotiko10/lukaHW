package com.example.service;

import com.example.dao.JdbcRoleDao;
import com.example.dao.JdbcUserDao;
import com.example.model.Role;
import com.example.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import com.example.exceptions.ValidationException;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final JdbcUserDao userDao = new JdbcUserDao();
    private final JdbcRoleDao roleDao = new JdbcRoleDao();

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public User getUserById(Long id) {
        return userDao.findById(id);
    }

    public void createUser(User user) {
        if (user.getLogin() == null || user.getLogin().trim().isEmpty()) {
            throw new ValidationException("Login is required");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new ValidationException("Password is required");
        }

        // check duplicates
        if (userDao.findByLogin(user.getLogin()) != null) {
            throw new ValidationException("Login already exists");
        }
        if (userDao.findByEmail(user.getEmail()) != null) {
            throw new ValidationException("Email already exists");
        }

        userDao.create(user);
        logger.info("User created: {}", user.getLogin());
    }

    public void updateUser(User user, boolean updatePassword) {
        User existing = userDao.findById(user.getId());
        if (existing == null) {
            throw new IllegalArgumentException("User not found");
        }

        // keep existing password if not updating
        if (!updatePassword || user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            user.setPassword(existing.getPassword());
        }

        // check for duplicate login (excluding current user)
        User userWithLogin = userDao.findByLogin(user.getLogin());
        if (userWithLogin != null && !userWithLogin.getId().equals(user.getId())) {
            throw new ValidationException("Login already exists");
        }

        // check for duplicate email (excluding current user)
        User userWithEmail = userDao.findByEmail(user.getEmail());
        if (userWithEmail != null && !userWithEmail.getId().equals(user.getId())) {
            throw new ValidationException("Email already exists");
        }

        userDao.update(user);
        logger.info("User updated: {}", user.getLogin());
    }

    public void deleteUser(Long userId, User currentUser) {
        User user = userDao.findById(userId);
        if (user != null) {
            if (currentUser.getId().equals(user.getId())) {
                throw new ValidationException("User cannot delete themself");
            }
            userDao.remove(user);
            logger.info("User deleted: {}", user.getLogin());
        }
    }

    public List<Role> getAllRoles() {
        return roleDao.findAll();
    }

    public User getUserByLogin(String login) {
        return userDao.findByLogin(login);
    }

    public User getUserByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public Role getRoleById(Long roleId) {
        return roleDao.findById(roleId);
    }
}
