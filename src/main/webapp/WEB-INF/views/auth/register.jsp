<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url value="/css/app.css" var="appCssUrl" />
<c:url value="/register" var="registerUrl" />
<c:url value="/login" var="loginUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Register | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
</head>
<body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main class="ds-container ds-section">
    <h1>Create your student account</h1>
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
            <input id="password" name="password" type="password"
                   autocomplete="new-password" required
                   aria-describedby="password-help">
            <br>
            <small id="password-help">
                Use at least 8 characters and no more than 72 UTF-8 bytes.
                Some characters use more than one byte.
            </small>
        </p>

        <p>
            <label for="confirmPassword">Confirm password</label><br>
            <input id="confirmPassword" name="confirmPassword"
                   type="password" autocomplete="new-password" required>
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