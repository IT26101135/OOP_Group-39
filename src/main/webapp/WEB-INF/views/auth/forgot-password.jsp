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
    <meta name="description" content="Reset password at DriveSpark Academy. Explore lessons, learning resources and driving school information.">
    <meta name="theme-color" content="#0B1120">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />"><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Reset password</title><link rel="stylesheet" href="<c:out value='${appCssUrl}' />"></head>
<body><%@ include file="/WEB-INF/views/common/navbar.jspf" %><main id="main-content" class="ds-container ds-section ds-auth-page"><a class="ds-auth-logo" href="<c:url value='/' />"><img src="<c:url value='/images/logo.svg' />" width="190" height="40" alt="DriveSpark Academy"></a><h1>Forgot password</h1>
<c:if test="${resetRequested}"><p role="status">If the account exists, a reset code is available for this local demonstration.</p></c:if>
<c:if test="${not empty demoResetToken}"><p>Demo reset code: <code><c:out value="${demoResetToken}" /></code></p></c:if>
<%-- This block shows a form with its existing field names. AuthController or PasswordController supplies the page data. --%>
<form method="post" action="<c:url value='/forgot-password' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><label>Email <input type="email" name="email" value="<c:out value='${resetEmail}' />" required></label><button type="submit">Request reset code</button></form>
<h2>Set a new password</h2><%-- This block shows a form with its existing field names. AuthController or PasswordController supplies the page data. --%>
<form method="post" action="<c:url value='/reset-password' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><label>Email <input type="email" name="email" required></label><label>Reset code <input name="token" required></label><label>New password <span data-password-wrap><input id="reset-password" type="password" data-password-strength="reset-strength" name="password" minlength="8" required><button class="ds-password-toggle" type="button" data-password-toggle="reset-password" aria-label="Show password" aria-pressed="false">Show</button></span><small id="reset-strength" aria-live="polite">Use 8+ characters, with a letter and number.</small></label><label>Confirm password <span data-password-wrap><input id="reset-confirm" type="password" name="confirmPassword" minlength="8" required><button class="ds-password-toggle" type="button" data-password-toggle="reset-confirm" aria-label="Show password confirmation" aria-pressed="false">Show</button></span></label><button type="submit">Reset password</button></form>
</main><%@ include file="/WEB-INF/views/common/footer.jspf" %></body></html>
