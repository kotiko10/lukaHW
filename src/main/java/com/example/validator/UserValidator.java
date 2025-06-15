package com.example.validator;

import com.example.model.User;
import com.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import com.example.exceptions.DatabaseReadException;

public class UserValidator {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    private final UserService userService = new UserService();
    protected static final Logger logger = LoggerFactory.getLogger(UserValidator.class);

    private final String LOGIN_NOT_UNIQUE = "Unable to verify login uniqueness. Please try again.";

    public List<String> validateForCreate(User user) {
        List<String> errors = new ArrayList<>();
        validateBasicFields(user, errors);

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            errors.add("Password is required");
        } else if (user.getPassword().length() < 3) {
            errors.add("Password must be at least 3 characters long");
        }

        try {
            if (userService.getUserByLogin(user.getLogin()) != null) {
                errors.add("Login already exists");
            }
        } catch (DatabaseReadException e) {
            logger.error("Database error: {}", e.getMessage(), e);
            errors.add("Unable to verify login uniqueness. Please try again.");
        } catch (RuntimeException e) {
            logger.error("Unexpected error while checking login during create: {}", e.getMessage(), e);
            errors.add("System error. Please try again.");
        }

        try {
            if (userService.getUserByEmail(user.getEmail()) != null) {
                errors.add("Email already exists");
            }
        } catch (DatabaseReadException e) {
            logger.error("Database error while checking email during create: {}", e.getMessage(), e);
            errors.add("Unable to verify email uniqueness. Please try again.");
        } catch (RuntimeException e) {
            logger.error("Unexpected error while checking email during create: {}", e.getMessage(), e);
            errors.add("System error. Please try again.");
        }

        return errors;
    }

    public List<String> validateForUpdate(User user, boolean updatePassword) {
        List<String> errors = new ArrayList<>();

        validateBasicFields(user, errors);

        if (user.getId() == null) {
            errors.add("User ID is required for update");
        }

        // Validate password if it's being updated
        if (updatePassword) {
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                errors.add("Password cannot be empty when updating password");
            } else if (user.getPassword().length() < 3) {
                errors.add("Password must be at least 3 characters long");
            }
        }

        try {
            User existingUser = userService.getUserByLogin(user.getLogin());
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                errors.add("Login already exists");
            }
        } catch (DatabaseReadException e) {
            logger.error("Database error while checking login during update: {}", e.getMessage(), e);
            errors.add(LOGIN_NOT_UNIQUE);
        } catch (RuntimeException e) {
            logger.error("Unexpected error while checking login during update: {}", e.getMessage(), e);
            errors.add("System error. Please try again.");
        }

        try {
            User existingUser = userService.getUserByEmail(user.getEmail());
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                errors.add("Email already exists");
            }
        } catch (DatabaseReadException e) {
            logger.error("Database error while checking email during update: {}", e.getMessage(), e);
            errors.add(LOGIN_NOT_UNIQUE);
        } catch (RuntimeException e) {
            logger.error("Unexpected error while checking email during update: {}", e.getMessage(), e);
            errors.add("System error. Please try again.");
        }
        return errors;
    }

    private void validateBasicFields(User user, List<String> errors) {
        if (user == null) {
            errors.add("User cannot be null");
            return;
        }

        if (user.getLogin() == null || user.getLogin().trim().isEmpty()) {
            errors.add("Login is required");
        } else if (user.getLogin().length() < 3) {
            errors.add("Login must be at least 3 characters long");
        } else if (user.getLogin().length() > 50) {
            errors.add("Login must not exceed 50 characters");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            errors.add("Email is required");
        } else if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            errors.add("Please enter a valid email address");
        } else if (user.getEmail().length() > 100) {
            errors.add("Email must not exceed 100 characters");
        }

        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            errors.add("First name is required");
        } else if (user.getFirstName().length() > 50) {
            errors.add("First name must not exceed 50 characters");
        }

        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            errors.add("Last name is required");
        } else if (user.getLastName().length() > 50) {
            errors.add("Last name must not exceed 50 characters");
        }

        if (user.getBirthday() != null) {
            LocalDate now = LocalDate.now();
            if (user.getBirthday().isAfter(now)) {
                errors.add("Birthday cannot be in the future");
            }
            if (user.getBirthday().isBefore(now.minusYears(150))) {
                errors.add("Birthday cannot be more than 150 years ago");
            }
        }
        if (user.getRole() == null) {
            errors.add("Role is required");
        }
    }
}