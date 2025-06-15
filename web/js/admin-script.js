document.addEventListener('DOMContentLoaded', function() {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            alert.style.display = 'none';
        }, 3000);
    });
});

function deleteUser(userId, userName, userSureName) {
    console.log("Delete function called for user:", userId, userName, userSureName);
    if (confirm('Are you sure you want to delete user ' + userName + ' ' + userSureName + '?')) {
        let deleteUrl = window.contextPath + '/admin/users/delete?id=' + userId;
        console.log("Redirecting to:", deleteUrl);
        window.location.href = deleteUrl;
    } else {
        console.log("Delete cancelled by user");
    }
}

function confirmDelete(userId, userName, contextPath) {
    if (confirm('Are you sure you want to delete user ' + userName + '?')) {
        window.location.href = contextPath + '/admin/users/delete?id=' + userId;
    }
}

