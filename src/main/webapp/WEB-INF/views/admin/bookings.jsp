<%--
============================================================
MEMBER   : IT26101134 - [Name]
MODULE   : Bookings, FIFO queue and scheduling
FILE     : bookings.jsp
WHAT IT DOES (simple words): Shows bookings information and forms.
REAL-LIFE EXAMPLE: A reception waiting line handles the oldest request first.
OOP USED : Not applicable (JSP presentation)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Manage lessons | DriveSpark Academy</title><meta name="description" content="Review the FIFO waiting line, assign requests and filter the driving lesson schedule.">
<link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />"><link rel="stylesheet" href="<c:url value='/css/tailwind.css' />"></head><body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %><main id="main-content" class="ds-container ds-section ds-dashboard-shell"><%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content">
<p class="ds-eyebrow">LESSON OPERATIONS</p><h1>Manage lesson requests</h1><p class="mb-7 text-slate-400">Handle requests fairly in submission order, then review the lesson schedule.</p>
<%-- AdminBookingController supplies the full unassigned FIFO queue, independent of display filters. --%>
<%-- AdminBookingController supplies this page section. --%>
<section class="ds-booking-panel mb-8" aria-labelledby="fifo-heading"><div class="mb-4 flex flex-wrap items-center justify-between gap-3"><h2 id="fifo-heading">FIFO waiting line</h2><span class="ds-queue-position"><c:out value="${queueSize}" /> waiting</span></div>
<c:choose><c:when test="${empty pendingQueue}"><p class="text-slate-400">No unassigned requests are waiting.</p></c:when><c:otherwise><ol class="mb-5 grid gap-2"><c:forEach items="${pendingQueue}" var="queued" varStatus="position"><li class="rounded-xl border border-white/10 bg-surface p-3"><span class="mr-2 font-bold text-indigo-200"><c:out value="${position.count}" />.</span><c:out value="${queued.id}" /> - Student <c:out value="${queued.studentId}" /> - <c:out value="${queued.startTime.toLocalDate()}" /> <c:out value="${queued.startTime.toLocalTime().withSecond(0).withNano(0)}" /></li></c:forEach></ol>
<p class="mb-4 text-sm text-slate-400">Choose resources for the oldest waiting request. The server checks conflicts again and sends a valid assignment to the instructor for approval.</p>
<%-- AdminBookingController processes only the current queue head, never a selected later request. --%>
<%-- AdminBookingController supplies this form and its original field names. --%>
<form class="grid gap-4 sm:grid-cols-2" method="post" action="<c:url value='/admin/bookings/process-next' />" data-confirm="Assign the oldest request for instructor approval?"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="bookingId" value="${pendingQueue[0].id}"><div><label for="next-instructor">Instructor</label><select class="block w-full" id="next-instructor" name="instructorId" required><c:forEach items="${instructors}" var="instructor"><option value="${instructor.id}"><c:out value="${instructor.name}" /></option></c:forEach></select></div><div><label for="next-vehicle">Vehicle</label><select class="block w-full" id="next-vehicle" name="vehicleId" required><c:forEach items="${vehicles}" var="vehicle"><option value="${vehicle.id}"><c:out value="${vehicle.registrationNumber}" /> - <c:out value="${vehicle.make}" /> <c:out value="${vehicle.model}" /></option></c:forEach></select></div><div class="sm:col-span-2"><button class="ds-button ds-button-accent" type="submit">Assign oldest request</button></div></form></c:otherwise></c:choose></section>
<%-- AdminBookingController echoes filter values so the selected schedule view is clear. --%>
<%-- AdminBookingController supplies this page section. --%>
<section class="ds-card mb-7" aria-labelledby="schedule-filters"><h2 id="schedule-filters">Filter schedule</h2><%-- AdminBookingController supplies this form and its original field names. --%>
<form class="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-3" method="get" action="<c:url value='/admin/bookings' />">
<div><label for="filter-from">From</label><input class="block w-full" id="filter-from" type="date" name="from" value="<c:out value='${filterFrom}' />"></div><div><label for="filter-to">To</label><input class="block w-full" id="filter-to" type="date" name="to" value="<c:out value='${filterTo}' />"></div><div><label for="filter-instructor">Instructor ID</label><input class="block w-full" id="filter-instructor" name="instructorId" value="<c:out value='${filterInstructor}' />"></div><div><label for="filter-vehicle">Vehicle ID</label><input class="block w-full" id="filter-vehicle" name="vehicleId" value="<c:out value='${filterVehicle}' />"></div><div><label for="filter-status">Status</label><select class="block w-full" id="filter-status" name="status"><option value="">All statuses</option><c:forEach items="${bookingStatuses}" var="status"><option value="${status}" <c:if test="${filterStatus eq status}">selected</c:if>><c:out value="${status}" /></option></c:forEach></select></div><div class="flex flex-wrap items-end gap-3"><button class="ds-button" type="submit">Filter schedule</button><a class="ds-button ds-button-outline" href="<c:url value='/admin/bookings' />">Clear filters</a></div></form></section>
<%-- AdminBookingController supplies filtered records while keeping FIFO order for pending work. --%>
<%-- AdminBookingController supplies this page section. --%>
<section aria-labelledby="schedule-results"><h2 id="schedule-results" class="mb-5">Lesson records</h2><c:choose><c:when test="${empty bookings}"><div class="ds-empty-state"><p>No lesson requests match these filters.</p></div></c:when><c:otherwise><div class="grid gap-5"><c:forEach items="${bookings}" var="booking"><%@ include file="/WEB-INF/views/admin/booking-card.jspf" %></c:forEach></div></c:otherwise></c:choose></section></div></main><%@ include file="/WEB-INF/views/common/footer.jspf" %></body></html>