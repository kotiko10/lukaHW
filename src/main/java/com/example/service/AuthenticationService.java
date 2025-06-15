package com.example.service;

import com.example.dao.JdbcUserDao;
import com.example.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private final JdbcUserDao userDao = new JdbcUserDao();

    public User authenticate(String login, String password) {
        if (login == null || login.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return null;
        }

        try {
            User user = userDao.findByLogin(login.trim());
            if (user != null && password.equals(user.getPassword())) {
                logger.info("User authenticated: {}", login);
                return user;
            }
            return null;
        } catch (Exception e) {
            logger.error("Authentication error", e);
            return null;
        }
    }
}