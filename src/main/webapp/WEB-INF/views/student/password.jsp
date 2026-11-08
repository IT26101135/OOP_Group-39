<%--
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : password.jsp
WHAT IT DOES (simple words): Shows password information and controls.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url value="/css/tailwind.css" var="appCssUrl" />
<!DOCTYPE html><html lang="en"><head>
    <meta name="description" content="Change password at DriveSpark Academy. Explore lessons, learning resources and driving school information.">
    <meta name="theme-color" content="#0B1120">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />"><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Change password</title><link rel="stylesheet" href="<c:out value='${appCssUrl}' />"></head>
<body><%@ include file="/WEB-INF/views/common/navbar.jspf" %><main id="main-content" class="ds-container ds-section ds-dashboard-shell"><%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content"><h1>Change password</h1><c:if test="${not empty formError}"><p role="alert"><c:out value="${formError}" /></p></c:if><%-- This block shows a form with its existing field names. StudentProfileController, StudentPasswordController or StudentProgressController supplies the page data. --%>
<form class="ds-card mt-6 max-w-xl" method="post" action="<c:url value='/student/password' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><label class="mb-5 block" for="current-password">Current password <span class="block" data-password-wrap><input class="block w-full" id="current-password" type="password" name="currentPassword" required autocomplete="current-password"><button class="ds-password-toggle" type="button" data-password-toggle="current-password" aria-label="Show current password" aria-pressed="false">Show</button></span></label><label class="mb-5 block" for="new-password">New password <span class="block" data-password-wrap><input class="block w-full" id="new-password" type="password" data-password-strength="new-strength" name="password" minlength="8" required autocomplete="new-password"><button class="ds-password-toggle" type="button" data-password-toggle="new-password" aria-label="Show new password" aria-pressed="false">Show</button></span><small id="new-strength" aria-live="polite">Use 8+ characters, with a letter and number.</small></label><label class="mb-5 block" for="confirm-new-password">Confirm new password <span class="block" data-password-wrap><input class="block w-full" id="confirm-new-password" type="password" name="confirmPassword" minlength="8" required autocomplete="new-password"><button class="ds-password-toggle" type="button" data-password-toggle="confirm-new-password" aria-label="Show password confirmation" aria-pressed="false">Show</button></span></label><button class="ds-button" type="submit">Save password</button></form></div></main><%@ include file="/WEB-INF/views/common/footer.jspf" %></body></html>
