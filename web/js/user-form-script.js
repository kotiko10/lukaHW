document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('form');
    const emailInput = document.getElementById('email');
    const emailError = document.getElementById('email-error');
    const passwordInput = document.getElementById('password');
    const passwordAgainInput = document.getElementById('passwordAgain');

    const emailPattern = /^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\.[A-Za-z]{2,})$/;

    const isEdit = form.dataset.isEdit === 'true';

    function validateEmail() {
        const email = emailInput.value.trim();

        if (!email) {
            showFieldError(emailInput, 'Email is required');
            return false;
        }

        if (email.length > 100) {
            showFieldError(emailInput, 'Email must not exceed 100 characters');
            return false;
        }

        if (!emailPattern.test(email)) {
            showFieldError(emailInput, 'Please enter a valid email address');
            return false;
        }

        clearFieldError(emailInput);
        return true;
    }

    function validatePassword() {
        const password = passwordInput.value;
        const passwordAgain = passwordAgainInput.value;

        if (!isEdit && (!password || password.trim().length === 0)) {
            showFieldError(passwordInput, 'Password is required');
            return false;
        }

        if (password && password.length < 3) {
            showFieldError(passwordInput, 'Password must be at least 3 characters long');
            return false;
        }

        if (password !== passwordAgain) {
            showFieldError(passwordAgainInput, 'Passwords do not match');
            return false;
        }

        clearFieldError(passwordInput);
        clearFieldError(passwordAgainInput);
        return true;
    }

    function validateRequiredField(field) {
        const fieldName = field.labels && field.labels[0]
            ? field.labels[0].textContent.replace('*', '').trim()
            : 'This field';

        if (!field.value.trim()) {
            showFieldError(field, fieldName + ' is required');
            return false;
        }

        clearFieldError(field);
        return true;
    }

    function showFieldError(field, message) {
        field.classList.add('is-invalid');
        const errorDiv = document.getElementById(field.id + '-error');
        if (errorDiv) {
            errorDiv.textContent = message;
            errorDiv.style.display = 'block';
        }
    }

    function clearFieldError(field) {
        field.classList.remove('is-invalid');
        const errorDiv = document.getElementById(field.id + '-error');
        if (errorDiv) {
            errorDiv.textContent = '';
            errorDiv.style.display = 'none';
        }
    }

    function initializeServerSideErrors() {
        //  function will be called from the JSP to show server-side errors
    }

    if (emailInput) {
        emailInput.addEventListener('blur', validateEmail);
        emailInput.addEventListener('input', function() {
            if (this.classList.contains('is-invalid')) {
                validateEmail();
            }
        });
    }

    if (passwordInput) {
        passwordInput.addEventListener('input', validatePassword);
    }

    if (passwordAgainInput) {
        passwordAgainInput.addEventListener('input', validatePassword);
    }

    const requiredFields = form.querySelectorAll('[required]');
    requiredFields.forEach(field => {
        if (field.id !== 'email' && field.id !== 'password' && field.id !== 'passwordAgain') {
            field.addEventListener('blur', function() {
                validateRequiredField(this);
            });

            field.addEventListener('input', function() {
                if (this.classList.contains('is-invalid')) {
                    validateRequiredField(this);
                }
            });
        }
    });

    if (form) {
        form.addEventListener('submit', function(e) {
            let isValid = true;

            if (emailInput && !validateEmail()) {
                isValid = false;
            }

            if (passwordInput && !validatePassword()) {
                isValid = false;
            }

            requiredFields.forEach(field => {
                if (field.id !== 'email' && field.id !== 'password' && field.id !== 'passwordAgain') {
                    if (!validateRequiredField(field)) {
                        isValid = false;
                    }
                }
            });

            if (!isValid) {
                e.preventDefault();
                const firstError = form.querySelector('.is-invalid');
                if (firstError) {
                    firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }
        });
    }

    window.showServerSideEmailError = function(errorMessage) {
        if (emailInput) {
            showFieldError(emailInput, errorMessage);
        }
    };
});