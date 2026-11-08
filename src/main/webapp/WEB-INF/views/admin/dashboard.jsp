<%-- Module owner: IT26101135 --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:url value="/css/tailwind.css" var="appCssUrl" />
<c:url value="/admin/vehicles" var="dashboardVehiclesUrl" />
<c:url value="/admin/instructors" var="dashboardInstructorsUrl" />
<c:url value="/admin/packages" var="dashboardPackagesUrl" />
<c:url value="/admin/reviews" var="dashboardReviewsUrl" />
<c:url value="/admin/bookings" var="dashboardBookingsUrl" />
<c:url value="/admin/students" var="dashboardStudentsUrl" />

<spring:eval expression="@adminReportService.bookingsByStatus()" var="bookingStats" />
<spring:eval expression="@adminReportService.vehicleUsage()" var="vehicleUsageMap" />
<spring:eval expression="@adminReportService.revenue()" var="revenueStats" />
<spring:eval expression="@adminReportService.instructorRatings()" var="instructorStats" />
<spring:eval expression="@adminReportService.enrollmentsByPackage()" var="pkgStats" />
<spring:eval expression="@userRepository.readAllStrict().stream().filter(u -> u.role.name() == 'STUDENT').count()" var="totalStudents" />

<c:set var="pendingBookings" value="${empty bookingStats['PENDING'] ? 0 : bookingStats['PENDING']}" />
<c:set var="confirmedBookings" value="${empty bookingStats['CONFIRMED'] ? 0 : bookingStats['CONFIRMED']}" />
<c:set var="completedBookings" value="${empty bookingStats['COMPLETED'] ? 0 : bookingStats['COMPLETED']}" />
<c:set var="paidRevenue" value="${empty revenueStats['PAID'] ? '0.00' : revenueStats['PAID']}" />
<c:set var="totalVehicles" value="${empty vehicleUsageMap ? 0 : vehicleUsageMap.size()}" />
<c:set var="totalInstructors" value="${empty instructorStats ? 0 : instructorStats.size()}" />
<c:set var="totalPackages" value="${empty pkgStats ? 0 : pkgStats.size()}" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="description" content="Administrator dashboard | DriveSpark Academy at DriveSpark Academy. Explore lessons, learning resources and driving school information.">
    <meta name="theme-color" content="#0B1120">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Administrator dashboard | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
</head>
<body>

<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main id="main-content" class="ds-container ds-section ds-admin-dashboard ds-dashboard-shell"><%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content">
    <header class="ds-admin-dashboard-heading">
        <p class="ds-admin-dashboard-eyebrow">ADMINISTRATION</p>
        <h1>Administrator dashboard</h1>
        <p>
            Welcome, <c:out value="${navDisplayName}" />.
            Manage your driving school from here.
        </p>
    </header>

    <section class="ds-stats-strip mb-8 rounded-2xl border border-white/10 bg-surface p-6" aria-label="Key academy statistics">
        <div class="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-6">
            <div class="flex flex-col">
                <span class="text-xs uppercase tracking-wide text-slate-400">Students</span>
                <strong class="text-2xl font-bold text-indigo-200"><c:out value="${totalStudents}" default="0" /></strong>
            </div>
            <div class="flex flex-col">
                <span class="text-xs uppercase tracking-wide text-slate-400">Fleet Vehicles</span>
                <strong class="text-2xl font-bold text-indigo-200"><c:out value="${totalVehicles}" default="0" /></strong>
            </div>
            <div class="flex flex-col">
                <span class="text-xs uppercase tracking-wide text-slate-400">Instructors</span>
                <strong class="text-2xl font-bold text-indigo-200"><c:out value="${totalInstructors}" default="0" /></strong>
            </div>
            <div class="flex flex-col">
                <span class="text-xs uppercase tracking-wide text-slate-400">Pending Lessons</span>
                <strong class="text-2xl font-bold text-amber-300"><c:out value="${pendingBookings}" default="0" /></strong>
            </div>
            <div class="flex flex-col">
                <span class="text-xs uppercase tracking-wide text-slate-400">Courses</span>
                <strong class="text-2xl font-bold text-indigo-200"><c:out value="${totalPackages}" default="0" /></strong>
            </div>
            <div class="flex flex-col">
                <span class="text-xs uppercase tracking-wide text-slate-400">Paid Revenue</span>
                <strong class="text-2xl font-bold text-emerald-400">LKR <c:out value="${paidRevenue}" default="0" /></strong>
            </div>
        </div>
    </section>

    <section aria-labelledby="dashboard-tools-title">
        <h2 id="dashboard-tools-title">Management tools</h2>

        <div class="ds-admin-dashboard-grid">
            <article class="ds-admin-dashboard-card">
                <span class="ds-admin-dashboard-label">STUDENTS</span>
                <span class="ds-status-badge" data-status="ACTIVE"><c:out value="${totalStudents}" default="0" /> registered</span>
                <h3>Manage student accounts</h3>
                <p>Approve, suspend, edit, search, and delete student accounts.</p>
                <a class="ds-button" href="<c:out value='${dashboardStudentsUrl}' />">Open student management</a>
            </article>
            <article class="ds-admin-dashboard-card">
                <span class="ds-admin-dashboard-label">VEHICLES</span>
                <span class="ds-status-badge" data-status="CONFIRMED"><c:out value="${totalVehicles}" default="0" /> vehicles</span>
                <h3>Manage vehicles</h3>
                <p>
                    View your vehicles, add new records, and update
                    registration numbers, makes and models.
                </p>
                <a class="ds-button"
                   href="<c:out value='${dashboardVehiclesUrl}' />">
                    Open vehicle management
                </a>
            </article>
            <article class="ds-admin-dashboard-card">
                <span class="ds-admin-dashboard-label">LESSONS</span>
                <span class="ds-status-badge" data-status="WARNING"><c:out value="${pendingBookings}" default="0" /> pending requests</span>
                <h3>Manage lesson requests</h3>
                <p>Assign instructors and vehicles to student requests.</p>
                <a class="ds-button" href="<c:out value='${dashboardBookingsUrl}' />">Open lesson management</a>
            </article>
            <article class="ds-admin-dashboard-card">
                <span class="ds-admin-dashboard-label">REVIEWS</span>
                <span class="ds-status-badge" data-status="ACTIVE">Moderation queue</span>
                <h3>Moderate reviews</h3>
                <p>Review student submissions and control which are visible.</p>
                <a class="ds-button" href="<c:out value='${dashboardReviewsUrl}' />">Open review moderation</a>
            </article>
            <article class="ds-admin-dashboard-card">
                <span class="ds-admin-dashboard-label">INSTRUCTORS</span>
                <span class="ds-status-badge" data-status="CONFIRMED"><c:out value="${totalInstructors}" default="0" /> active</span>
                <h3>Manage instructors</h3>
                <p>Add instructors and review their records.</p>
                <a class="ds-button" href="<c:out value='${dashboardInstructorsUrl}' />">Open instructor management</a>
            </article>
            <article class="ds-admin-dashboard-card">
                <span class="ds-admin-dashboard-label">LEARNING PACKAGES</span>
                <span class="ds-status-badge" data-status="ACTIVE"><c:out value="${totalPackages}" default="0" /> available</span>
                <h3>Manage packages</h3>
                <p>Create the lesson packages shown to students.</p>
                <a class="ds-button" href="<c:out value='${dashboardPackagesUrl}' />">Open package management</a>
            </article>
        </div>
    </section>
</div></main>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>

</body>
</html>
