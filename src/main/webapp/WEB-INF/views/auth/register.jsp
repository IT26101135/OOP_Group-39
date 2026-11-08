<%-- Module owner: IT26101135 --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url value="/css/tailwind.css" var="appCssUrl" />
<c:url value="/register" var="registerUrl" />
<c:url value="/login" var="loginUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="description" content="Register | DriveSpark Academy at DriveSpark Academy. Explore lessons, learning resources and driving school information.">
    <meta name="theme-color" content="#0B1120">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Register | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
</head>
<body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main id="main-content" class="ds-container ds-section ds-auth-page">
    <a class="ds-auth-logo" href="<c:url value='/' />"><img src="<c:url value='/images/logo.svg' />" width="190" height="40" alt="DriveSpark Academy"></a><h1>Create your student account</h1>
    <p>Enter your details to register with DriveSpark Academy.</p>

    <c:if test="${not empty formError}">
        <p role="alert"><c:out value="${formError}" /></p>
    </c:if>

    <form method="post" action="<c:out value='${registerUrl}' />">
        <input type="hidden"
               name="<c:out value='${csrfParameterName}' />"
               value="<c:out value='${csrfToken}' />">

        <p>
            <label for="name">Full name</label><br>
            <input id="name" name="name" type="text"
                   autocomplete="name"
                   required minlength="2" maxlength="100"
                   value="<c:out value='${registrationForm.name}' />">
        </p>

        <p>
            <label for="email">Email</label><br>
            <input id="email" name="email" type="email"
                   autocomplete="email" required maxlength="254"
                   value="<c:out value='${registrationForm.email}' />">
        </p>

        <p>
            <label for="phone">Phone number</label><br>
            <input id="phone" name="phone" type="tel"
                   autocomplete="tel" required maxlength="16"
                   aria-describedby="phone-help"
                   value="<c:out value='${registrationForm.phone}' />">
            <br>
            <small id="phone-help">
                Use 7–15 digits, optionally starting with +.
            </small>
        </p>

        <p>
            <label for="password">Password</label><br>
            <span data-password-wrap><input id="password" name="password" type="password" data-password-strength="password-strength-hint"
                   autocomplete="new-password" required
                   aria-describedby="password-help password-strength-hint"><button class="ds-password-toggle" type="button" data-password-toggle="password" aria-label="Show password" aria-pressed="false">Show</button></span>
            <br>
            <small id="password-help">
                Use at least 8 characters and no more than 72 UTF-8 bytes.
                Some characters use more than one byte.
            </small>
            <small id="password-strength-hint" class="block text-slate-300" aria-live="polite">Use at least 8 characters, including a letter and a number.</small>
        </p>

        <p>
            <label for="confirmPassword">Confirm password</label><br>
            <span data-password-wrap><input id="confirmPassword" name="confirmPassword"
                   type="password" autocomplete="new-password" required><button class="ds-password-toggle" type="button" data-password-toggle="confirmPassword" aria-label="Show password confirmation" aria-pressed="false">Show</button></span>
        </p>

        <button class="ds-button" type="submit">Create account</button>
    </form>

    <p>
        Already registered?
        <a href="<c:out value='${loginUrl}' />">Log in</a>
    </p>
</main>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
