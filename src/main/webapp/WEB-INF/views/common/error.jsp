<%-- Module owner: IT26101136 --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url value="/css/tailwind.css" var="appCssUrl" />
<c:url value="/" var="homeUrl" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="description" content="DriveSpark Academy - Error at DriveSpark Academy. Explore lessons, learning resources and driving school information.">
    <meta name="theme-color" content="#0B1120">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>DriveSpark Academy - Error</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
</head>
<body>
<main id="main-content" class="ds-error-shell">
    <div class="ds-error-art"><img src="<c:url value='/images/error-road.svg' />" width="360" height="220" alt="A quiet road under a soft indigo glow"></div>
    <p class="ds-eyebrow">DRIVESPARK ACADEMY</p>
    <h1><c:out value="${title}"/></h1>

    <p>
        Error code:
        <strong><c:out value="${statusCode}"/></strong>
    </p>

    <p><c:out value="${message}"/></p>

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
