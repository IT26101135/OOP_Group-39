<%--
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Shared UI
FILE     : error.jsp
WHAT IT DOES (simple words): Shows a friendly application error and a safe way to continue.
REAL-LIFE EXAMPLE: A receptionist explains a problem without showing private records.
OOP USED : Displays the result of shared exception handling
CRUD     : Supports error messages for CREATE, READ, UPDATE and DELETE
============================================================
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url value="/css/tailwind.css" var="appCssUrl" />
<c:url value="/" var="homeUrl" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="description" content="DriveSpark Academy - Error at DriveSpark Academy. Explore lessons, learning resources and driving school information.">
    <meta name="theme-color" content="#050505">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>DriveSpark Academy - Error</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
</head>
<body>
<a class="skip-link" href="#main-content">Skip to error message</a>
<%-- GlobalExceptionHandler supplies the title, status and safe message for this section. --%>
<%-- The error handler supplies this message; a focusable main region lets the skip link work without scripts. --%>
<main id="main-content" tabindex="-1" class="ds-error-shell">
    <div class="ds-error-art"><img src="<c:url value='/images/error-road.svg' />" width="360" height="220" alt="A quiet road illustration"></div>
    <p class="ds-eyebrow">DRIVESPARK ACADEMY</p>
    <h1><c:out value="${title}"/></h1>

    <p>
        Error code:
        <strong><c:out value="${statusCode}"/></strong>
    </p>

    <p><c:out value="${message}"/></p>
    <%-- Booking rules explain payment problems here; this link gives students a useful next step. --%>
    <p><a class="ds-home-text-link" href="<c:url value='/enrollments' />">My enrollments and payment instructions</a></p>

    <p>The page could not be opened. Return home and continue from there.</p>
    <c:choose>
        <c:when test="${statusCode eq 403 and navRole eq 'ADMIN'}"><a class="ds-button" href="<c:url value='/admin/dashboard' />">Go to my dashboard</a></c:when>
        <c:when test="${statusCode eq 403 and navRole eq 'INSTRUCTOR'}"><a class="ds-button" href="<c:url value='/instructor/dashboard' />">Go to my dashboard</a></c:when>
        <c:when test="${statusCode eq 403 and navRole eq 'STUDENT'}"><a class="ds-button" href="<c:url value='/student/profile' />">Go to my dashboard</a></c:when>
        <c:otherwise><a class="ds-button" href="<c:out value='${homeUrl}' />">Back to home</a></c:otherwise>
    </c:choose>
</main>
</body>
</html>
