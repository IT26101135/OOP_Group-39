<%-- Module owner: IT26101135 --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url value="/css/tailwind.css" var="appCssUrl" />
<c:url value="/student/profile" var="studentProfileUrl" />
<c:url value="/student/profile/edit" var="studentProfileEditUrl" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="description" content="Edit profile | DriveSpark Academy at DriveSpark Academy. Explore lessons, learning resources and driving school information.">
    <meta name="theme-color" content="#0B1120">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Edit profile | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
</head>
<body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-container ds-section ds-profile ds-dashboard-shell"><%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content">
    <h1>Edit profile</h1>
    <p>Update your contact and learning details.</p>
    <p>Use your updated email address the next time you log in.</p>
    <c:if test="${not empty formError}">
        <p class="ds-profile-error" role="alert"><c:out value="${formError}" /></p>
    </c:if>
    <form class="ds-profile-form" method="post"
          action="<c:out value='${studentProfileEditUrl}' />">
        <input type="hidden" name="<c:out value='${csrfParameterName}' />"
               value="<c:out value='${csrfToken}' />">
        <p>
            <label for="profile-name">Full name</label>
            <input id="profile-name" name="name" type="text" autocomplete="name"
                   required minlength="2" maxlength="100"
                   value="<c:out value='${profileForm.name}' />">
        </p>
        <p>
            <label for="profile-email">Email</label>
            <input id="profile-email" name="email" type="email" autocomplete="email"
                   required maxlength="254"
                   value="<c:out value='${profileForm.email}' />">
        </p>
        <p>
            <label for="profile-phone">Phone number</label>
            <input id="profile-phone" name="phone" type="tel" autocomplete="tel"
                   required maxlength="16" aria-describedby="profile-phone-help"
                   value="<c:out value='${profileForm.phone}' />">
            <small id="profile-phone-help">Use 7-15 digits, optionally starting with +.</small>
        </p>
        <p><label for="profile-nic">NIC</label>
            <input id="profile-nic" name="nic" maxlength="12" value="<c:out value='${profileForm.nic}' />"></p>
        <p><label for="profile-dob">Date of birth</label>
            <input id="profile-dob" name="dateOfBirth" type="date" value="<c:out value='${profileForm.dateOfBirth}' />"></p>
        <p><label for="profile-address">Address</label>
            <textarea id="profile-address" name="address" maxlength="500"><c:out value="${profileForm.address}" /></textarea></p>
        <p><label for="profile-license">License type wanted</label>
            <input id="profile-license" name="licenseTypeWanted" maxlength="80" value="<c:out value='${profileForm.licenseTypeWanted}' />"></p>
        <p><label for="profile-emergency">Emergency contact</label>
            <input id="profile-emergency" name="emergencyContact" maxlength="120" value="<c:out value='${profileForm.emergencyContact}' />"></p>
        <div class="ds-profile-actions">
            <button class="ds-button" type="submit">Save changes</button>
            <a href="<c:out value='${studentProfileUrl}' />">Cancel</a>
        </div>
    </form>
</div></main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
