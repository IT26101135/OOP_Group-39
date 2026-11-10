<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url value="/css/app.css" var="appCssUrl" />
<c:url value="/css/instructor.css" var="instructorCssUrl" />
<c:url value="/admin/instructors/create" var="createInstructorUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Manage instructors | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
    <link rel="stylesheet" href="<c:out value='${instructorCssUrl}' />">
</head>
<body>

<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main class="ds-container ds-section ds-instructor">

    <div class="ds-instructor-heading">
        <div>
            <p class="ds-instructor-eyebrow">ADMINISTRATION</p>
            <h1>Manage instructors</h1>
            <p class="ds-instructor-description">
                View instructor records and manage instructor availability.
            </p>
        </div>

        <a class="ds-button"
           href="<c:out value='${createInstructorUrl}' />">
            Add instructor
        </a>
    </div>

    <c:if test="${instructorCreated}">
        <p class="ds-instructor-message" role="status">
            Instructor created successfully.
        </p>
    </c:if>

    <c:if test="${instructorUpdated}">
        <p class="ds-instructor-message" role="status">
            Instructor updated successfully.
        </p>
    </c:if>

    <c:if test="${availabilityCreated}">
        <p class="ds-instructor-message" role="status">
            Instructor availability added successfully.
        </p>
    </c:if>

    <section class="ds-instructor-panel"
             aria-labelledby="instructor-list-title">

        <div class="ds-instructor-panel-heading">
            <h2 id="instructor-list-title">Instructor records</h2>
            <p>Sorted by experience: High to Low</p>
        </div>

        <c:choose>
            <c:when test="${empty instructors}">
                <div class="ds-instructor-empty">
                    <h3>No instructors to display</h3>
                    <p>
                        There are currently no instructor records available.
                    </p>
                    <a href="<c:out value='${createInstructorUrl}' />">
                        Add the first instructor
                    </a>
                </div>
            </c:when>

            <c:otherwise>
                <div class="ds-instructor-table-wrapper">
                    <table class="ds-instructor-table">
                        <thead>
                        <tr>
                            <th scope="col">Instructor ID</th>
                            <th scope="col">Name</th>
                            <th scope="col">Email</th>
                            <th scope="col">Phone</th>
                            <th scope="col">Experience</th>
                            <th scope="col">Status</th>
                            <th scope="col">Actions</th>
                        </tr>
                        </thead>

                        <tbody>
                        <c:forEach items="${instructors}" var="instructor">
                            <tr>
                                <td data-label="Instructor ID">
                                    <strong>
                                        <c:out value="${instructor.id}" />
                                    </strong>
                                </td>

                                <td data-label="Name">
                                    <c:out value="${instructor.name}" />
                                </td>

                                <td data-label="Email">
                                    <c:out value="${instructor.email}" />
                                </td>

                                <td data-label="Phone">
                                    <c:out value="${instructor.phone}" />
                                </td>

                                <td data-label="Experience">
                                    <c:out value="${instructor.yearsOfExperience}" />
                                    years
                                </td>

                                <td data-label="Status">
                                    <c:choose>
                                        <c:when test="${instructor.active}">
                                            <span class="ds-instructor-status">
                                                Active
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="ds-instructor-status ds-instructor-inactive">
                                                Inactive
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td data-label="Actions">
                                    <div class="ds-instructor-actions">
                                        <c:url value="/admin/instructors/${instructor.id}/edit"
                                               var="editInstructorUrl" />

                                        <a href="<c:out value='${editInstructorUrl}' />">
                                            Edit
                                        </a>

                                        <c:if test="${instructor.active}">
                                            <c:url value="/admin/instructors/${instructor.id}/availability/create"
                                                   var="availabilityUrl" />

                                            <a href="<c:out value='${availabilityUrl}' />">
                                                Add availability
                                            </a>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>

    </section>
</main>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>

</body>
</html>