<%--
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : list.jsp
WHAT IT DOES (simple words): Keeps reviews and reports details clear and safe for the school.
REAL-LIFE EXAMPLE: Like a feedback book and a school manager counting its records.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for reviews; reports READ saved data
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="Share feedback on completed lessons and manage your own reviews."><meta name="theme-color" content="#0B1120">
<title>Lesson reviews | DriveSpark Academy</title>
<link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
<link rel="stylesheet" href="<c:url value='/css/tailwind.css' />">
</head><body><%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-container ds-section ds-dashboard-shell">
<%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content">

<%-- ReviewController supplies role-filtered reviews and completed lessons that have no review yet. --%>
<header class="mb-8"><p class="ds-eyebrow">YOUR EXPERIENCE MATTERS</p><h1>Lesson reviews</h1>
<p class="text-slate-400">Share what helped you learn. Approved feedback helps the next learner choose.</p></header>
<c:if test="${navRole eq 'STUDENT'}">
<section class="ds-card mb-8" aria-labelledby="write-review-title">
<h2 id="write-review-title">Write a review</h2>
<c:choose><c:when test="${empty completedBookings}">
<p class="mt-4 text-slate-400">You have no completed lessons waiting for a review. Your existing feedback is below.</p>
</c:when><c:otherwise>
<form class="mt-5 grid gap-5" method="post" action="<c:url value='/reviews' />">
<input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />">
<div class="flex flex-col gap-2"><label for="review-lesson">Completed lesson</label><select class="w-full" id="review-lesson" name="bookingId" required>
<option value="">Choose a completed lesson</option>
<c:forEach items="${completedBookings}" var="completed">
<option value="<c:out value='${completed.id}' />"><c:out value="${completed.id}" /> - <c:out value="${completed.startTime.toLocalDate()}" /> at <c:out value="${completed.startTime.toLocalTime().withSecond(0).withNano(0)}" /></option>
</c:forEach></select></div>
<c:set var="ratingFieldName" value="rating" /><c:set var="ratingFieldId" value="new-review-rating" />
<c:set var="ratingLabel" value="How was your lesson?" /><c:set var="ratingValue" value="5" />
<%@ include file="/WEB-INF/views/common/components/star-input.jspf" %>
<div class="flex flex-col gap-2"><label for="review-comment">Your feedback</label><textarea class="w-full min-h-28" id="review-comment" name="comment" minlength="10" maxlength="1000" required aria-describedby="review-comment-help"></textarea>
<p id="review-comment-help" class="mt-2 text-xs text-slate-400">Write 10 to 1000 characters. New and edited reviews wait for approval.</p></div>
<div><button class="ds-button" type="submit">Submit review</button></div></form>
</c:otherwise></c:choose></section></c:if>
<%-- Each card comes from ReviewController; only the author gets edit and delete forms. --%>
<section aria-labelledby="visible-reviews-title"><h2 id="visible-reviews-title" class="mb-5">Visible feedback</h2>
<c:if test="${empty reviews}"><p class="ds-card">No feedback is available yet.</p></c:if>
<div class="grid gap-5 xl:grid-cols-2"><c:forEach items="${reviews}" var="review">
<article class="ds-feature-card" data-reveal>
<div class="mb-4 flex flex-wrap items-center justify-between gap-3"><c:set var="ratingValue" value="${review.rating}" /><%@ include file="/WEB-INF/views/common/components/star-display.jspf" %>
<span class="status-badge" data-status="<c:out value='${review.status}' />"><c:out value="${review.status}" /></span></div>
<p class="break-words"><c:out value="${review.comment}" /></p>
<p class="mt-4 text-xs text-slate-400"><c:out value="${review.id}" /> &middot; <time datetime="<c:out value='${review.createdAt}' />"><c:out value="${review.createdAt.toLocalDate()}" /></time>
<c:if test="${not empty review.bookingId}"> &middot; Lesson <c:out value="${review.bookingId}" /></c:if></p>
<c:if test="${navRole eq 'STUDENT' and review.studentId eq currentUserId}">
<details class="mt-5 border-t border-white/10 pt-4"><summary class="cursor-pointer font-semibold text-indigo-200">Edit your feedback</summary>
<form class="mt-4 grid gap-4" method="post" action="<c:url value='/reviews/update' />">
<input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="reviewId" value="<c:out value='${review.id}' />">
<c:set var="ratingFieldName" value="rating" /><c:set var="ratingFieldId" value="edit-${review.id}-rating" />
<c:set var="ratingLabel" value="Update your rating" /><c:set var="ratingValue" value="${review.rating}" />
<%@ include file="/WEB-INF/views/common/components/star-input.jspf" %>
<div class="flex flex-col gap-2"><label for="edit-comment-${review.id}">Update feedback</label><textarea class="w-full min-h-28" id="edit-comment-${review.id}" name="comment" minlength="10" maxlength="1000" required><c:out value="${review.comment}" /></textarea></div>
<div><button class="ds-button" type="submit">Save review</button></div></form>
<form class="mt-3" method="post" action="<c:url value='/reviews/delete' />" data-confirm="Delete your review? This removes your feedback.">
<input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="reviewId" value="<c:out value='${review.id}' />">
<button class="ds-button ds-button-outline" type="submit">Delete review</button></form></details>
</c:if></article></c:forEach></div></section>
</div></main><%@ include file="/WEB-INF/views/common/footer.jspf" %></body></html>
