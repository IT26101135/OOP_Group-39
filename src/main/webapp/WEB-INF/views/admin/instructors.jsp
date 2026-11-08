<%-- Module owner: IT26101133 --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url value="/css/tailwind.css" var="appCssUrl" />
<!DOCTYPE html>
<html lang="en"><head>
    <meta name="description" content="Manage instructors at DriveSpark Academy. Explore lessons, learning resources and driving school information.">
    <meta name="theme-color" content="#0B1120">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />"><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Manage instructors</title><link rel="stylesheet" href="<c:out value='${appCssUrl}' />"></head>
<body><%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-container ds-section ds-dashboard-shell"><%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content"><h1>Manage instructors</h1>
<c:if test="${not empty success}"><p role="status"><c:out value="${success}" /></p></c:if>
<h2>Add instructor</h2>
<form method="post" action="<c:url value='/admin/instructors' />">
<input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />" />
<label>Name <input name="name" required maxlength="100"></label>
<label>Email <input name="email" type="email" required maxlength="254"></label>
<label>Phone <input name="phone" required></label>
<label>Years of experience <input name="yearsOfExperience" type="number" min="0" max="60" required></label>
<label>Initial password <input name="password" type="password" minlength="8" maxlength="72" autocomplete="new-password" required></label>
<label>Bio <textarea name="bio" maxlength="1000"></textarea></label>
<label>License number <input name="licenseNumber" maxlength="40"></label>
<label>Qualifications <textarea name="qualifications" maxlength="500"></textarea></label>
<label>Photo path (optional) <input name="photoPath" maxlength="300"></label>
<fieldset><legend>Vehicle types taught</legend><label><input type="checkbox" name="vehicleTypes" value="Manual">Manual</label><label><input type="checkbox" name="vehicleTypes" value="Automatic">Automatic</label><label><input type="checkbox" name="vehicleTypes" value="Motorcycle">Motorcycle</label></fieldset>
<button type="submit">Add instructor</button></form>
<h2>Instructor records</h2>
<c:choose><c:when test="${empty instructors}"><p>No instructors have been added.</p></c:when><c:otherwise><c:forEach items="${instructors}" var="instructor"><article class="ds-feature-card"><h2><c:out value="${instructor.name}" /> (<c:out value="${instructor.id}" />)</h2><form method="post" action="<c:url value='/admin/instructors/update' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="<c:out value='${instructor.id}' />"><label>Name <input name="name" value="<c:out value='${instructor.name}' />" required minlength="2" maxlength="100"></label><label>Email <input name="email" type="email" value="<c:out value='${instructor.email}' />" required></label><label>Phone <input name="phone" value="<c:out value='${instructor.phone}' />" required></label><label>Experience <input name="yearsOfExperience" type="number" min="0" max="60" value="<c:out value='${instructor.yearsOfExperience}' />" required></label><button type="submit">Save instructor</button></form><form method="post" action="<c:url value='/admin/instructors/profile' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="<c:out value='${instructor.id}' />"><label>Bio <textarea name="bio" maxlength="1000"><c:out value="${instructor.bio}" /></textarea></label><label>License number <input name="licenseNumber" value="<c:out value='${instructor.licenseNumber}' />"></label><label>Qualifications <textarea name="qualifications" maxlength="500"><c:out value="${instructor.qualifications}" /></textarea></label><label>Photo path <input name="photoPath" value="<c:out value='${instructor.photoPath}' />"></label><label>Vehicle types (comma separated) <input name="vehicleTypes" value="<c:forEach items='${instructor.vehicleTypes}' var='type' varStatus='loop'><c:if test='${!loop.first}'>,</c:if><c:out value='${type}' /></c:forEach>"></label><button type="submit">Save profile</button></form><p>Login: <c:out value="${instructor.email}" /> Â· Active: <c:out value="${instructor.active}" /></p><form method="post" action="<c:url value='/admin/instructors/status' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="<c:out value='${instructor.id}' />"><input type="hidden" name="active" value="${not instructor.active}"><button type="submit"><c:choose><c:when test="${instructor.active}">Deactivate</c:when><c:otherwise>Activate</c:otherwise></c:choose></button></form><form method="post" action="<c:url value='/admin/instructors/delete' />" data-confirm="Delete this instructor?"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="<c:out value='${instructor.id}' />"><button type="submit">Delete</button></form></article></c:forEach></c:otherwise></c:choose>
</div></main><%@ include file="/WEB-INF/views/common/footer.jspf" %></body></html>
