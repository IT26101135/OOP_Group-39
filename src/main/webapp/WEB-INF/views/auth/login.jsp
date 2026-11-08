<%-- Module owner: IT26101135 --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url value="/css/tailwind.css" var="appCssUrl" />
<c:url value="/login" var="loginUrl" />
<c:url value="/register" var="registerUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="description" content="Login | DriveSpark Academy at DriveSpark Academy. Explore lessons, learning resources and driving school information.">
    <meta name="theme-color" content="#0B1120">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Login | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
</head>
<body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main id="main-content" class="ds-container ds-section ds-auth-page">
    <a class="ds-auth-logo" href="<c:url value='/' />"><img src="<c:url value='/images/logo.svg' />" width="190" height="40" alt="DriveSpark Academy"></a><h1>Log in</h1>

    <c:if test="${param.registered != null}">
        <p role="status">Your account was created. You can now log in.</p>
    </c:if>

    <c:if test="${param.loggedOut != null}">
        <p role="status">You have been logged out.</p>
    </c:if>
    <c:if test="${param.required != null}">
        <p role="status">Please log in to view that page.</p>
    </c:if>
    <c:if test="${param.accountDeleted != null}">
        <p role="status">Your account was deleted.</p>
    </c:if>
    <c:if test="${passwordReset eq true or param.passwordReset != null}">
        <p role="status">Your password was reset. You can log in with the new password.</p>
    </c:if>

    <c:if test="${not empty formError}">
        <p role="alert"><c:out value="${formError}" /></p>
    </c:if>

    <form method="post" action="<c:out value='${loginUrl}' />">
        <input type="hidden"
               name="<c:out value='${csrfParameterName}' />"
               value="<c:out value='${csrfToken}' />">

        <p>
            <label for="email">Email</label><br>
            <input id="email" name="email" type="email"
                   autocomplete="username" required maxlength="254"
                   value="<c:out value='${loginForm.email}' />">
        </p>

        <p>
            <label for="password">Password</label><br>
            <span data-password-wrap><input id="password" name="password" type="password"
                   autocomplete="current-password" required><button class="ds-password-toggle" type="button" data-password-toggle="password" aria-label="Show password" aria-pressed="false">Show</button></span>
        </p>

        <button class="ds-button" type="submit">Log in</button>
    </form>

    <p>
        Need an account?
        <a href="<c:out value='${registerUrl}' />">Register</a>
    </p>
    <p><a href="<c:url value='/forgot-password' />">Forgot password?</a></p>
</main>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
