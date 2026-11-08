<%--
============================================================
MEMBER   : IT26101132 - [Name]
MODULE   : Vehicles and Sample Data
FILE     : list.jsp
WHAT IT DOES (simple words): Keeps vehicles and sample data details clear and safe for the school.
REAL-LIFE EXAMPLE: Like a fleet logbook showing which teaching cars need attention.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for vehicles
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en"><head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Manage the driving school fleet, insurance dates and service reminders.">
    <meta name="theme-color" content="#0B1120">
    <title>Vehicle fleet | DriveSpark Academy</title>
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <link rel="stylesheet" href="<c:url value='/css/tailwind.css' />">
</head><body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-container ds-section ds-dashboard-shell">
<%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %>
<div class="ds-dashboard-content">
    <%-- VehicleController supplies saved vehicles and successful form messages. --%>
    <header class="mb-8 flex flex-wrap items-start justify-between gap-4">
        <div><p class="ds-eyebrow">READY FOR THE ROAD</p><h1>Vehicle fleet</h1>
        <p class="max-w-xl text-slate-400">Keep track of teaching vehicles, service dates and insurance renewals.</p></div>
        <a class="ds-button" href="<c:url value='/admin/vehicles/create' />">Add vehicle</a>
    </header>
    <c:if test="${vehicleCreated or vehicleUpdated or vehicleDeleted}">
        <p class="ds-vehicle-message mb-6" role="status">Vehicle record saved successfully.</p>
    </c:if>
    <%-- Each card shows one stored vehicle. Missing dates stay clearly marked as unknown. --%>
    <section aria-label="Vehicle records" class="grid gap-5 xl:grid-cols-2">
    <c:if test="${empty vehicles}"><div class="ds-card"><h2>No vehicles yet</h2><p>Add your first teaching vehicle to get started.</p></div></c:if>
    <c:forEach items="${vehicles}" var="vehicle">
        <article class="ds-feature-card" data-reveal>
            <div class="mb-5 flex flex-wrap items-start justify-between gap-3">
                <div><p class="text-xs font-bold tracking-widest text-indigo-300"><c:out value="${vehicle.id}" /></p>
                <h2 class="mt-2"><c:out value="${vehicle.registrationNumber}" /></h2>
                <p><c:out value="${vehicle.make}" /> <c:out value="${vehicle.model}" /></p></div>
                <span class="status-badge" data-status="<c:out value='${vehicle.operationalStatus}' />"><c:out value="${vehicle.operationalStatus}" /></span>
            </div>
            <dl class="mb-5 grid grid-cols-2 gap-4 text-sm">
                <div><dt class="text-slate-400">Category</dt><dd><c:out value="${vehicle.vehicleCategory}" /></dd></div>
                <div><dt class="text-slate-400">Transmission</dt><dd><c:out value="${vehicle.transmissionType}" /></dd></div>
                <div><dt class="text-slate-400">Insurance expiry</dt><dd><c:out value="${vehicle.insuranceExpiry}" default="Not recorded" /></dd></div>
                <div><dt class="text-slate-400">Last service</dt><dd><c:out value="${vehicle.lastServiceDate}" default="Not recorded" /></dd></div>
                <div><dt class="text-slate-400">Next service</dt><dd><c:out value="${vehicle.nextServiceDate}" default="Not recorded" /></dd></div>
            </dl>
            <div class="mb-5 flex flex-wrap gap-2">
                <c:choose>
                    <c:when test="${empty vehicle.insuranceExpiry}"><span class="status-badge">Insurance date needed</span></c:when>
                    <c:when test="${vehicle.insuranceExpired}"><span class="status-badge" data-status="REJECTED">Insurance expired</span></c:when>
                    <c:when test="${vehicle.insuranceWarning}"><span class="status-badge" data-status="WARNING">Insurance due within 30 days</span></c:when>
                    <c:otherwise><span class="status-badge" data-status="COMPLETED">Insurance recorded</span></c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${empty vehicle.nextServiceDate}"><span class="status-badge">Service date needed</span></c:when>
                    <c:when test="${vehicle.serviceOverdue}"><span class="status-badge" data-status="REJECTED">Service overdue</span></c:when>
                    <c:when test="${vehicle.serviceWarning}"><span class="status-badge" data-status="WARNING">Service due within 30 days</span></c:when>
                    <c:otherwise><span class="status-badge" data-status="COMPLETED">Service scheduled</span></c:otherwise>
                </c:choose>
            </div>
            <%-- VehicleController rechecks the selected ID and protects vehicles linked to lesson history. --%>
            <div class="flex flex-wrap gap-3 border-t border-white/10 pt-4">
                <a class="ds-button" aria-label="Edit <c:out value='${vehicle.registrationNumber}' />" href="<c:url value='/admin/vehicles/${vehicle.id}/edit' />">Edit vehicle</a>
                <form method="post" action="<c:url value='/admin/vehicles/${vehicle.id}/delete' />" data-confirm="Delete this vehicle? Vehicles referenced by lessons will be kept.">
                    <input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />">
                    <button class="ds-button ds-button-outline" type="submit" aria-label="Delete <c:out value='${vehicle.registrationNumber}' />">Delete</button>
                </form>
            </div>
        </article>
    </c:forEach>
    </section>
</div></main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body></html>
