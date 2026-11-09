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
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
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
    <%-- ErrorPageSupport supplies only known local form and role destinations. --%>
    <c:if test="${not empty retryUrl}"><p><a class="ds-button" href="<c:url value='${retryUrl}' />">Back to the form</a></p></c:if>
    <p><a class="ds-button ds-button-outline" href="<c:url value='${recoveryUrl}' />"><c:choose><c:when test="${navRole eq 'PUBLIC'}">Log in</c:when><c:otherwise>Go to my dashboard</c:otherwise></c:choose></a></p>
    <p><a class="ds-home-text-link" href="<c:out value='${homeUrl}' />">Back to home</a></p>
</main>
</body>
</html>
