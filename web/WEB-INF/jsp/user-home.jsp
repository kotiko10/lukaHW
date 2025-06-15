<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
  <title>User Home</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-5">
  <div class="text-center">
    <h1>Hello, ${sessionScope.currentUser.firstName}!</h1>
    <p class="mt-4">Click <a href="${pageContext.request.contextPath}/logout">here</a> to logout</p>
  </div>
</div>
</body>
</html>