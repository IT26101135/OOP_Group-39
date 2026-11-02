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
    <title>Edit profile | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
    <link rel="stylesheet" href="<c:out value='${profileCssUrl}' />">
</head>
<body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main class="ds-container ds-section ds-profile">
    <h1>Edit profile</h1>
    <p>Update your name, email address and phone number.</p>
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
        <div class="ds-profile-actions">
            <button class="ds-button" type="submit">Save changes</button>
            <a href="<c:out value='${studentProfileUrl}' />">Cancel</a>
        </div>
    </form>
</main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
