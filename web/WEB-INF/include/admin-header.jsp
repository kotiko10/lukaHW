<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="admin-header">
    <div class="container">
        <div class="d-flex justify-content-between align-items-center">
            <h4 class="mb-0">Admin ${sessionScope.currentUser.firstName} ${sessionScope.currentUser.lastName}</h4>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-secondary btn-sm">Logout</a>
        </div>
    </div>
</div>

