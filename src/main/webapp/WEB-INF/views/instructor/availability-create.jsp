<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url value="/css/app.css" var="appCssUrl" />
<c:url value="/css/instructor.css" var="instructorCssUrl" />
<c:url value="/admin/instructors/${instructor.id}/availability/create" var="createAvailabilityUrl" />
<c:url value="/admin/instructors" var="instructorListUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Add instructor availability | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
    <link rel="stylesheet" href="<c:out value='${instructorCssUrl}' />">
</head>
<body>

<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main class="ds-container ds-section ds-instructor">

    <div class="ds-instructor-form-heading">
        <p class="ds-instructor-eyebrow">INSTRUCTOR MANAGEMENT</p>
        <h1>Add instructor availability</h1>
        <p class="ds-instructor-description">
            Set an available working interval for this instructor.
        </p>
    </div>

    <section class="ds-instructor-form-panel">

        <div class="ds-instructor-readonly">
            <h2>Instructor information</h2>

            <dl class="ds-instructor-details">
                <div>
                    <dt>Instructor ID</dt>
                    <dd><c:out value="${instructor.id}" /></dd>
                </div>

                <div>
                    <dt>Instructor name</dt>
                    <dd><c:out value="${instructor.name}" /></dd>
                </div>
            </dl>
        </div>

        <c:if test="${not empty formError}">
            <p class="ds-instructor-error" role="alert">
                <c:out value="${formError}" />
            </p>
        </c:if>

        <form class="ds-instructor-form"
              method="post"
              action="<c:out value='${createAvailabilityUrl}' />">

            <input type="hidden"
                   name="<c:out value='${csrfParameterName}' />"
                   value="<c:out value='${csrfToken}' />">

            <div class="ds-instructor-field">
                <label for="availability-start">
                    Start date and time
                </label>

                <input id="availability-start"
                       name="start"
                       type="datetime-local"
                       required
                       step="60"
                       value="<c:out value='${instructorAvailabilityForm.start}' />">

                <small>Sri Lanka local time (Asia/Colombo).</small>
            </div>

            <div class="ds-instructor-field">
                <label for="availability-end">
                    End date and time
                </label>

                <input id="availability-end"
                       name="end"
                       type="datetime-local"
                       required
                       step="60"
                       value="<c:out value='${instructorAvailabilityForm.end}' />">

                <small>End time must be later than start time.</small>
            </div>

            <p class="ds-instructor-form-note">
                This interval represents the instructor's available
                working time. All times use Sri Lanka local time
                (UTC+05:30).
            </p>

            <div class="ds-instructor-form-actions">
                <button class="ds-button" type="submit">
                    Add availability
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