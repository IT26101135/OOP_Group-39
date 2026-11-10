<%--
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : register.jsp
WHAT IT DOES (simple words): Helps a new student enter their registration details.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url value="/css/tailwind.css" var="appCssUrl" />
<c:url value="/register" var="registerUrl" />
<c:url value="/login" var="loginUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="description" content="Create your student account to choose a package and request driving lessons.">
    <meta name="theme-color" content="#050505">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Register | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
</head>
<body class="ds-form-page ds-vignette-page">
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main id="main-content" class="ds-container ds-section ds-auth-page ds-public-auth">
    <%-- LocalPhotoAdvice supplies this scene; the shared edge fade leaves the form unchanged. --%>
    <aside class="ds-auth-photo ds-photo-surface ds-page-photo ds-edge-vignette" aria-label="Mountain road at sunset"><c:set var="photoKey" value="hero-road" /><c:set var="photoAlt" value="Scenic mountain road at sunset" /><c:set var="photoEager" value="true" /><%@ include file="/WEB-INF/views/common/components/local-photo.jspf" %></aside>
    <div class="ds-auth-content">
    <%-- AuthController supplies the introduction and account messages below. --%>
    <header class="ds-auth-heading"><p class="ds-eyebrow">STUDENT REGISTRATION</p><h1>Create your account.</h1>
    <p>Create your student account and log in immediately. Lessons become available after your package payment is confirmed.</p></header>

    <c:if test="${not empty formError}">
        <p role="alert"><c:out value="${formError}" /></p>
    </c:if>

    <%-- This block shows a form with its existing field names. AuthController or PasswordController supplies the page data. --%>
<form method="post" action="<c:out value='${registerUrl}' />">
        <input type="hidden"
               name="<c:out value='${csrfParameterName}' />"
               value="<c:out value='${csrfToken}' />">

        <p>
            <label for="name">Full name</label>
            <input id="name" name="name" type="text"
                   autocomplete="name"
                   required minlength="2" maxlength="100"
                   value="<c:out value='${registrationForm.name}' />">
        </p>

        <p>
            <label for="email">Email</label>
            <input id="email" name="email" type="email"
                   autocomplete="email" required maxlength="254"
                   value="<c:out value='${registrationForm.email}' />">
        </p>

        <p>
            <label for="phone">Phone number</label>
            <input id="phone" name="phone" type="tel"
                   autocomplete="tel" required maxlength="16"
                   aria-describedby="phone-help"
                   value="<c:out value='${registrationForm.phone}' />">

            <small id="phone-help">
                Use 7 to 15 digits. You can start with + for the country code.
            </small>
        </p>

        <p>
            <label for="password">Password</label>
            <span data-password-wrap><input id="password" name="password" type="password" data-password-strength="password-strength-hint"
                   autocomplete="new-password" required
                   aria-describedby="password-help password-strength-hint"><button class="ds-password-toggle" type="button" data-password-toggle="password" aria-label="Show password" aria-pressed="false">Show</button></span>

            <small id="password-help">
                Use at least 8 characters. Very long passwords, especially with symbols or non-English letters, may need to be shortened.
            </small>
            <small id="password-strength-hint" class="block text-slate-300" aria-live="polite">Use at least 8 characters, including a letter and a number.</small>
        </p>

        <p>
            <label for="confirmPassword">Confirm password</label>
            <span data-password-wrap><input id="confirmPassword" name="confirmPassword"
                   type="password" autocomplete="new-password" required><button class="ds-password-toggle" type="button" data-password-toggle="confirmPassword" aria-label="Show password confirmation" aria-pressed="false">Show</button></span>
        </p>

        <button class="ds-button ds-auth-submit" type="submit">Create account</button>
    </form>

    <%-- AuthController keeps the existing login route available after registration. --%>
    <p>
        Already registered?
        <a href="<c:out value='${loginUrl}' />">Log in</a>
    </p>
    </div>
</main>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
