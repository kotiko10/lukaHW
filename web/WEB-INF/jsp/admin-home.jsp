<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="u" uri="http://example.com/usertags" %>
<!DOCTYPE html>
<html>
<head>
    <title>Admin Home</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/admin-style.css" rel="stylesheet">
</head>
<body>
<%@include file="../include/admin-header.jsp" %>

<div class="container">
    <c:if test="${not empty param.success}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
                ${param.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${not empty param.error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                ${param.error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="mb-3">
        <a href="${pageContext.request.contextPath}/admin/users/add" class="add-user-link">Add new user</a>
    </div>

    <u:userList users="${users}" contextPath="${pageContext.request.contextPath}" />
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    window.contextPath = '${pageContext.request.contextPath}' || '';
</script>
<script src="/js/admin-script.js"></script>
</body>
</html>