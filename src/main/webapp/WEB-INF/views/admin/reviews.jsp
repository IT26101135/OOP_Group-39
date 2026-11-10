<%--
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : reviews.jsp
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
<meta name="description" content="Approve or hide student lesson feedback and filter by moderation status."><meta name="theme-color" content="#0B1120">
<title>Review student feedback | DriveSpark Academy</title>
<link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
<link rel="stylesheet" href="<c:url value='/css/tailwind.css' />">
</head><body class="ds-account-page"><%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-container ds-section ds-dashboard-shell">
<%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content">

<%-- AdminReviewController supplies reviews filtered by the requested moderation status. --%>
<header class="mb-8"><p class="ds-eyebrow">LEARNER FEEDBACK</p><h1>Review student feedback</h1>
<p class="text-slate-400">Review feedback before it becomes public. Only approved reviews affect instructor ratings.</p></header>
<%@ include file="/WEB-INF/views/common/components/review-tabs.jspf" %>
<c:if test="${empty reviews}"><p class="ds-card mt-6">No reviews match this filter.</p></c:if>
<section class="mt-6 grid gap-5 xl:grid-cols-2" aria-label="Reviews for moderation">
<c:forEach items="${reviews}" var="review"><article class="ds-feature-card" data-reveal>
<div class="mb-4 flex flex-wrap items-center justify-between gap-3"><c:set var="ratingValue" value="${review.rating}" /><%@ include file="/WEB-INF/views/common/components/star-display.jspf" %>
<span class="status-badge" data-status="<c:out value='${review.status}' />"><c:out value="${review.status eq 'PENDING' ? 'Awaiting approval' : publicDisplay.label(review.status)}" /></span></div>
<p class="break-words"><c:out value="${review.comment}" /></p>
<dl class="my-5 grid grid-cols-2 gap-3 text-sm">
<div><dt class="text-slate-400">Review</dt><dd><c:out value="${'Student feedback'}" /></dd></div>
<div><dt class="text-slate-400">Student</dt><dd><c:out value="${accountDisplay.person(review.studentId)}" /></dd></div>
<div><dt class="text-slate-400">Lesson</dt><dd><c:out value="${accountDisplay.lesson(review.bookingId)}" default="Legacy feedback" /></dd></div>
<div><dt class="text-slate-400">Instructor</dt><dd><c:out value="${accountDisplay.person(review.instructorId)}" default="Not linked" /></dd></div>
</dl>
<%-- These forms keep AdminReviewController routes and CSRF field names unchanged. --%>
<div class="flex flex-wrap gap-3 border-t border-white/10 pt-4">
<form method="post" action="<c:url value='/admin/reviews/approve' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="reviewId" value="<c:out value='${review.id}' />"><button class="ds-button" type="submit">Approve</button></form>
<form method="post" action="<c:url value='/admin/reviews/hide' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="reviewId" value="<c:out value='${review.id}' />"><button class="ds-button ds-button-outline" type="submit">Hide</button></form>
<form method="post" action="<c:url value='/admin/reviews/delete' />" data-confirm="Delete this review?"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="reviewId" value="<c:out value='${review.id}' />"><button class="ds-button ds-button-outline" type="submit">Delete</button></form>
</div></article></c:forEach></section>
</div></main><%@ include file="/WEB-INF/views/common/footer.jspf" %></body></html>
