<%--
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : availability.jsp
WHAT IT DOES (simple words): Shows availability information and forms.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Not applicable (JSP presentation)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>My availability | DriveSpark Academy</title><meta name="description" content="Manage weekly teaching hours and planned leave without losing historical records.">
<link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />"><link rel="stylesheet" href="<c:url value='/css/tailwind.css' />"></head><body class="ds-account-page">
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-container ds-section ds-dashboard-shell">
    <%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content">
<p class="ds-eyebrow">PLAN YOUR TEACHING WEEK</p><h1>Working hours and leave</h1><p class="mb-7 text-slate-400">Set regular teaching hours and keep planned days off up to date. Confirmed lessons must be rescheduled before affected hours can be edited.</p>
<%-- InstructorAvailabilityController supplies the day choices and accepts working hours. --%>
<%-- InstructorAvailabilityController supplies this page section. --%>
<section class="ds-card mb-7" aria-labelledby="new-hours"><h2 id="new-hours">Add working hours</h2>
<%-- InstructorAvailabilityController supplies this form and its original field names. --%>
<form class="mt-5 grid gap-4 sm:grid-cols-3" method="post" action="<c:url value='/instructor/availability' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />">
<div><label for="working-day">Day</label><select class="block w-full" id="working-day" name="dayOfWeek"><c:forEach items="${days}" var="day"><option value="${day}"><c:out value="${publicDisplay.label(day)}" /></option></c:forEach></select></div>
<div><label for="working-start">Start time</label><input class="block w-full" id="working-start" type="time" name="startTime" required></div><div><label for="working-end">End time</label><input class="block w-full" id="working-end" type="time" name="endTime" required></div>
<div class="sm:col-span-3"><button class="ds-button" type="submit">Add availability</button></div></form></section>
<%-- InstructorAvailabilityController supplies only the signed-in instructor's saved hours. --%>
<%-- InstructorAvailabilityController supplies this page section. --%>
<section class="mb-8" aria-labelledby="saved-hours"><h2 id="saved-hours" class="mb-5">Weekly availability</h2>
<c:choose><c:when test="${empty availability}"><div class="ds-empty-state"><p>No working hours are set. Add the days and times you can teach.</p><a class="ds-home-text-link" href="#new-hours">Add working hours</a></div></c:when><c:otherwise><div class="grid gap-4"><c:forEach items="${availability}" var="block">
<%-- InstructorAvailabilityController supplies this record card. --%>
<article class="ds-card"><h3 class="text-lg"><c:out value="${publicDisplay.label(block.dayOfWeek)}" /> <span class="text-indigo-200"><c:out value="${publicDisplay.time(block.startTime)}" /> to <c:out value="${publicDisplay.time(block.endTime)}" /></span></h3>
<details class="mt-4"><summary>Edit these hours</summary>
<%-- InstructorAvailabilityController sends edits through ownership and overlap checks. --%>
<%-- InstructorAvailabilityController supplies this form and its original field names. --%>
<form class="mt-4 grid gap-4 sm:grid-cols-3" method="post" action="<c:url value='/instructor/availability/update' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="${block.id}">
<div><label for="day-${block.id}">Day</label><select class="block w-full" id="day-${block.id}" name="dayOfWeek"><c:forEach items="${days}" var="day"><option value="${day}" <c:if test="${day eq block.dayOfWeek}">selected</c:if>><c:out value="${publicDisplay.label(day)}" /></option></c:forEach></select></div>
<div><label for="start-${block.id}">Start time</label><input class="block w-full" id="start-${block.id}" type="time" name="startTime" value="${block.startTime}" required></div><div><label for="end-${block.id}">End time</label><input class="block w-full" id="end-${block.id}" type="time" name="endTime" value="${block.endTime}" required></div><div class="sm:col-span-3"><button class="ds-button" type="submit">Save working hours</button></div></form></details>
<%-- InstructorAvailabilityController supplies this form and its original field names. --%>
<form class="mt-4" method="post" action="<c:url value='/instructor/availability/delete' />" data-confirm="Remove these working hours?"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="${block.id}"><button class="ds-button ds-button-danger" type="submit">Remove hours</button></form></article>
</c:forEach></div></c:otherwise></c:choose></section>
<%-- InstructorAvailabilityController accepts a date and reason for a new day off. --%>
<%-- InstructorAvailabilityController supplies this page section. --%>
<section class="ds-card mb-7" aria-labelledby="add-leave"><h2 id="add-leave">Plan a day off</h2><%-- InstructorAvailabilityController supplies this form and its original field names. --%>
<form class="mt-5 grid gap-4 sm:grid-cols-2" method="post" action="<c:url value='/instructor/availability/leave' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />">
<div><label for="leave-date">Date</label><input class="block w-full" id="leave-date" type="date" name="date" required></div><div><label for="leave-reason">Reason</label><input class="block w-full" id="leave-reason" name="reason" maxlength="300" required></div><div class="sm:col-span-2"><button class="ds-button" type="submit">Add leave day</button></div></form></section>
<%-- InstructorAvailabilityController keeps past leave visible as well as future plans. --%>
<%-- InstructorAvailabilityController supplies this page section. --%>
<section aria-labelledby="saved-leave"><h2 id="saved-leave" class="mb-5">Leave days</h2><c:choose><c:when test="${empty leaves}"><div class="ds-empty-state"><p>No days off recorded. Add a date when you need one.</p><a class="ds-home-text-link" href="#add-leave">Add a day off</a></div></c:when><c:otherwise><div class="grid gap-4"><c:forEach items="${leaves}" var="leave"><%-- InstructorAvailabilityController supplies this record card. --%>
<article class="ds-card"><h3><c:out value="${publicDisplay.date(leave.date)}" /></h3><p class="mt-2 text-slate-300"><c:out value="${leave.reason}" /></p>
<details class="mt-4"><summary>Edit leave day</summary><%-- InstructorAvailabilityController supplies this form and its original field names. --%>
<form class="mt-4 grid gap-4 sm:grid-cols-2" method="post" action="<c:url value='/instructor/availability/leave/update' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="${leave.id}"><div><label for="date-${leave.id}">Date (today or later)</label><input class="block w-full" id="date-${leave.id}" type="date" name="date" value="${leave.date}" required></div><div><label for="reason-${leave.id}">Reason</label><input class="block w-full" id="reason-${leave.id}" name="reason" value="<c:out value='${leave.reason}' />" maxlength="300" required></div><div class="sm:col-span-2"><button class="ds-button" type="submit">Save leave day</button></div></form></details>
<%-- InstructorAvailabilityController supplies this form and its original field names. --%>
<form class="mt-4" method="post" action="<c:url value='/instructor/availability/leave/delete' />" data-confirm="Remove this leave record?"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="${leave.id}"><button class="ds-button ds-button-danger" type="submit">Remove leave</button></form></article></c:forEach></div></c:otherwise></c:choose></section>
</div>
</main><%@ include file="/WEB-INF/views/common/footer.jspf" %></body></html>
