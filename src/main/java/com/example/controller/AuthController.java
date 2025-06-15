package com.example.controller;

import com.example.model.User;
import com.example.service.AuthenticationService;
import com.example.util.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AuthController", urlPatterns = {"/login", "/logout"})
public class AuthController extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationService authService = new AuthenticationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getServletPath();

        if ("/logout".equals(action)) {
            handleLogout(request, response);
        } else {
            if (SessionUtils.isLoggedIn(request)) {
                redirectToHomePage(request, response);
            } else {
                request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getServletPath();

        if ("/login".equals(action)) {
            handleLogin(request, response);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String login = request.getParameter("login");
        String password = request.getParameter("password");

        try {
            User user = authService.authenticate(login, password);

            if (user != null) {
                SessionUtils.setCurrentUser(request, user);
                logger.info("User logged in: {}", login);
                redirectToHomePage(request, response);
            } else {
                request.setAttribute("error", "Invalid login or password");
                request.setAttribute("login", login);
                request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
            }
        } catch (Exception e) {
            logger.error("Login error", e);
            request.setAttribute("error", "An error occurred during login");
            request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        SessionUtils.logout(request);
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private void redirectToHomePage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (SessionUtils.isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/admin/users");
        } else {
            response.sendRedirect(request.getContextPath() + "/user/home");
        }
    }
}
