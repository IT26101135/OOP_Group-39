<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url value="/css/app.css" var="appCssUrl" />
<c:url value="/css/review.css" var="reviewCssUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Reviews - DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
    <link rel="stylesheet" href="<c:out value='${reviewCssUrl}' />">
</head>
<body class="review-page">

<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main class="ds-container ds-section">

    <section class="form-section review-list">

        <h1>Manage Reviews</h1>

        <p>View and moderate student reviews.</p>
        <c:if test="${not empty moderationSuccess}">
            <p class="success-message" role="status"><c:out value="${moderationSuccess}" /></p>
        </c:if>

        <c:if test="${empty reviews}">
            <p>No reviews available.</p>
        </c:if>

        <c:if test="${not empty reviews}">

            <c:forEach var="review" items="${reviews}">

                <article class="review-card">

                    <h3>Review ID:
                        <c:out value="${review.id}" />
                    </h3>

                    <p>
                        <strong>Student ID:</strong>
                        <c:out value="${review.studentId}" />
                    </p>

                    <p>
                        <strong>Rating:</strong>
                        <c:out value="${review.rating}" /> / 5
                    </p>

                    <p>
                        <strong>Comment:</strong>
                    </p>

                    <p>
                        <c:out value="${review.comment}" />
                    </p>

                    <p>
                        <strong>Status:</strong>
                        <span class="review-status"><c:out value="${review.status}" /></span>
                    </p>

                    <p>
                        <strong>Created At:</strong>
                        <c:out value="${review.createdAt}" />
                    </p>

                    <c:url value="/admin/reviews/${review.id}/approve"
                           var="approveUrl" />

                    <c:url value="/admin/reviews/${review.id}/hide"
                           var="hideUrl" />

                    <div class="form-actions">

                        <c:if test="${review.status != 'APPROVED'}">

                            <form method="post" action="<c:out value='${approveUrl}' />">

                                <input type="hidden"
                                       name="<c:out value='${csrfParameterName}' />"
                                       value="<c:out value='${csrfToken}' />">

                                <button type="submit" class="ds-button">
                                    Approve
                                </button>

                            </form>

                        </c:if>

                        <c:if test="${review.status != 'HIDDEN'}">

                            <form method="post" action="<c:out value='${hideUrl}' />">

                                <input type="hidden"
                                       name="<c:out value='${csrfParameterName}' />"
                                       value="<c:out value='${csrfToken}' />">

                                <button type="submit" class="ds-button">
                                    Hide
                                </button>

                            </form>

                        </c:if>

                    </div>

                </article>

            </c:forEach>

        </c:if>

    </section>

</main>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>

</body>
</html>