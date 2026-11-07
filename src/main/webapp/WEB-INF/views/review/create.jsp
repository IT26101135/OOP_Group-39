<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url value="/css/app.css" var="appCssUrl" />
<c:url value="/css/review.css" var="reviewCssUrl" />
<c:url value="/reviews/create" var="createReviewUrl" />
<c:url value="/reviews" var="reviewsUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Review - DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
    <link rel="stylesheet" href="<c:out value='${reviewCssUrl}' />">
</head>
<body class="review-page">

<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main class="ds-container ds-section">

    <section class="form-section">

        <h1>Write a Review</h1>

        <p>Share your experience with DriveSpark Academy.</p>

        <c:if test="${not empty formError}">
            <div class="error-message" role="alert">
                <c:out value="${formError}" />
            </div>
        </c:if>

        <form method="post"
              action="<c:out value='${createReviewUrl}' />">

            <!-- CSRF Protection -->
            <input type="hidden"
                   name="<c:out value='${csrfParameterName}' />"
                   value="<c:out value='${csrfToken}' />">

            <!-- Rating -->
            <div class="form-group">

                <label for="rating">Rating</label>

                <select id="rating"
                        name="rating"
                        required>

                    <option value="">Select a rating</option>

                    <option value="1"
                            <c:if test="${reviewForm.rating == 1}">selected</c:if>>
                        1 - Poor
                    </option>

                    <option value="2"
                            <c:if test="${reviewForm.rating == 2}">selected</c:if>>
                        2 - Fair
                    </option>

                    <option value="3"
                            <c:if test="${reviewForm.rating == 3}">selected</c:if>>
                        3 - Good
                    </option>

                    <option value="4"
                            <c:if test="${reviewForm.rating == 4}">selected</c:if>>
                        4 - Very Good
                    </option>

                    <option value="5"
                            <c:if test="${reviewForm.rating == 5}">selected</c:if>>
                        5 - Excellent
                    </option>

                </select>

            </div>

            <!-- Comment -->
            <div class="form-group">

                <label for="comment">Your Review</label>

                <textarea id="comment"
                          name="comment"
                          rows="6"
                          minlength="10"
                          maxlength="1000"
                          required
                          placeholder="Write your experience here..."><c:out value="${reviewForm.comment}" /></textarea>

                <small>Enter between 10 and 1000 characters.</small>

            </div>

            <!-- Buttons -->
            <div class="form-actions">

                <button type="submit" class="ds-button">Submit Review</button>

                <a href="<c:out value='${reviewsUrl}' />">
                    Cancel
                </a>

            </div>

        </form>

    </section>

</main>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>

</body>
</html>