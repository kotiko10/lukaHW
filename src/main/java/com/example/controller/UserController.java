package com.example.controller;

import com.example.exceptions.ValidationException;
import com.example.model.Role;
import com.example.model.User;
import com.example.service.UserService;
import com.example.util.SessionUtils;
import com.example.validator.UserValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet(name = "UserController", urlPatterns = {"/admin/users", "/admin/users/add", "/admin/users/edit", "/admin/users/delete"})
public class UserController extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService = new UserService();
    private final UserValidator userValidator = new UserValidator();

    private static final String USER_FORM_JSP = "/WEB-INF/jsp/user-form.jsp";
    private static final String ADMIN_HOME_JSP = "/WEB-INF/jsp/admin-home.jsp";
    private static final String ADMIN_USERS_PATH =  "/admin/users";
    private static final String ADD_PATH =  "/admin/users/add";
    private static final String EDIT_PATH =  "/admin/users/edit";
    private static final String DELETE_PATH =  "/admin/users/delete";

    private static final String ATTR_ROLES = "roles";
    private static final String ATTR_IS_EDIT = "isEdit";
    private static final String ATTR_ERRORS= "errors";
    private static final String ATTR_USER = "user";


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtils.isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String path = request.getServletPath();
        logger.info("Request path: {}", path);
        try {
            switch (path) {
                case ADMIN_USERS_PATH:
                    showUserList(request, response);
                    break;
                case ADD_PATH:
                    showAddUserForm(request, response);
                    break;
                case EDIT_PATH:
                    showEditUserForm(request, response);
                    break;
                case DELETE_PATH:
                    deleteUser(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error in UserController", e);
            request.setAttribute("error", "An error occurred: " + e.getMessage());
            request.getRequestDispatcher(ADMIN_HOME_JSP).forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtils.isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String path = request.getServletPath();

        try {
            switch (path) {
                case ADD_PATH:
                    handleAddUser(request, response);
                    break;
                case EDIT_PATH:
                    handleEditUser(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error in UserController POST", e);
            request.setAttribute("error", "An error occurred: " + e.getMessage());
            showUserFormWithError(request, response, createUserFromRequest(request));
        }
    }

    private void showUserList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<User> users = userService.getAllUsers();
        request.setAttribute("users", users);
        request.getRequestDispatcher(ADMIN_HOME_JSP).forward(request, response);
    }

    private void showAddUserForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Role> roles = userService.getAllRoles();
        request.setAttribute(ATTR_ROLES, roles);
        request.setAttribute(ATTR_IS_EDIT, false);

        if (request.getAttribute(ATTR_USER) == null) {
            request.setAttribute(ATTR_USER, new User());
        }

        request.getRequestDispatcher(USER_FORM_JSP).forward(request, response);
    }

    private void showEditUserForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.sendRedirect(request.getContextPath() + ADMIN_USERS_PATH);
            return;
        }

        try {
            Long userId = Long.parseLong(idParam);
            User user = userService.getUserById(userId);
            if (user == null) {
                request.setAttribute("error", "User not found");
                showUserList(request, response);
                return;
            }

            List<Role> roles = userService.getAllRoles();
            request.setAttribute(ATTR_USER, user);
            request.setAttribute(ATTR_ROLES, roles);
            request.setAttribute(ATTR_IS_EDIT, true);
            request.getRequestDispatcher(USER_FORM_JSP).forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + ADMIN_USERS_PATH);
        }
    }

    private void handleAddUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = createUserFromRequest(request);
        List<String> errors = userValidator.validateForCreate(user);

        if (!errors.isEmpty()) {
            request.setAttribute(ATTR_ERRORS, errors);
            request.setAttribute(ATTR_USER, user);
            request.setAttribute(ATTR_IS_EDIT, false);
            List<Role> roles = userService.getAllRoles();
            request.setAttribute(ATTR_ROLES, roles);
            request.getRequestDispatcher(USER_FORM_JSP).forward(request, response);
            return;
        }

        userService.createUser(user);
        response.sendRedirect(request.getContextPath() + "/admin/users?success=User created successfully");
    }

    private void handleEditUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.sendRedirect(request.getContextPath() + ADMIN_USERS_PATH);
            return;
        }

        Long userId = Long.parseLong(idParam);
        User user = createUserFromRequest(request);
        user.setId(userId);
        boolean updatePassword = request.getParameter("password") != null &&
                !request.getParameter("password").trim().isEmpty();

        List<String> errors = userValidator.validateForUpdate(user, updatePassword);

        if (!errors.isEmpty()) {
            request.setAttribute(ATTR_ERRORS, errors);
            request.setAttribute(ATTR_USER, user);
            request.setAttribute(ATTR_IS_EDIT, true);
            List<Role> roles = userService.getAllRoles();
            request.setAttribute(ATTR_ROLES, roles);
            request.getRequestDispatcher(USER_FORM_JSP).forward(request, response);
            return;
        }

        userService.updateUser(user, updatePassword);
        response.sendRedirect(request.getContextPath() + "/admin/users?success=User updated successfully");
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idParam = request.getParameter("id");
        if (idParam != null) {
            try {
                Long userId = Long.parseLong(idParam);
                userService.deleteUser(userId, SessionUtils.getCurrentUser(request));
                response.sendRedirect(request.getContextPath() + "/admin/users?success=User deleted successfully");
            } catch (NumberFormatException e) {
                response.sendRedirect(request.getContextPath() + "/admin/users?error=Invalid user ID");
            } catch (ValidationException e) { //deleteUser throws validation exception for admin deleting himself and I catch it here
                response.sendRedirect(request.getContextPath() + "/admin/users?error=" + e.getMessage());
            } catch (Exception e) {
                response.sendRedirect(request.getContextPath() + "/admin/users?error=Error deleting user");
            }
        } else {
            response.sendRedirect(request.getContextPath() + ADMIN_USERS_PATH);
        }
    }

    private User createUserFromRequest(HttpServletRequest request) {
        User user = new User();
        user.setLogin(request.getParameter("login"));
        user.setPassword(request.getParameter("password"));
        user.setEmail(request.getParameter("email"));
        user.setFirstName(request.getParameter("firstName"));
        user.setLastName(request.getParameter("lastName"));

        String birthdayStr = request.getParameter("birthday");
        if (birthdayStr != null && !birthdayStr.trim().isEmpty()) {
            try {
                user.setBirthday(LocalDate.parse(birthdayStr));
            } catch (DateTimeParseException e) {
                logger.warn("Invalid birthday format: {}", birthdayStr);
            }
        }

        String roleIdStr = request.getParameter("roleId");
        if (roleIdStr != null && !roleIdStr.trim().isEmpty()) {
            try {
                Long roleId = Long.parseLong(roleIdStr);
                Role role = userService.getRoleById(roleId);
                user.setRole(role);
            } catch (NumberFormatException e) {
                logger.warn("Invalid role ID: {}", roleIdStr);
            }
        }

        return user;
    }

    private void showUserFormWithError(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        List<Role> roles = userService.getAllRoles();
        request.setAttribute(ATTR_ROLES, roles);
        request.setAttribute(ATTR_USER, user);
        request.getRequestDispatcher(USER_FORM_JSP).forward(request, response);
    }
}