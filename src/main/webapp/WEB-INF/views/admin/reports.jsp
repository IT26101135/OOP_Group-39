<%-- Module owner: IT26101137 --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url value="/css/tailwind.css" var="appCssUrl" />
<!DOCTYPE html><html lang="en"><head>
    <meta name="description" content="Reports at DriveSpark Academy. Explore lessons, learning resources and driving school information.">
    <meta name="theme-color" content="#0B1120">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />"><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Reports</title><link rel="stylesheet" href="<c:out value='${appCssUrl}' />"></head>
<body><%@ include file="/WEB-INF/views/common/navbar.jspf" %><main id="main-content" class="ds-container ds-section ds-dashboard-shell"><%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content"><h1>Driving school reports</h1><p class="no-print"><a href="<c:url value='/admin/reports/export.csv' />">Export CSV</a> Â· <button onclick="window.print()">Print</button></p>
<c:forEach items="${reports}" var="report"><section><h2><c:out value="${report.key}" /></h2><div class="safe-scroll rounded-2xl border border-white/10 bg-surface"><table><thead><tr><th>Item</th><th>Value</th></tr></thead><tbody><c:forEach items="${report.value}" var="row"><tr><td><c:out value="${row.key}" /></td><td><c:out value="${row.value}" /></td></tr></c:forEach></tbody></table></div></section></c:forEach>
</div></main><%@ include file="/WEB-INF/views/common/footer.jspf" %></body></html>
