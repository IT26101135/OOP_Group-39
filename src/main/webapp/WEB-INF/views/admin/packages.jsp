<%-- Module owner: IT26101136 --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url value="/css/tailwind.css" var="appCssUrl" />
<!DOCTYPE html>
<html lang="en"><head>
    <meta name="theme-color" content="#0B1120">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />"><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Manage learning packages</title><link rel="stylesheet" href="<c:out value='${appCssUrl}' />"></head>
<body><%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-container ds-section ds-dashboard-shell"><%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content"><h1>Manage learning packages</h1>
<c:if test="${not empty success}"><p role="status"><c:out value="${success}" /></p></c:if>
<h2>Add package</h2>
<form method="post" action="<c:url value='/admin/packages' />">
<input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />" />
<label>Name <input name="name" required minlength="2" maxlength="100"></label>
<label>Description <textarea name="description" required minlength="10" maxlength="1000"></textarea></label>
<label>Price (LKR) <input name="price" type="number" min="0.01" max="1000000" step="0.01" required></label>
<label>Lessons <input name="lessonCount" type="number" min="1" max="100" required></label>
<label>Validity (days) <input name="validityDays" type="number" min="1" max="365" required></label>
<label>Package type <select name="type" required><c:forEach items="${packageTypes}" var="packageType"><option value="<c:out value='${packageType}' />"><c:out value="${packageType}" /></option></c:forEach></select></label>
<label>Duration (hours) <input name="durationHours" type="number" min="1" max="1000" required></label>
<label><input name="active" type="checkbox" value="true" checked> Available to students</label>
<button type="submit">Add package</button></form>
<h2>Package records</h2>
<c:choose><c:when test="${empty packages}"><p>No packages have been added.</p></c:when><c:otherwise><div class="safe-scroll rounded-2xl border border-white/10 bg-surface"><table><thead><tr><th>ID</th><th>Package details</th><th>Availability</th></tr></thead><tbody><c:forEach items="${packages}" var="learningPackage"><tr><td><c:out value="${learningPackage.id}" /></td><td><form method="post" action="<c:url value='/admin/packages/update' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="<c:out value='${learningPackage.id}' />"><label>Name <input name="name" value="<c:out value='${learningPackage.name}' />" required minlength="2" maxlength="100"></label><label>Description <textarea name="description" required minlength="10" maxlength="1000"><c:out value="${learningPackage.description}" /></textarea></label><label>Price <input name="price" type="number" min="0.01" step="0.01" value="${learningPackage.price}" required></label><label>Lessons <input name="lessonCount" type="number" min="1" max="100" value="${learningPackage.lessonCount}" required></label><label>Validity days <input name="validityDays" type="number" min="1" max="365" value="${learningPackage.validityDays}" required></label><label>Type <select name="type"><c:forEach items="${packageTypes}" var="packageType"><option value="<c:out value='${packageType}' />" <c:if test="${learningPackage.type eq packageType}">selected</c:if>><c:out value="${packageType}" /></option></c:forEach></select></label><label>Duration hours <input name="durationHours" type="number" min="1" max="1000" value="${learningPackage.durationHours}" required></label><button type="submit">Save package</button></form></td><td><c:out value="${learningPackage.active}" /><form method="post" action="<c:url value='/admin/packages/status' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="<c:out value='${learningPackage.id}' />"><input type="hidden" name="active" value="${not learningPackage.active}"><button type="submit"><c:choose><c:when test="${learningPackage.active}">Deactivate</c:when><c:otherwise>Activate</c:otherwise></c:choose></button></form><form method="post" action="<c:url value='/admin/packages/delete' />" data-confirm="Delete this package?"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="<c:out value='${learningPackage.id}' />"><button type="submit">Delete</button></form></td></tr></c:forEach></tbody></table></div></c:otherwise></c:choose>
</div></main><%@ include file="/WEB-INF/views/common/footer.jspf" %></body></html>
