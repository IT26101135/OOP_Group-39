<%-- Module owner: IT26101135 --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url value="/css/tailwind.css" var="appCssUrl" />
<c:url value="/student/profile" var="studentProfileUrl" />
<c:url value="/student/profile/edit" var="studentProfileEditUrl" />
<c:url value="/student/password" var="studentPasswordUrl" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="description" content="My profile | DriveSpark Academy at DriveSpark Academy. Explore lessons, learning resources and driving school information.">
    <meta name="theme-color" content="#0B1120">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>My profile | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
</head>
<body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-container ds-section ds-profile ds-dashboard-shell"><%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content">
    <h1>My profile</h1>
    <c:if test="${passwordChanged eq true}"><p role="status">Password changed.</p></c:if>
    <p>Your student account details.</p>
    <c:if test="${profileUpdated eq true}">
        <p class="ds-profile-message" role="status">Your profile has been saved.</p>
    </c:if>
    <dl class="ds-profile-details">
        <dt>Student ID</dt><dd><c:out value="${profile.id}" /></dd>
        <dt>Full name</dt><dd><c:out value="${profile.name}" /></dd>
        <dt>Email</dt><dd><c:out value="${profile.email}" /></dd>
        <dt>Phone number</dt><dd><c:out value="${profile.phone}" /></dd>
        <dt>NIC</dt><dd><c:out value="${profile.nic}" /></dd>
        <dt>Date of birth</dt><dd><c:out value="${profile.dateOfBirth}" /></dd>
        <dt>Address</dt><dd><c:out value="${profile.address}" /></dd>
        <dt>License type wanted</dt><dd><c:out value="${profile.licenseTypeWanted}" /></dd>
        <dt>Emergency contact</dt><dd><c:out value="${profile.emergencyContact}" /></dd>
        <dt>Active package</dt><dd><c:out value="${profile.activePackageId}" /></dd>
        <dt>Lessons remaining</dt><dd><c:out value="${profile.lessonsRemaining}" /></dd>
    </dl>
    <a class="ds-button" href="<c:out value='${studentProfileEditUrl}' />">Edit profile</a>
    <a class="ds-button" href="<c:out value='${studentPasswordUrl}' />">Change password</a>
    <form method="post" action="<c:url value='/student/account/delete' />"
          data-confirm="Delete this account? This cannot be undone.">
        <input type="hidden" name="<c:out value='${csrfParameterName}' />"
               value="<c:out value='${csrfToken}' />">
        <button type="submit">Delete my account</button>
    </form>
</div></main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
