<%--
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : lesson-dashboard.jsp
WHAT IT DOES (simple words): Keeps assigned lessons, student progress and instructor feedback together.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Not applicable (JSP presentation)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="theme-color" content="#050505">
<meta name="description" content="Review assigned lessons, record student progress and see your instructor profile and feedback.">
<title>Instructor dashboard | DriveSpark Academy</title>
<link rel="icon" href="<c:url value='/images/favicon.svg' />" type="image/svg+xml">
<link rel="stylesheet" href="<c:url value='/css/tailwind.css' />">
</head><body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-container ds-section ds-dashboard-shell">
    <%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %>
    <div class="ds-dashboard-content">
<%-- InstructorDashboardController supplies the welcome section and saved lesson totals. --%>
<header class="ds-workspace-heading"><p class="ds-eyebrow">YOUR INSTRUCTOR WORKSPACE</p><h1>My lesson dashboard</h1><p class="text-slate-400">Review your schedule, help students build skills and keep their progress up to date.</p></header>
<%-- InstructorDashboardController counts the saved assigned lessons for these cards. --%>
<%-- InstructorDashboardController supplies this page section. --%>
<section class="ds-workspace-overview ds-instructor-overview" aria-label="Lesson overview">
<%-- InstructorDashboardController supplies this record card. --%>
<article class="ds-card"><h2 class="text-lg">Today</h2><p class="ds-workspace-number" data-counter="<c:out value='${todayCount}' />"><c:out value="${todayCount}" /></p></article>
<%-- InstructorDashboardController supplies this record card. --%>
<article class="ds-card"><h2 class="text-lg">Upcoming dates</h2><p class="ds-workspace-number" data-counter="<c:out value='${upcomingCount}' />"><c:out value="${upcomingCount}" /></p></article>
<%-- InstructorDashboardController supplies this record card. --%>
<article class="ds-card"><h2 class="text-lg">Assigned students</h2><p class="ds-workspace-number" data-counter="<c:out value='${studentCount}' />"><c:out value="${studentCount}" /></p></article>
<%-- InstructorDashboardController supplies this record card. --%>
<article class="ds-card"><h2 class="text-lg">Approved rating</h2><p class="ds-workspace-number"><c:out value="${averageRating}" /> <span class="text-sm text-slate-400">/ 5</span></p></article>
</section>
<c:set var="tabsLabel" value="Instructor workspace sections" /><c:set var="firstTabTitle" value="Assigned lessons" /><c:set var="secondTabTitle" value="Profile and feedback" /><c:set var="firstPanelId" value="assigned-lessons" /><c:set var="secondPanelId" value="instructor-profile" />
<%@ include file="/WEB-INF/views/common/components/tabs.jspf" %>
<%-- InstructorDashboardController supplies the assigned bookings and safe student names. --%>
<%-- InstructorDashboardController supplies this page section. --%>
<section id="assigned-lessons" class="mt-6" aria-label="Assigned lessons">
<c:choose><c:when test="${empty bookings}"><div class="ds-empty-state"><h2>Your schedule is clear</h2><p>Assigned lessons will appear here for you to review.</p></div></c:when><c:otherwise><div class="grid gap-5">
<c:forEach items="${bookings}" var="booking">
<%-- InstructorDashboardController supplies this record card. --%>
<article class="ds-feature-card">
<div class="mb-4 flex flex-wrap items-start justify-between gap-3"><div><p class="ds-eyebrow"><c:out value="${booking.id}" /></p><h2><c:out value="${booking.startTime.toLocalDate()}" /></h2><p><c:out value="${booking.startTime.toLocalTime().withSecond(0).withNano(0)}" /> to <c:out value="${booking.endTime.toLocalTime().withSecond(0).withNano(0)}" /></p></div><span class="status-badge" data-status="<c:out value='${booking.status}' />"><c:out value="${booking.status}" /></span></div>
<p>Student: <c:out value="${studentDetails[booking.id].name}" /> (<c:out value="${booking.studentId}" />)</p><p>Vehicle: <c:out value="${booking.vehicleId}" default="Awaiting assignment" /></p>
<c:if test="${booking.status eq 'PENDING'}">
<div class="mt-5 grid gap-4 sm:grid-cols-2">
<%-- InstructorDashboardController sends acceptance to the booking rules for another conflict check. --%>
<%-- InstructorDashboardController supplies this form and its original field names. --%>
<form method="post" action="<c:url value='/instructor/accept' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="bookingId" value="<c:out value='${booking.id}' />"><button class="ds-button" type="submit">Accept lesson</button></form>
<%-- InstructorDashboardController requires a reason before declining an assigned lesson. --%>
<%-- InstructorDashboardController supplies this form and its original field names. --%>
<form method="post" action="<c:url value='/instructor/decline' />" data-confirm="Decline this assigned lesson?" data-confirm-reason="true"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="bookingId" value="<c:out value='${booking.id}' />"><label for="decline-${booking.id}">Reason for declining</label><input class="block w-full" id="decline-${booking.id}" name="reason" maxlength="300" required><button class="ds-button ds-button-danger mt-3" type="submit">Decline lesson</button></form>
</div></c:if>
<c:if test="${booking.status eq 'CONFIRMED'}"><div class="mt-5 flex flex-wrap gap-3">
<%-- InstructorDashboardController records attendance through the booking service. --%>
<%-- InstructorDashboardController supplies this form and its original field names. --%>
<form method="post" action="<c:url value='/instructor/complete' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="bookingId" value="<c:out value='${booking.id}' />"><button class="ds-button" type="submit">Mark completed</button></form>
<%-- InstructorDashboardController supplies this form and its original field names. --%>
<form method="post" action="<c:url value='/instructor/no-show' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="bookingId" value="<c:out value='${booking.id}' />"><button class="ds-button ds-button-outline" type="submit">Mark no-show</button></form>
</div></c:if>
<c:if test="${booking.status eq 'COMPLETED'}">
<details class="mt-5"><summary>Lesson notes and skill ratings</summary>
<%-- InstructorDashboardController supplies existing notes so editing never starts with blank saved feedback. --%>
<%-- InstructorDashboardController supplies this form and its original field names. --%>
<form class="mt-4 grid gap-4 sm:grid-cols-2" method="post" action="<c:url value='/instructor/lesson-notes' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="bookingId" value="<c:out value='${booking.id}' />">
<div class="sm:col-span-2"><label for="notes-${booking.id}">Instructor notes</label><textarea class="block w-full" id="notes-${booking.id}" name="notes" maxlength="2000" required><c:out value="${lessonRecords[booking.id].notes}" /></textarea></div>
<div><label for="clutchControl-${booking.id}">Clutch control (1 to 5)</label><input class="block w-full" id="clutchControl-${booking.id}" type="number" name="clutchControl" min="1" max="5" value="<c:out value="${lessonRecords[booking.id].skillRatings['Clutch control']}" />" required></div>
<div><label for="parking-${booking.id}">Parking (1 to 5)</label><input class="block w-full" id="parking-${booking.id}" type="number" name="parking" min="1" max="5" value="<c:out value="${lessonRecords[booking.id].skillRatings['Parking']}" />" required></div>
<div><label for="roadSigns-${booking.id}">Road signs (1 to 5)</label><input class="block w-full" id="roadSigns-${booking.id}" type="number" name="roadSigns" min="1" max="5" value="<c:out value="${lessonRecords[booking.id].skillRatings['Road signs']}" />" required></div>
<div><label for="laneDiscipline-${booking.id}">Lane discipline (1 to 5)</label><input class="block w-full" id="laneDiscipline-${booking.id}" type="number" name="laneDiscipline" min="1" max="5" value="<c:out value="${lessonRecords[booking.id].skillRatings['Lane discipline']}" />" required></div>
<label class="sm:col-span-2"><input type="checkbox" name="readyForTest" value="true" <c:if test="${lessonRecords[booking.id].readyForTest}">checked</c:if>> Ready for test</label>
<div class="sm:col-span-2"><button class="ds-button" type="submit">Save lesson record</button></div>
</form></details></c:if>
</article></c:forEach></div></c:otherwise></c:choose>
</section>
<%-- InstructorDashboardController supplies the instructor profile and approved-only rating counts. --%>
<%-- InstructorDashboardController supplies this page section. --%>
<section id="instructor-profile" class="mt-6" aria-label="Profile and feedback">
<div class="grid gap-5 lg:grid-cols-2">
<%-- InstructorDashboardController supplies this record card. --%>
<article class="ds-card"><p class="ds-eyebrow">MY INSTRUCTOR PROFILE</p><h2><c:out value="${instructor.name}" /></h2><p class="mt-3"><c:out value="${instructor.bio}" default="Your instructor profile is managed by the academy." /></p><dl class="mt-5 space-y-3"><div><dt class="text-sm text-slate-400">Experience</dt><dd><c:out value="${instructor.yearsOfExperience}" /> years</dd></div><div><dt class="text-sm text-slate-400">Qualifications</dt><dd><c:out value="${instructor.qualifications}" default="Not recorded" /></dd></div><div><dt class="text-sm text-slate-400">Vehicle types taught</dt><dd><c:forEach items="${instructor.vehicleTypes}" var="type"><span class="status-badge"><c:out value="${type}" /></span> </c:forEach></dd></div></dl><a class="ds-button mt-5" href="<c:url value='/instructor/availability' />">Manage working hours</a></article>
<%-- InstructorDashboardController supplies this record card. --%>
<article class="ds-card"><h2>Approved student ratings</h2><p class="mb-4"><c:out value="${approvedReviewCount}" /> approved reviews</p><c:forEach begin="1" end="5" var="star"><div class="mb-3"><div class="flex justify-between text-sm text-slate-300"><span><c:out value="${star}" /> stars</span><span><c:out value="${ratingCounts[star]}" /></span></div><progress class="ds-rating-progress" value="${ratingCounts[star]}" max="${approvedReviewCount eq 0 ? 1 : approvedReviewCount}" aria-label="${star} star reviews"></progress></div></c:forEach></article>
</div>
<h2 class="mb-5 mt-8">My student reviews</h2>
<c:choose><c:when test="${empty ownReviews}"><div class="ds-empty-state"><p>No reviews have been submitted for your lessons.</p></div></c:when><c:otherwise><div class="grid gap-4"><c:forEach items="${ownReviews}" var="review"><%-- InstructorDashboardController supplies this record card. --%>
<article class="ds-card"><p><c:out value="${review.rating}" /> / 5 <span class="status-badge" data-status="<c:out value='${review.status}' />"><c:out value="${review.status}" /></span></p><p class="mt-3 whitespace-pre-line text-slate-300"><c:out value="${review.comment}" /></p></article></c:forEach></div></c:otherwise></c:choose>
</section>
    </div>
</main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body></html>
