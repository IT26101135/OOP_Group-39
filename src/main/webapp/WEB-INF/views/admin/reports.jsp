<%--
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : reports.jsp
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
<meta name="description" content="School totals, accessible charts, printable reports and CSV export from saved records."><meta name="theme-color" content="#0B1120">
<title>School reports | DriveSpark Academy</title>
<link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
<link rel="stylesheet" href="<c:url value='/css/tailwind.css' />">
<link rel="stylesheet" href="<c:url value='/css/report.css' />">
</head><body><%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-container ds-section ds-dashboard-shell">
<%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content">

<%-- AdminReportController supplies ReportSection rows calculated from existing saved records only. --%>
<header class="mb-8 flex flex-wrap items-start justify-between gap-5">
<div><p class="ds-eyebrow">SCHOOL OVERVIEW</p><h1>Driving school reports</h1>
<p class="max-w-xl text-slate-400">Enrollment, lesson and fleet totals from the current records.</p></div>
<div class="no-print flex flex-wrap gap-3"><a class="ds-button" href="<c:url value='/admin/reports/export.csv' />">Export CSV</a>
<button class="ds-button ds-button-outline" type="button" data-print>Print report</button></div>
</header>
<noscript><p class="no-print mb-5">Use your browser menu to print this report. CSV export works without JavaScript.</p></noscript>
<p class="mb-6 text-sm text-slate-400">Counts include saved history. Instructor and vehicle usage count all assigned lesson statuses, not just completed lessons. Revenue counts Paid enrollments only, using their captured amounts. Awaiting payment and Cancelled requests are excluded from revenue.</p>
<div class="ds-report-grid grid gap-6 xl:grid-cols-2">
<c:forEach items="${reports}" var="report" varStatus="sectionIndex">
<section class="ds-card ds-report-section" aria-labelledby="report-${sectionIndex.index}">
<h2 id="report-${sectionIndex.index}"><c:out value="${report.title}" /></h2>
<c:choose><c:when test="${empty report.rows}"><p class="mt-5 text-slate-400">No records in this group yet.</p></c:when><c:otherwise>
<%-- SVG bars use the exact table counts; zero stays zero and each section has its own scale. --%>
<c:if test="${report.chart}"><figure class="my-5" aria-label="<c:out value='${report.title}' /> chart">
<c:forEach items="${report.rows}" var="row">
<div class="mb-4"><div class="mb-2 flex justify-between gap-3 text-xs"><span class="break-all text-slate-300"><c:out value="${row.label}" /></span><strong><c:out value="${row.value}" /></strong></div>
<svg class="ds-report-chart block w-full" data-progress viewBox="0 0 100 5" preserveAspectRatio="none" role="img" aria-label="<c:out value='${row.label}' />: <c:out value='${row.value}' />">
<rect width="100" height="5" rx="2.5" class="ds-report-track" />
<rect width="<c:out value='${row.percentage}' />" height="5" rx="2.5" class="ds-report-bar" />
</svg></div></c:forEach>
<figcaption class="text-xs text-slate-400">Bar lengths are relative to the largest count in this section.</figcaption></figure></c:if>
<%-- The exact numbers remain in a table for screen readers, printing and script-free browsing. --%>
<div class="safe-scroll mt-5"><table class="ds-report-table w-full text-sm">
<caption class="sr-only"><c:out value="${report.title}" /> exact values</caption>
<thead><tr><th scope="col">Item</th><th scope="col">Value</th></tr></thead><tbody>
<c:forEach items="${report.rows}" var="row"><tr><th scope="row"><c:out value="${row.label}" /></th><td><c:out value="${row.value}" /></td></tr></c:forEach>
</tbody></table></div>
</c:otherwise></c:choose></section></c:forEach></div>
</div></main><%@ include file="/WEB-INF/views/common/footer.jspf" %></body></html>
