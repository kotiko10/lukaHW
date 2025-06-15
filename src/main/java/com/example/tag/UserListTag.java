package com.example.tag;

import com.example.model.User;
import lombok.Setter;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Setter
public class UserListTag extends TagSupport implements Tag {
    private List<User> users;
    private String contextPath;

    @Override
    public int doStartTag() throws JspException {
        JspWriter out = pageContext.getOut();

        try {
            if (users == null || users.isEmpty()) {
                out.write("<div class='text-center py-4'>");
                out.write("<p class='text-muted'>No users found</p>");
                out.write("</div>");
                return SKIP_BODY;
            }

            out.write("<div class='user-table'>");
            out.write("<table class='table table-bordered table-hover mb-0'>");
            out.write("<thead>");
            out.write("<tr>");
            out.write("<th>Login</th>");
            out.write("<th>First Name</th>");
            out.write("<th>Last Name</th>");
            out.write("<th>Age</th>");
            out.write("<th>Role</th>");
            out.write("<th>Actions</th>");
            out.write("</tr>");
            out.write("</thead>");
            out.write("<tbody>");

            for (User user : users) {
                out.write("<tr>");

                out.write("<td><strong>" + escapeHtml(user.getLogin()) + "</strong></td>");

                out.write("<td>" + escapeHtml(user.getFirstName()) + "</td>");

                out.write("<td>" + escapeHtml(user.getLastName()) + "</td>");

                out.write("<td>");
                if (user.getBirthday() != null) {
                    LocalDate now = LocalDate.now();
                    int age = Period.between(user.getBirthday(), now).getYears();
                    out.write(String.valueOf(age));
                } else {
                    out.write("-");
                }
                out.write("</td>");

                out.write("<td>");
                if (user.getRole() != null) {
                    String roleName = user.getRole().getName();
                    if ("ADMIN".equals(roleName)) {
                        out.write("<span class='badge bg-danger'>Admin</span>");
                    } else if ("USER".equals(roleName)) {
                        out.write("<span class='badge bg-primary'>User</span>");
                    } else {
                        out.write("<span class='badge bg-secondary'>" + escapeHtml(roleName) + "</span>");
                    }
                } else {
                    out.write("-");
                }
                out.write("</td>");

                out.write("<td>");
                String editUrl = contextPath + "/admin/users/edit?id=" + user.getId();
                out.write("<a href='" + editUrl + "' class='btn-link me-2'>Edit</a>");

                String deleteOnClick = "deleteUser(" + user.getId() + ", '" +
                        escapeHtml(user.getFirstName()) + "', '" +
                        escapeHtml(user.getLastName()) + "')";
                out.write("<a href='#' onclick=\"" + deleteOnClick + "\" class='btn-link text-danger'>Delete</a>");
                out.write("</td>");

                out.write("</tr>");
            }

            out.write("</tbody>");
            out.write("</table>");
            out.write("</div>");

        } catch (IOException e) {
            throw new JspException("Error writing user list", e);
        }

        return SKIP_BODY;
    }

    @Override
    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}