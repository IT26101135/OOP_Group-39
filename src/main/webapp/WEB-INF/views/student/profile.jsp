<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url value="/css/app.css" var="appCssUrl" />
<c:url value="/css/profile.css" var="profileCssUrl" />
<c:url value="/student/profile" var="studentProfileUrl" />
<c:url value="/student/profile/edit" var="studentProfileEditUrl" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>My profile | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
    <link rel="stylesheet" href="<c:out value='${profileCssUrl}' />">
</head>
<body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main class="ds-container ds-section ds-profile">
    <h1>My profile</h1>
    <p>Your student account details.</p>
    <c:if test="${profileUpdated eq true}">
        <p class="ds-profile-message" role="status">Your profile has been saved.</p>
    </c:if>
    <dl class="ds-profile-details">
        <dt>Student ID</dt><dd><c:out value="${profile.id}" /></dd>
        <dt>Full name</dt><dd><c:out value="${profile.name}" /></dd>
        <dt>Email</dt><dd><c:out value="${profile.email}" /></dd>
        <dt>Phone number</dt><dd><c:out value="${profile.phone}" /></dd>
    </dl>
    <a class="ds-button" href="<c:out value='${studentProfileEditUrl}' />">Edit profile</a>
</main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
