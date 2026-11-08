<%-- Module owner: IT26101135 --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url value="/css/tailwind.css" var="appCssUrl" />
<!DOCTYPE html>
<html lang="en"><head>
    <meta name="description" content="Manage students at DriveSpark Academy. Explore lessons, learning resources and driving school information.">
    <meta name="theme-color" content="#0B1120">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />"><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Manage students</title><link rel="stylesheet" href="<c:out value='${appCssUrl}' />"></head>
<body><%@ include file="/WEB-INF/views/common/navbar.jspf" %><main id="main-content" class="ds-container ds-section ds-dashboard-shell"><%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content"><h1>Manage student accounts</h1>
<c:if test="${not empty success}"><p role="status"><c:out value="${success}" /></p></c:if>
<form method="get" action="<c:url value='/admin/students' />"><label>Search by ID, name, or email <input name="q" value="<c:out value='${query}' />"></label><button type="submit">Search</button></form>
<c:choose><c:when test="${empty students}"><p>No matching students.</p></c:when><c:otherwise><c:forEach items="${students}" var="student"><article class="ds-feature-card"><h2><c:out value="${student.name}" /> (<c:out value="${student.id}" />)</h2><p>Status: <c:out value="${student.status}" /> Â· Created: <c:out value="${student.createdAt}" /></p>
<form method="post" action="<c:url value='/admin/students/update' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="<c:out value='${student.id}' />"><label>Name <input name="name" value="<c:out value='${student.name}' />" required minlength="2" maxlength="100"></label><label>Email <input name="email" type="email" value="<c:out value='${student.email}' />" required></label><label>Phone <input name="phone" value="<c:out value='${student.phone}' />" required></label><button type="submit">Save student</button></form>
<form method="post" action="<c:url value='/admin/students/status' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="<c:out value='${student.id}' />"><label>Account status <select name="status"><c:forEach items="${statuses}" var="status"><option value="<c:out value='${status}' />" <c:if test="${student.status eq status}">selected</c:if>><c:out value="${status}" /></option></c:forEach></select></label><button type="submit">Update status</button></form>
<form method="post" action="<c:url value='/admin/students/delete' />" data-confirm="Delete this student account?"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="<c:out value='${student.id}' />"><button type="submit">Delete student</button></form></article></c:forEach></c:otherwise></c:choose>
</div></main><%@ include file="/WEB-INF/views/common/footer.jspf" %></body></html>
