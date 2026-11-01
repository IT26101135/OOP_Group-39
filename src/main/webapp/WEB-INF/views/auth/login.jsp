<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url value="/css/app.css" var="appCssUrl" />
<c:url value="/login" var="loginUrl" />
<c:url value="/register" var="registerUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Login | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
</head>
<body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main class="ds-container ds-section">
    <h1>Log in</h1>

    <c:if test="${param.registered != null}">
        <p role="status">Your account was created. You can now log in.</p>
    </c:if>

    <c:if test="${param.loggedOut != null}">
        <p role="status">You have been logged out.</p>
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
            <input id="password" name="password" type="password"
                   autocomplete="current-password" required>
        </p>

        <button class="ds-button" type="submit">Log in</button>
    </form>

    <p>
        Need an account?
        <a href="<c:out value='${registerUrl}' />">Register</a>
    </p>
</main>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>