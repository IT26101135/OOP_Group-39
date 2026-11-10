<%--
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : instructors.jsp
WHAT IT DOES (simple words): Shows instructors information and forms.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Not applicable (JSP presentation)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Manage instructors | DriveSpark Academy</title><meta name="description" content="Manage instructor accounts, contact details and teaching profiles.">
<link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />"><link rel="stylesheet" href="<c:url value='/css/tailwind.css' />"></head><body class="ds-account-page">
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-container ds-section ds-dashboard-shell">
    <%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content">
<p class="ds-eyebrow">TEACHING TEAM</p><h1>Manage instructors</h1>
<%-- AdminManagementController supplies instructor records and accepts each complete account/profile form. --%>
<%-- AdminManagementController supplies this page section. --%>
<section class="ds-card mb-8"><h2>Add an instructor</h2><%-- AdminManagementController supplies this form and its original field names. --%>
<form class="mt-5 grid gap-4 sm:grid-cols-2" method="post" action="<c:url value='/admin/instructors' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><div><label for="name-new">Full name</label><input class="block w-full" id="name-new" name="name" type="text" required minlength="2" maxlength="100"></div>
<div><label for="email-new">Email</label><input class="block w-full" id="email-new" name="email" type="email" required maxlength="254"></div>
<div><label for="phone-new">Phone</label><input class="block w-full" id="phone-new" name="phone" type="tel" required maxlength="16"></div>
<div><label for="yearsOfExperience-new">Experience (years)</label><input class="block w-full" id="yearsOfExperience-new" name="yearsOfExperience" type="number" required min="0" max="60"></div>
<div><label for="password-new">Initial password</label><input class="block w-full" id="password-new" name="password" type="password" required minlength="8" maxlength="72" autocomplete="new-password"></div>
<div class="sm:col-span-2"><label for="bio-new">Bio</label><textarea class="block w-full" id="bio-new" name="bio" maxlength="1000"></textarea></div>
<div><label for="licenseNumber-new">License number</label><input class="block w-full" id="licenseNumber-new" name="licenseNumber" type="text" maxlength="40"></div>
<div class="sm:col-span-2"><label for="qualifications-new">Qualifications</label><textarea class="block w-full" id="qualifications-new" name="qualifications" maxlength="500"></textarea></div>
<div><label for="photoPath-new">Photo path (optional)</label><input class="block w-full" id="photoPath-new" name="photoPath" type="text" maxlength="300"></div>
<fieldset class="sm:col-span-2"><legend class="mb-2 text-sm text-slate-300">Vehicle types taught</legend><div class="flex flex-wrap gap-4"><label><input type="checkbox" name="vehicleTypes" value="Manual"> Manual</label><label><input type="checkbox" name="vehicleTypes" value="Automatic"> Automatic</label><label><input type="checkbox" name="vehicleTypes" value="Motorcycle"> Motorcycle</label></div></fieldset>
<div class="sm:col-span-2"><button class="ds-button" type="submit">Add instructor</button></div></form></section>
<%-- AdminManagementController supplies this page section. --%>
<section aria-labelledby="instructor-records"><h2 id="instructor-records" class="mb-5">Instructor records</h2><c:choose><c:when test="${empty instructors}"><div class="ds-empty-state"><p>No instructors have been added. Use the form above to add one.</p></div></c:when><c:otherwise><div class="grid gap-5"><c:forEach items="${instructors}" var="instructor"><%-- AdminManagementController supplies this record card. --%>
<article class="ds-card"><div class="mb-4 flex flex-wrap items-start justify-between gap-3"><h3 class="text-xl"><c:out value="${instructor.name}" /></h3><span class="status-badge" data-status="${instructor.active ? 'ACTIVE' : 'INACTIVE'}">${instructor.active ? 'Active' : 'Inactive'}</span></div>
<%-- Sending contact and teaching fields together preserves profile details during a basic edit. --%>
<%-- AdminManagementController supplies this form and its original field names. --%>
<form class="grid gap-4 sm:grid-cols-2" method="post" action="<c:url value='/admin/instructors/update' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="${instructor.id}"><div><label for="name-${instructor.id}">Full name</label><input class="block w-full" id="name-${instructor.id}" name="name" type="text" required minlength="2" maxlength="100" value="<c:out value='${instructor.name}' />"></div>
<div><label for="email-${instructor.id}">Email</label><input class="block w-full" id="email-${instructor.id}" name="email" type="email" required maxlength="254" value="<c:out value='${instructor.email}' />"></div>
<div><label for="phone-${instructor.id}">Phone</label><input class="block w-full" id="phone-${instructor.id}" name="phone" type="tel" required maxlength="16" value="<c:out value='${instructor.phone}' />"></div>
<div><label for="yearsOfExperience-${instructor.id}">Experience (years)</label><input class="block w-full" id="yearsOfExperience-${instructor.id}" name="yearsOfExperience" type="number" required min="0" max="60" value="<c:out value='${instructor.yearsOfExperience}' />"></div>
<div class="sm:col-span-2"><label for="bio-${instructor.id}">Bio</label><textarea class="block w-full" id="bio-${instructor.id}" name="bio" maxlength="1000"><c:out value="${instructor.bio}" /></textarea></div>
<div><label for="licenseNumber-${instructor.id}">License number</label><input class="block w-full" id="licenseNumber-${instructor.id}" name="licenseNumber" type="text" maxlength="40" value="<c:out value='${instructor.licenseNumber}' />"></div>
<div class="sm:col-span-2"><label for="qualifications-${instructor.id}">Qualifications</label><textarea class="block w-full" id="qualifications-${instructor.id}" name="qualifications" maxlength="500"><c:out value="${instructor.qualifications}" /></textarea></div>
<div><label for="photoPath-${instructor.id}">Photo path (optional)</label><input class="block w-full" id="photoPath-${instructor.id}" name="photoPath" type="text" maxlength="300" value="<c:out value='${instructor.photoPath}' />"></div>
<fieldset class="sm:col-span-2"><legend class="mb-2 text-sm text-slate-300">Vehicle types taught</legend><div class="flex flex-wrap gap-4"><label><input type="checkbox" name="vehicleTypes" value="Manual" <c:if test="${instructor.teachesVehicleType('Manual')}">checked</c:if>> Manual</label><label><input type="checkbox" name="vehicleTypes" value="Automatic" <c:if test="${instructor.teachesVehicleType('Automatic')}">checked</c:if>> Automatic</label><label><input type="checkbox" name="vehicleTypes" value="Motorcycle" <c:if test="${instructor.teachesVehicleType('Motorcycle')}">checked</c:if>> Motorcycle</label></div></fieldset>
<div class="sm:col-span-2"><button class="ds-button" type="submit">Save instructor</button></div></form>
<div class="mt-5 flex flex-wrap gap-3 border-t border-white/10 pt-5">
<%-- AdminManagementController supplies this form and its original field names. --%>
<form method="post" action="<c:url value='/admin/instructors/status' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="${instructor.id}"><input type="hidden" name="active" value="${not instructor.active}"><button class="ds-button ds-button-outline" type="submit">${instructor.active ? 'Deactivate' : 'Activate'}</button></form>
<%-- AdminManagementController supplies this form and its original field names. --%>
<form method="post" action="<c:url value='/admin/instructors/delete' />" data-confirm="Delete this instructor?"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="${instructor.id}"><button class="ds-button ds-button-danger" type="submit">Delete instructor</button></form></div></article></c:forEach></div></c:otherwise></c:choose></section>
</div>
</main><%@ include file="/WEB-INF/views/common/footer.jspf" %></body></html>
