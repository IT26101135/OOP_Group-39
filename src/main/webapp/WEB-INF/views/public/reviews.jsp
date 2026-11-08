<%--
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : reviews.jsp
WHAT IT DOES (simple words): Shows learner feedback that the school has approved for everyone to read.
REAL-LIFE EXAMPLE: Like a public feedback book with private moderation notes kept elsewhere.
OOP USED : Not applicable (JSP presentation; the review service supplies approved records)
CRUD     : READ (displays approved reviews only)
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="Read approved learner feedback about DriveSpark Academy lessons and learning experiences.">
<meta name="theme-color" content="#050505"><title>Learner reviews | DriveSpark Academy</title>
<link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
<link rel="stylesheet" href="<c:url value='/css/tailwind.css' />">
</head><body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-public ds-container ds-reviews-public">
    <%-- PublicReviewController supplies approved feedback only; private moderation data stays off this page. --%>
    <header class="ds-public-heading"><p class="ds-eyebrow">THEIR JOURNEY. THEIR WORDS.</p><h1>Confidence,<br>one story at a time.</h1><p>Read the experiences learners have shared with the academy.</p></header>
    <c:if test="${empty reviews}"><section class="ds-public-empty"><h2>Stories are on the way.</h2><p>Approved learner feedback will appear here when available.</p></section></c:if>
    <%-- PublicReviewController provides each review. These class names also support the home-page reader. --%>
    <div class="ds-public-review-list">
        <c:forEach items="${reviews}" var="review">
            <article class="ds-review-card" data-reveal>
                <c:set var="ratingValue" value="${review.rating}" /><%@ include file="/WEB-INF/views/common/components/star-display.jspf" %>
                <blockquote><c:out value="${review.comment}" /></blockquote>
                <p class="ds-review-meta">DriveSpark learner &middot; <time datetime="<c:out value='${review.createdAt}' />"><c:out value="${review.createdAt.toLocalDate()}" /></time></p>
            </article>
        </c:forEach>
    </div>
    <%-- PublicReviewController shows this project note without inventing aggregate ratings or review counts. --%>
    <p class="ds-public-note">This university project may include sample learner reviews. Only approved feedback is displayed.</p>
    <section class="ds-public-closing"><div><p class="ds-eyebrow">YOUR STORY STARTS HERE</p><h2>Take your next turn.</h2></div><a class="ds-button ds-button-outline" href="<c:url value='/packages' />">Explore packages</a></section>
</main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body></html>
