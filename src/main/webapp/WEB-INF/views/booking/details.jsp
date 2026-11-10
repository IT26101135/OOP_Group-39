<%--
============================================================
MEMBER   : IT26101134 - [Name]
MODULE   : Bookings, FIFO queue and scheduling
FILE     : details.jsp
WHAT IT DOES (simple words): Shows one booking and its package, status and waiting-line position.
REAL-LIFE EXAMPLE: Read a lesson slip without changing the appointment.
OOP USED : Presentation reads encapsulated booking properties through getters.
CRUD     : READ only; changes remain on the existing booking list pages.
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Booking details | DriveSpark Academy</title>
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <link rel="stylesheet" href="<c:url value='/css/tailwind.css' />">
</head>
<body class="ds-account-page">
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-container ds-section ds-dashboard-shell">
    <%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %>
    <div class="ds-dashboard-content">
        <p class="ds-eyebrow">YOUR LESSON RECORD</p>
        <h1>Booking details</h1>
        <p class="mb-6">Read your lesson information here. To make changes, return to the booking list.</p>
        <%-- Step 1: Show the authorized booking and its own enrollment's package. --%>
        <article class="ds-card">
            <h2><c:out value="${'LESSON'}" /></h2>
            <p class="my-3"><span class="status-badge" data-status="${booking.status}"><c:out value="${publicDisplay.label(booking.status)}" /></span></p>
            <dl class="grid gap-4 sm:grid-cols-2">
                <div><dt>Package</dt><dd><c:out value="${packageDetails.packageName}" /></dd></div>
                <div><dt>Course type</dt><dd><c:out value="${publicDisplay.label(packageDetails.type)}" /></dd></div>
                <div><dt>Student</dt><dd><c:out value="${accountDisplay.person(booking.studentId)}" /></dd></div>
                <div><dt>Enrollment</dt><dd><c:out value="${accountDisplay.enrollment(booking.enrollmentId)}" /></dd></div>
                <div><dt>Lesson date</dt><dd><c:out value="${publicDisplay.date(booking.startTime.toLocalDate())}" /></dd></div>
                <div><dt>Lesson time</dt><dd><c:out value="${publicDisplay.time(booking.startTime.toLocalTime())}" /> to <c:out value="${publicDisplay.time(booking.endTime.toLocalTime())}" /></dd></div>
                <div><dt>Requested instructor</dt><dd><c:out value="${accountDisplay.person(booking.requestedInstructorId)}" default="Not recorded" /></dd></div>
                <div><dt>Assigned instructor</dt><dd><c:out value="${accountDisplay.person(booking.instructorId)}" default="Not assigned" /></dd></div>
                <div><dt>Vehicle</dt><dd><c:out value="${accountDisplay.vehicle(booking.vehicleId)}" default="Not assigned" /></dd></div>
                <div><dt>Package valid until</dt><dd><c:out value="${publicDisplay.date(packageDetails.validity)}" /></dd></div>
                <div><dt>Request submitted</dt><dd><c:out value="${publicDisplay.date(booking.createdAt.toLocalDate())}" /> <c:out value="${publicDisplay.time(booking.createdAt.toLocalTime())}" /></dd></div>
            </dl>
            <%-- Step 2: Distinguish waiting for assignment from waiting for instructor approval. --%>
            <c:choose>
                <c:when test="${booking.status eq 'PENDING' and empty booking.instructorId}">
                    <p class="mt-5">Waiting for assignment.</p>
                    <c:if test="${queuePosition gt 0}"><p>Position in waiting list <c:out value="${queuePosition}" /></p></c:if>
                </c:when>
                <c:when test="${booking.status eq 'PENDING'}"><p class="mt-5">Assigned - waiting for instructor approval.</p></c:when>
                <c:when test="${booking.status eq 'CONFIRMED'}"><p class="mt-5">Your lesson is confirmed.</p></c:when>
                <c:otherwise><p class="mt-5">This lesson request is in your history.</p></c:otherwise>
            </c:choose>
            <c:if test="${not empty booking.rejectionReason}"><p class="mt-4">Reason: <c:out value="${booking.rejectionReason}" /></p></c:if>
        </article>
        <a class="ds-button ds-button-outline mt-6" href="<c:url value='${backUrl}' />">Back to bookings</a>
    </div>
</main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
