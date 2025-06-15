package com.example.controller;

import com.example.util.SessionUtils;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "HomeController", urlPatterns = {"/user/home", "/admin/home", "/"})
public class HomeController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/".equals(path)) {
            if (SessionUtils.isLoggedIn(request)) {
                if (SessionUtils.isAdmin(request)) {
                    response.sendRedirect(request.getContextPath() + "/admin/users");
                } else {
                    response.sendRedirect(request.getContextPath() + "/user/home");
                }
            } else {
                response.sendRedirect(request.getContextPath() + "/login");
            }
            return;
        }

        if (!SessionUtils.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if ("/user/home".equals(path)) {
            request.getRequestDispatcher("/WEB-INF/jsp/user-home.jsp").forward(request, response);
        } else if ("/admin/home".equals(path)) {
            if (SessionUtils.isAdmin(request)) {
                response.sendRedirect(request.getContextPath() + "/admin/users");
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            }
        }
    }
}
