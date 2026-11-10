<%--
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : login.jsp
WHAT IT DOES (simple words): Shows a clear login form and friendly account messages.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url value="/css/tailwind.css" var="appCssUrl" />
<c:url value="/login" var="loginUrl" />
<c:url value="/register" var="registerUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="description" content="Log in to manage your DriveSpark Academy lessons, enrolments and learning progress.">
    <meta name="theme-color" content="#050505">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Log in | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
</head>
<body class="ds-form-page ds-vignette-page">
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main id="main-content" class="ds-container ds-section ds-auth-page ds-public-auth">
    <%-- LocalPhotoAdvice supplies this scene; the shared edge fade leaves the form unchanged. --%>
    <aside class="ds-auth-photo ds-photo-surface ds-page-photo ds-edge-vignette" aria-label="Mountain road at sunset"><c:set var="photoKey" value="hero-road" /><c:set var="photoAlt" value="Scenic mountain road at sunset" /><c:set var="photoEager" value="true" /><%@ include file="/WEB-INF/views/common/components/local-photo.jspf" %></aside>
    <div class="ds-auth-content">
    <%-- AuthController supplies the introduction and account messages below. --%>
    <header class="ds-auth-heading"><p class="ds-eyebrow">YOUR ACCOUNT</p><h1>Welcome back.</h1>
    <p>Check your lessons, payments and progress.</p></header>

    <c:if test="${param.registered != null}">
        <p role="status">Your account was created. You can log in now.</p>
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

    <%-- This block shows a form with its existing field names. AuthController or PasswordController supplies the page data. --%>
<form method="post" action="<c:out value='${loginUrl}' />">
        <input type="hidden"
               name="<c:out value='${csrfParameterName}' />"
               value="<c:out value='${csrfToken}' />">

        <p>
            <label for="email">Email</label>
            <input id="email" name="email" type="email"
                   autocomplete="username" required maxlength="254"
                   value="<c:out value='${loginForm.email}' />">
        </p>

        <p>
            <label for="password">Password</label>
            <span data-password-wrap><input id="password" name="password" type="password"
                   autocomplete="current-password" required><button class="ds-password-toggle" type="button" data-password-toggle="password" aria-label="Show password" aria-pressed="false">Show</button></span>
        </p>

        <button class="ds-button ds-auth-submit" type="submit">Log in</button>
    </form>

    <%-- AuthController keeps these links available alongside the login form. --%>
    <p>
        Need an account?
        <a href="<c:out value='${registerUrl}' />">Register</a>
    </p>
    <p><a href="<c:url value='/forgot-password' />">Forgot password?</a></p>
    </div>
</main>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
