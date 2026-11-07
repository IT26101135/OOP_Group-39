<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url value="/css/app.css" var="appCssUrl" />
<c:url value="/css/review.css" var="reviewCssUrl" />
<c:url value="/reviews/create" var="createReviewUrl" />

<c:set var="hasOwnReview" value="false" />

<c:forEach var="review" items="${reviews}">
    <c:if test="${review.studentId eq currentStudentId}">
        <c:set var="hasOwnReview" value="true" />
    </c:if>
</c:forEach>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Reviews | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
    <link rel="stylesheet" href="<c:out value='${reviewCssUrl}' />">
</head>
<body class="review-page">

<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main class="ds-container ds-section">

    <h1>Student Reviews</h1>

    <p>Read reviews about DriveSpark Academy.</p>

    <c:if test="${reviewSubmitted eq true}">
        <p class="success-message" role="status">Your review was submitted and is waiting for approval.</p>
    </c:if>

    <c:if test="${reviewUpdated eq true}">
        <p class="success-message" role="status">Your review was updated and is waiting for approval again.</p>
    </c:if>

    <c:if test="${reviewDeleted eq true}">
        <p class="success-message" role="status">Your review was deleted.</p>
    </c:if>

    <c:if test="${not hasOwnReview}">
        <a class="ds-button"
           href="<c:out value='${createReviewUrl}' />">
            Write a Review
        </a>
    </c:if>

    <hr>

    <c:choose>

        <c:when test="${empty reviews}">
            <p>No reviews are available yet.</p>
        </c:when>

        <c:otherwise>

            <c:forEach var="review" items="${reviews}">

                <article class="review-card">

                    <h2>Rating: <c:out value="${review.rating}" /> / 5</h2>

                    <p>
                        <c:out value="${review.comment}" />
                    </p>

                    <p>
                        Status:
                        <strong>
                            <c:out value="${review.status}" />
                        </strong>
                    </p>

                    <p>
                        Date:
                        <c:out value="${review.createdAt}" />
                    </p>

                    <c:if test="${review.studentId eq currentStudentId}">

                        <c:url value="/reviews/${review.id}/edit"
                               var="editReviewUrl" />

                        <c:url value="/reviews/${review.id}/delete"
                               var="deleteReviewUrl" />

                        <a href="<c:out value='${editReviewUrl}' />">
                            Edit
                        </a>

                        <form method="post"
                              action="<c:out value='${deleteReviewUrl}' />">

                            <input type="hidden"
                                   name="<c:out value='${csrfParameterName}' />"
                                   value="<c:out value='${csrfToken}' />">

                            <button type="submit" class="ds-button">
                                Delete
                            </button>

                        </form>

                    </c:if>

                </article>

                <hr>

            </c:forEach>

        </c:otherwise>

    </c:choose>

</main>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>

</body>
</html>