<%--
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : dashboard.jsp
WHAT IT DOES (simple words): Shows saved school totals and links to each management area.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url value="/css/tailwind.css" var="appCssUrl" />
<c:url value="/admin/vehicles" var="dashboardVehiclesUrl" />
<c:url value="/admin/instructors" var="dashboardInstructorsUrl" />
<c:url value="/admin/packages" var="dashboardPackagesUrl" />
<c:url value="/admin/reviews" var="dashboardReviewsUrl" />
<c:url value="/admin/bookings" var="dashboardBookingsUrl" />
<c:url value="/admin/students" var="dashboardStudentsUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="description" content="Manage academy records and view current student, instructor, vehicle, package and lesson totals.">
    <meta name="theme-color" content="#050505">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Administrator dashboard | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
</head>
<body>

<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main id="main-content" class="ds-container ds-section ds-admin-dashboard ds-dashboard-shell"><%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content">
    <%-- AdminDashboardController supplies the welcome name; summary values below come from existing records. --%>
    <header class="ds-admin-dashboard-heading ds-workspace-heading">
        <p class="ds-admin-dashboard-eyebrow">ADMINISTRATION</p>
        <h1>Administrator dashboard</h1>
        <p>
            Welcome, <c:out value="${navDisplayName}" />.
            Manage your driving school from here.
        </p>
    </header>

    <%-- This block shows a page section. AdminDashboardController supplies the page data. --%>
<section class="ds-workspace-statistics" aria-label="Key academy statistics">
        <div class="ds-workspace-stat-grid">
            <div class="flex flex-col">
                <span class="text-xs uppercase tracking-wide text-slate-400">Students</span>
                <strong class="ds-workspace-number"><c:out value="${summary.studentCount}" default="0" /></strong>
            </div>
            <div class="flex flex-col">
                <span class="text-xs uppercase tracking-wide text-slate-400">Fleet Vehicles</span>
                <strong class="ds-workspace-number"><c:out value="${summary.vehicleCount}" default="0" /></strong>
            </div>
            <div class="flex flex-col">
                <span class="text-xs uppercase tracking-wide text-slate-400">Instructors</span>
                <strong class="ds-workspace-number"><c:out value="${summary.instructorCount}" default="0" /></strong>
            </div>
            <div class="flex flex-col">
                <span class="text-xs uppercase tracking-wide text-slate-400">Pending Lessons</span>
                <strong class="ds-workspace-number"><c:out value="${summary.pendingBookings}" default="0" /></strong>
            </div>
            <div class="flex flex-col">
                <span class="text-xs uppercase tracking-wide text-slate-400">Courses</span>
                <strong class="ds-workspace-number"><c:out value="${summary.packageCount}" default="0" /></strong>
            </div>
            <div class="flex flex-col">
                <span class="text-xs uppercase tracking-wide text-slate-400">Paid Revenue</span>
                <strong class="ds-workspace-number"><span class="ds-workspace-unit">LKR</span> <c:out value="${summary.paidRevenue}" default="0" /></strong>
            </div>
        </div>
    </section>

    <%-- This block shows a page section. AdminDashboardController supplies the page data. --%>
<section aria-labelledby="dashboard-tools-title">
        <h2 id="dashboard-tools-title">Management tools</h2>

        <div class="ds-admin-dashboard-grid">
            <%-- This block shows one saved record. AdminDashboardController supplies the page data. --%>
<article class="ds-admin-dashboard-card">
                <span class="ds-admin-dashboard-label">STUDENTS</span>
                <span class="ds-status-badge" data-status="ACTIVE"><c:out value="${summary.studentCount}" default="0" /> registered</span>
                <h3>Manage student accounts</h3>
                <p>Approve, suspend, edit, search, and delete student accounts.</p>
                <a class="ds-button ds-button-outline" href="<c:out value='${dashboardStudentsUrl}' />">Open student management</a>
            </article>
            <%-- This block shows one saved record. AdminDashboardController supplies the page data. --%>
<article class="ds-admin-dashboard-card">
                <span class="ds-admin-dashboard-label">VEHICLES</span>
                <span class="ds-status-badge" data-status="CONFIRMED"><c:out value="${summary.vehicleCount}" default="0" /> vehicles</span>
                <h3>Manage vehicles</h3>
                <p>
                    View your vehicles, add new records, and update
                    registration numbers, makes and models.
                </p>
                <a class="ds-button ds-button-outline"
                   href="<c:out value='${dashboardVehiclesUrl}' />">
                    Open vehicle management
                </a>
            </article>
            <%-- This block shows one saved record. AdminDashboardController supplies the page data. --%>
<article class="ds-admin-dashboard-card">
                <span class="ds-admin-dashboard-label">LESSONS</span>
                <span class="ds-status-badge" data-status="WARNING"><c:out value="${summary.pendingBookings}" default="0" /> pending requests</span>
                <h3>Manage lesson requests</h3>
                <p>Assign instructors and vehicles to student requests.</p>
                <a class="ds-button ds-button-outline" href="<c:out value='${dashboardBookingsUrl}' />">Open lesson management</a>
            </article>
            <%-- This block shows one saved record. AdminDashboardController supplies the page data. --%>
<article class="ds-admin-dashboard-card">
                <span class="ds-admin-dashboard-label">REVIEWS</span>
                <span class="ds-status-badge" data-status="ACTIVE">Moderation queue</span>
                <h3>Moderate reviews</h3>
                <p>Review student submissions and control which are visible.</p>
                <a class="ds-button ds-button-outline" href="<c:out value='${dashboardReviewsUrl}' />">Open review moderation</a>
            </article>
            <%-- This block shows one saved record. AdminDashboardController supplies the page data. --%>
<article class="ds-admin-dashboard-card">
                <span class="ds-admin-dashboard-label">INSTRUCTORS</span>
                <span class="ds-status-badge" data-status="CONFIRMED"><c:out value="${summary.instructorCount}" default="0" /> total</span>
                <h3>Manage instructors</h3>
                <p>Add instructors and review their records.</p>
                <a class="ds-button ds-button-outline" href="<c:out value='${dashboardInstructorsUrl}' />">Open instructor management</a>
            </article>
            <%-- This block shows one saved record. AdminDashboardController supplies the page data. --%>
<article class="ds-admin-dashboard-card">
                <span class="ds-admin-dashboard-label">LEARNING PACKAGES</span>
                <span class="ds-status-badge" data-status="ACTIVE"><c:out value="${summary.packageCount}" default="0" /> total</span>
                <h3>Manage packages</h3>
                <p>Create the lesson packages shown to students.</p>
                <a class="ds-button ds-button-outline" href="<c:out value='${dashboardPackagesUrl}' />">Open package management</a>
            </article>
        </div>
    </section>
</div></main>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>

</body>
</html>
