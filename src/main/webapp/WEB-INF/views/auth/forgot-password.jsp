<%--
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : forgot-password.jsp
WHAT IT DOES (simple words): Shows forgot password information and controls.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url value="/css/tailwind.css" var="appCssUrl" />
<!DOCTYPE html><html lang="en"><head>
    <meta name="description" content="Request a reset code and choose a new password for your account.">
    <meta name="theme-color" content="#050505">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />"><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Reset password | DriveSpark Academy</title><link rel="stylesheet" href="<c:out value='${appCssUrl}' />"></head>
<body class="ds-form-page"><%@ include file="/WEB-INF/views/common/navbar.jspf" %><main id="main-content" class="ds-container ds-section ds-auth-page ds-public-auth ds-auth-recovery"><header class="ds-auth-heading"><a class="ds-wordmark" href="<c:url value='/' />">DriveSpark<span>Academy</span></a><h1>Reset your password.</h1></header>
<c:if test="${resetRequested}"><p role="status">If your account is eligible, use the reset code below to choose a new password.</p></c:if>
<c:if test="${not empty demoResetToken}"><p>Reset code: <code><c:out value="${demoResetToken}" /></code></p></c:if>
<%-- This block shows a form with its existing field names. AuthController or PasswordController supplies the page data. --%>
<div class="ds-auth-forms"><form method="post" action="<c:url value='/forgot-password' />"><h2>Request a reset code</h2><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><label>Email <input type="email" name="email" value="<c:out value='${resetEmail}' />" required></label><button class="ds-button" type="submit">Request reset code</button></form>
<%-- This block shows a form with its existing field names. AuthController or PasswordController supplies the page data. --%>
<form method="post" action="<c:url value='/reset-password' />"><h2>Set a new password</h2><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><label>Email <input type="email" name="email" required></label><label>Reset code <input name="token" required></label><label>New password <span data-password-wrap><input id="reset-password" type="password" data-password-strength="reset-strength" name="password" minlength="8" required><button class="ds-password-toggle" type="button" data-password-toggle="reset-password" aria-label="Show password" aria-pressed="false">Show</button></span><small id="reset-strength" aria-live="polite">Use at least 8 characters, including a letter and a number.</small></label><label>Confirm password <span data-password-wrap><input id="reset-confirm" type="password" name="confirmPassword" minlength="8" required><button class="ds-password-toggle" type="button" data-password-toggle="reset-confirm" aria-label="Show password confirmation" aria-pressed="false">Show</button></span></label><button class="ds-button" type="submit">Reset password</button></form>
</div></main><%@ include file="/WEB-INF/views/common/footer.jspf" %></body></html>
