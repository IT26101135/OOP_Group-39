<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url value="/css/app.css" var="appCssUrl" />
<c:url value="/css/instructor.css" var="instructorCssUrl" />
<c:url value="/admin/instructors/create" var="createInstructorUrl" />
<c:url value="/admin/instructors" var="instructorListUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Add instructor | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
    <link rel="stylesheet" href="<c:out value='${instructorCssUrl}' />">
</head>
<body>

<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main class="ds-container ds-section ds-instructor">

    <div class="ds-instructor-form-heading">
        <p class="ds-instructor-eyebrow">INSTRUCTOR MANAGEMENT</p>
        <h1>Add an instructor</h1>
        <p class="ds-instructor-description">
            Enter the instructor details to create a new record.
        </p>
    </div>

    <section class="ds-instructor-form-panel">

        <c:if test="${not empty formError}">
            <p class="ds-instructor-error" role="alert">
                <c:out value="${formError}" />
            </p>
        </c:if>

        <form class="ds-instructor-form"
              method="post"
              action="<c:out value='${createInstructorUrl}' />">

            <input type="hidden"
                   name="<c:out value='${csrfParameterName}' />"
                   value="<c:out value='${csrfToken}' />">

            <div class="ds-instructor-field">
                <label for="instructor-name">Full name</label>
                <input id="instructor-name"
                       name="name"
                       type="text"
                       required
                       minlength="2"
                       maxlength="100"
                       autocomplete="name"
                       value="<c:out value='${instructorCreateForm.name}' />">
                <small>Enter 2 to 100 characters.</small>
            </div>

            <div class="ds-instructor-field">
                <label for="instructor-email">Email address</label>
                <input id="instructor-email"
                       name="email"
                       type="email"
                       required
                       maxlength="254"
                       autocomplete="email"
                       value="<c:out value='${instructorCreateForm.email}' />">
            </div>

            <div class="ds-instructor-field">
                <label for="instructor-phone">Phone number</label>
                <input id="instructor-phone"
                       name="phone"
                       type="tel"
                       required
                       pattern="\+?[0-9]{7,15}"
                       autocomplete="tel"
                       placeholder="+94771234567"
                       value="<c:out value='${instructorCreateForm.phone}' />">
                <small>7 to 15 digits, optionally starting with +.</small>
            </div>

            <div class="ds-instructor-field">
                <label for="instructor-experience">
                    Years of experience
                </label>
                <input id="instructor-experience"
                       name="yearsOfExperience"
                       type="number"
                       required
                       min="0"
                       max="60"
                       step="1"
                       value="<c:out value='${instructorCreateForm.yearsOfExperience}' />">
                <small>Enter a number between 0 and 60.</small>
            </div>

            <p class="ds-instructor-form-note">
                Instructor ID will be generated automatically.
                New instructors are created with active status.
            </p>

            <div class="ds-instructor-form-actions">
                <button class="ds-button" type="submit">
                    Create instructor
                </button>

                <a href="<c:out value='${instructorListUrl}' />">
                    Cancel
                </a>
            </div>

        </form>
    </section>
</main>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>

</body>
</html>