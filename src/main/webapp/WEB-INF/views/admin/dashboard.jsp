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
    <title>School dashboard | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
</head>
<body class="ds-account-page">

<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main id="main-content" class="ds-container ds-section ds-admin-dashboard ds-dashboard-shell"><%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content">
    <%-- AdminDashboardController supplies the welcome name; summary values below come from existing records. --%>
    <header class="ds-admin-dashboard-heading ds-workspace-heading">
        <p class="ds-admin-dashboard-eyebrow">ADMINISTRATION</p>
        <h1>School dashboard</h1>
        <p>
            Welcome, <c:out value="${navDisplayName}" />.
            Manage your driving school from here.
        </p>
    </header>
    <%-- DashboardSummaryService counts waiting receipts from existing enrollment records. --%>
    <section class="ds-feature-card mt-6" aria-labelledby="awaiting-payments-title">
        <h2 id="awaiting-payments-title">Awaiting payment <span class="status-badge ds-payment-status" data-status="AWAITING_PAYMENT"><c:out value="${summary.awaitingPayments}" default="0" /></span></h2>
        <p class="mt-3">Check payment receipts before confirming that students can book lessons.</p>
        <a class="ds-button ds-button-outline mt-4" href="<c:url value='/admin/enrollments' />">Awaiting payment</a>
    </section>

    <%-- This block shows a page section. AdminDashboardController supplies the page data. --%>
<section class="ds-workspace-statistics" aria-label="Key academy statistics">
        <div class="ds-workspace-stat-grid">
            <div class="ds-stat-card flex flex-col">
                <span class="text-xs uppercase tracking-wide text-slate-400">Students</span>
                <strong class="ds-workspace-number"><c:out value="${summary.studentCount}" default="0" /></strong>
            </div>
            <div class="ds-stat-card flex flex-col">
                <span class="text-xs uppercase tracking-wide text-slate-400">Training vehicles</span>
                <strong class="ds-workspace-number"><c:out value="${summary.vehicleCount}" default="0" /></strong>
            </div>
            <div class="ds-stat-card flex flex-col">
                <span class="text-xs uppercase tracking-wide text-slate-400">Instructors</span>
                <strong class="ds-workspace-number"><c:out value="${summary.instructorCount}" default="0" /></strong>
            </div>
            <div class="ds-stat-card flex flex-col">
                <span class="text-xs uppercase tracking-wide text-slate-400">Waiting requests</span>
                <strong class="ds-workspace-number"><c:out value="${summary.pendingBookings}" default="0" /></strong>
            </div>
            <div class="ds-stat-card flex flex-col">
                <span class="text-xs uppercase tracking-wide text-slate-400">Courses</span>
                <strong class="ds-workspace-number"><c:out value="${summary.packageCount}" default="0" /></strong>
            </div>
            <div class="ds-stat-card flex flex-col">
                <span class="text-xs uppercase tracking-wide text-slate-400">Payments received</span>
                <strong class="ds-workspace-number"><span class="ds-workspace-unit">LKR</span> <c:out value="${publicDisplay.money(summary.paidRevenue)}" default="0" /></strong>
            </div>
        </div>
    </section>

    <%-- This block shows a page section. AdminDashboardController supplies the page data. --%>
<section aria-labelledby="dashboard-tools-title">
        <h2 id="dashboard-tools-title">School records</h2>

        <div class="ds-admin-dashboard-grid">
            <%-- This block shows one saved record. AdminDashboardController supplies the page data. --%>
<article class="ds-admin-dashboard-card">
                <span class="ds-admin-dashboard-label">STUDENTS</span>
                <span class="ds-status-badge" data-status="ACTIVE"><c:out value="${summary.studentCount}" default="0" /> registered</span>
                <h3>Manage student accounts</h3>
                <p>Approve, suspend, edit, search, and delete student accounts.</p>
                <a class="ds-button ds-button-outline" href="<c:out value='${dashboardStudentsUrl}' />">View students</a>
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
                    View vehicles
                </a>
            </article>
            <%-- This block shows one saved record. AdminDashboardController supplies the page data. --%>
<article class="ds-admin-dashboard-card">
                <span class="ds-admin-dashboard-label">LESSONS</span>
                <span class="ds-status-badge" data-status="WARNING"><c:out value="${summary.pendingBookings}" default="0" /> pending requests</span>
                <h3>Manage lesson requests</h3>
                <p>Assign instructors and vehicles to student requests.</p>
                <a class="ds-button ds-button-outline" href="<c:out value='${dashboardBookingsUrl}' />">View lesson requests</a>
            </article>
            <%-- This block shows one saved record. AdminDashboardController supplies the page data. --%>
<article class="ds-admin-dashboard-card">
                <span class="ds-admin-dashboard-label">REVIEWS</span>
                <span class="ds-status-badge" data-status="ACTIVE">Awaiting review</span>
                <h3>Review student feedback</h3>
                <p>Review student submissions and control which are visible.</p>
                <a class="ds-button ds-button-outline" href="<c:out value='${dashboardReviewsUrl}' />">Review feedback</a>
            </article>
            <%-- This block shows one saved record. AdminDashboardController supplies the page data. --%>
<article class="ds-admin-dashboard-card">
                <span class="ds-admin-dashboard-label">INSTRUCTORS</span>
                <span class="ds-status-badge" data-status="CONFIRMED"><c:out value="${summary.instructorCount}" default="0" /> total</span>
                <h3>Manage instructors</h3>
                <p>Add instructors and review their records.</p>
                <a class="ds-button ds-button-outline" href="<c:out value='${dashboardInstructorsUrl}' />">View instructors</a>
            </article>
            <%-- This block shows one saved record. AdminDashboardController supplies the page data. --%>
<article class="ds-admin-dashboard-card">
                <span class="ds-admin-dashboard-label">LEARNING PACKAGES</span>
                <span class="ds-status-badge" data-status="ACTIVE"><c:out value="${summary.packageCount}" default="0" /> total</span>
                <h3>Manage packages</h3>
                <p>Create the lesson packages shown to students.</p>
                <a class="ds-button ds-button-outline" href="<c:out value='${dashboardPackagesUrl}' />">View packages</a>
            </article>
        </div>
    </section>
    <section class="ds-card mt-6" aria-labelledby="contact-messages-title" id="contact-messages">
        <h2 id="contact-messages-title">Website enquiries</h2>
        <p>Read messages sent through Contact us. Follow up outside this website using the sender's email address.</p>
        <c:choose><c:when test="${empty contactMessages}">
            <p class="mt-4">No enquiries have arrived yet. You can check the public contact form below.</p>
            <a class="ds-home-text-link" href="<c:url value='/contact' />">View contact page</a>
        </c:when><c:otherwise><div class="grid gap-4 mt-4"><c:forEach items="${contactMessages}" var="enquiry">
            <article class="border-b border-slate-200 pb-4">
                <h3><c:out value="${enquiry.name}" /></h3>
                <p>Email: <c:out value="${enquiry.email}" /></p>
                <p><c:out value="${publicDisplay.date(enquiry.createdAt.toLocalDate())}" /> at <c:out value="${publicDisplay.time(enquiry.createdAt.toLocalTime())}" /></p>
                <p class="mt-3 break-words whitespace-pre-wrap"><c:out value="${enquiry.message}" /></p>
            </article>
        </c:forEach></div></c:otherwise></c:choose>
    </section>
</div></main>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>

</body>
</html>
