<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
  <title><c:choose><c:when test="${isEdit}">Edit user</c:when><c:otherwise>Add user</c:otherwise></c:choose></title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="/css/user-form-style.css">
</head>
<body>
<%@include file="../include/admin-header.jsp" %>
<div class="container">
  <div class="row justify-content-center">
    <div class="col-lg-8">
      <div class="form-container">
        <h2 class="form-title">
          <c:choose>
            <c:when test="${isEdit}">Edit user</c:when>
            <c:otherwise>Add user</c:otherwise>
          </c:choose>
        </h2>

        <c:if test="${not empty errors}">
          <div class="alert alert-danger" role="alert" id="generalErrors">
            <h6 class="alert-heading">Please fix the following errors:</h6>
            <ul class="mb-0">
              <c:forEach var="error" items="${errors}">
                <c:if test="${!error.contains('email') && !error.contains('Email')}">
                  <li>${error}</li>
                </c:if>
              </c:forEach>
            </ul>
          </div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/admin/users/${isEdit ? 'edit' : 'add'}"
              novalidate data-is-edit="${isEdit}">
          <c:if test="${isEdit}">
            <input type="hidden" name="id" value="${user.id}">
          </c:if>

          <div class="row">
            <div class="col-md-6">
              <div class="mb-3">
                <label for="login" class="form-label">Login <span class="required">*</span></label>
                <input type="text" class="form-control" id="login" name="login"
                       value="${user.login}" required ${isEdit ? 'readonly' : ''}>
                <div class="invalid-feedback" id="login-error"></div>
                <c:if test="${isEdit}">
                  <div class="form-note">This field cannot be edited when editing a user</div>
                </c:if>
              </div>

              <div class="mb-3">
                <label for="password" class="form-label">
                  Password
                  <c:if test="${not isEdit}"><span class="required">*</span></c:if>
                </label>
                <input type="password" class="form-control" id="password" name="password"
                ${not isEdit ? 'required' : ''}>
                <div class="invalid-feedback" id="password-error"></div>
                <c:if test="${isEdit}">
                  <div class="form-note">Leave empty to keep current password</div>
                </c:if>
              </div>

              <div class="mb-3">
                <label for="passwordAgain" class="form-label">
                  Password again
                  <c:if test="${not isEdit}"><span class="required">*</span></c:if>
                </label>
                <input type="password" class="form-control" id="passwordAgain" name="passwordAgain"
                ${not isEdit ? 'required' : ''}>
                <div class="invalid-feedback" id="passwordAgain-error"></div>
                <div class="form-note">Must match the password above</div>
              </div>

              <div class="mb-3">
                <label for="email" class="form-label">Email <span class="required">*</span></label>
                <input type="email" class="form-control" id="email" name="email"
                       value="${user.email}" required>
                <div class="invalid-feedback" id="email-error"></div>
              </div>
            </div>

            <div class="col-md-6">
              <div class="mb-3">
                <label for="firstName" class="form-label">First name <span class="required">*</span></label>
                <input type="text" class="form-control" id="firstName" name="firstName"
                       value="${user.firstName}" required>
                <div class="invalid-feedback" id="firstName-error"></div>
              </div>

              <div class="mb-3">
                <label for="lastName" class="form-label">Last name <span class="required">*</span></label>
                <input type="text" class="form-control" id="lastName" name="lastName"
                       value="${user.lastName}" required>
                <div class="invalid-feedback" id="lastName-error"></div>
              </div>

              <div class="mb-3">
                <label for="birthday" class="form-label">Birthday</label>
                <input type="date" class="form-control" id="birthday" name="birthday"
                       value="${user.birthday}">
                <div class="invalid-feedback" id="birthday-error"></div>
              </div>

              <div class="mb-3">
                <label for="roleId" class="form-label">Role <span class="required">*</span></label>
                <select class="form-select" id="roleId" name="roleId" required>
                  <option value="">Select a role</option>
                  <c:forEach var="role" items="${roles}">
                    <option value="${role.id}"
                            <c:if test="${user.role != null && user.role.id == role.id}">selected</c:if>>
                      <c:choose>
                        <c:when test="${role.name == 'ADMIN'}">Admin</c:when>
                        <c:when test="${role.name == 'USER'}">User</c:when>
                        <c:otherwise>${role.name}</c:otherwise>
                      </c:choose>
                    </option>
                  </c:forEach>
                </select>
                <div class="invalid-feedback" id="roleId-error"></div>
              </div>
            </div>
          </div>

          <div class="d-flex justify-content-end btn-group-custom mt-4">
            <a href="${pageContext.request.contextPath}/admin/users"
               class="btn btn-secondary">Cancel</a>
            <button type="submit" class="btn btn-primary">
              <c:choose>
                <c:when test="${isEdit}">Update User</c:when>
                <c:otherwise>Ok</c:otherwise>
              </c:choose>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="/js/user-form-script.js"></script>
<c:forEach var="error" items="${errors}">
  <c:if test="${error.contains('email') || error.contains('Email')}">
    <script>
      document.addEventListener('DOMContentLoaded', function() {
        if (window.showServerSideEmailError) {
          window.showServerSideEmailError('${error}');
        }
      });
    </script>
  </c:if>
</c:forEach>

</body>
</html>