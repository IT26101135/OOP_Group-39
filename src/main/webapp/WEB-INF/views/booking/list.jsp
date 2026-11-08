<%--
============================================================
MEMBER   : IT26101134 - [Name]
MODULE   : Bookings, FIFO queue and scheduling
FILE     : list.jsp
WHAT IT DOES (simple words): Shows list information and forms.
REAL-LIFE EXAMPLE: A reception waiting line handles the oldest request first.
OOP USED : Not applicable (JSP presentation)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>My lessons | DriveSpark Academy</title><meta name="description" content="Request a free lesson slot, follow FIFO positions and review your booking history.">
<link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />"><link rel="stylesheet" href="<c:url value='/css/tailwind.css' />"></head><body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-container ds-section ds-dashboard-shell"><%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content">
<p class="ds-eyebrow">PLAN YOUR NEXT DRIVE</p><h1>My lessons</h1><p class="mb-7 text-slate-400">Choose an instructor and a free hour, then follow your request from the waiting line to confirmation.</p>
<%-- BookingController supplies only the student's active package and the available instructor list. --%>
<%-- BookingController supplies this page section. --%>
<section class="ds-booking-panel mb-8" aria-labelledby="request-lesson"><h2 id="request-lesson">Request a lesson</h2>
<c:choose><c:when test="${not canBook or empty packages}"><div class="ds-empty-state mt-5"><h3>A learning package is your first step</h3><p>You need an active enrollment with lessons remaining before requesting a lesson.</p><a class="ds-button mt-4" href="<c:url value='/enrollments' />">My enrollments</a></div></c:when><c:otherwise>
<p class="mb-5 mt-3 text-slate-400">1. Choose an instructor and date. 2. Select a free hour. 3. Send your request. Availability is checked again when submitted.</p>
<%-- BookingController supplies this form and its original field names. --%>
<form class="grid gap-4 sm:grid-cols-2" method="get" action="<c:url value='/bookings' />"><div><label for="booking-instructor">Instructor</label><select class="block w-full" id="booking-instructor" name="instructorId" required><c:forEach items="${instructors}" var="instructor"><option value="${instructor.id}" <c:if test="${selectedInstructorId eq instructor.id}">selected</c:if>><c:out value="${instructor.name}" /></option></c:forEach></select></div><div><label for="booking-date">Date</label><input class="block w-full" id="booking-date" type="date" name="date" min="${today}" value="<c:out value='${selectedDate}' />" required></div><div class="sm:col-span-2"><button class="ds-button" type="submit">Find available times</button></div></form>
<c:if test="${not empty slotError}"><p role="alert" class="mt-4"><c:out value="${slotError}" /></p></c:if>
<c:if test="${not empty selectedDate and empty slotError}"><c:choose><c:when test="${empty availableSlots}"><p class="mt-5 rounded-xl border border-white/10 p-4 text-slate-300">No free hours for this instructor and date. Try another date or instructor.</p></c:when><c:otherwise>
<%-- BookingController sends the original packageId, instructorId and startTime fields to the service. --%>
<%-- BookingController supplies this form and its original field names. --%>
<form class="mt-6 border-t border-white/10 pt-5" method="post" action="<c:url value='/bookings' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="instructorId" value="<c:out value='${selectedInstructorId}' />"><label for="booking-package">Your learning package</label><select class="block w-full" id="booking-package" name="packageId" required><c:forEach items="${packages}" var="learningPackage"><option value="${learningPackage.id}"><c:out value="${learningPackage.name}" /></option></c:forEach></select>
<fieldset class="ds-slot-fieldset"><legend>Available hours on <c:out value="${selectedDate}" /></legend><div class="ds-slot-options"><c:forEach items="${availableSlots}" var="slot"><label class="ds-slot-chip"><input type="radio" name="startTime" value="${slot}" required><span><c:out value="${slot.toLocalTime()}" /></span></label></c:forEach></div></fieldset><button class="ds-button ds-button-accent" type="submit">Send lesson request</button></form></c:otherwise></c:choose></c:if>
</c:otherwise></c:choose></section>
<c:set var="tabsLabel" value="Lesson request sections" /><c:set var="firstTabTitle" value="Current requests" /><c:set var="secondTabTitle" value="History" /><c:set var="firstPanelId" value="current-requests" /><c:set var="secondPanelId" value="request-history" /><%@ include file="/WEB-INF/views/common/components/tabs.jspf" %>
<%-- BookingController groups pending/confirmed requests separately from finished history. --%>
<%-- BookingController supplies this page section. --%>
<section id="current-requests" class="mt-6" aria-label="Current requests"><c:choose><c:when test="${empty openBookings}"><div class="ds-empty-state"><p>No current lesson requests.</p></div></c:when><c:otherwise><div class="grid gap-5"><c:forEach items="${openBookings}" var="booking"><%@ include file="/WEB-INF/views/booking/request-card.jspf" %></c:forEach></div></c:otherwise></c:choose></section>
<%-- BookingController supplies this page section. --%>
<section id="request-history" class="mt-6" aria-label="Request history"><c:choose><c:when test="${empty historyBookings}"><div class="ds-empty-state"><p>Completed, declined and cancelled requests will appear here.</p></div></c:when><c:otherwise><div class="grid gap-5"><c:forEach items="${historyBookings}" var="booking"><%@ include file="/WEB-INF/views/booking/request-card.jspf" %></c:forEach></div></c:otherwise></c:choose></section>
</div></main><%@ include file="/WEB-INF/views/common/footer.jspf" %></body></html>
