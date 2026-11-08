<%--
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Shared UI
FILE     : setup-check.jsp
WHAT IT DOES (simple words): Shows a small page confirming that the project has started.
REAL-LIFE EXAMPLE: A reception sign tells the team that the office is open.
OOP USED : Not applicable (JSP presentation)
CRUD     : READ (shows setup information from SetupController)
============================================================
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url value="/css/tailwind.css" var="appCssUrl" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="description" content="DriveSpark Academy - Setup Check at DriveSpark Academy. Explore lessons, learning resources and driving school information.">
    <meta name="theme-color" content="#050505">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>DriveSpark Academy - Setup Check</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
</head>
<body>
<a class="skip-link" href="#main-content">Skip to setup information</a>
<%-- SetupController supplies the project name and setup message. --%>
<main id="main-content" tabindex="-1" class="ds-container ds-section">
    <h1><c:out value="${projectName}"/></h1>
    <p><c:out value="${message}"/></p>
    <p>Phase 0: project setup.</p>
</main>
</body>
</html>
