<%--
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and enrollment
FILE     : packages.jsp
WHAT IT DOES (simple words): Shows packages information and controls.
REAL-LIFE EXAMPLE: A course folder keeps the learning plan and enrollment details together.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Manage driving courses, lesson counts, validity periods and student availability.">
    <title>Manage learning packages | DriveSpark Academy</title>
    <link rel="icon" href="<c:url value='/images/favicon.svg' />" type="image/svg+xml">
    <link rel="stylesheet" href="<c:url value='/css/tailwind.css' />">
</head>
<body class="ds-account-page">
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-container ds-section ds-dashboard-shell">
    <%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %>
    <div class="ds-dashboard-content">
        <p class="ds-eyebrow">LEARNING CATALOG</p><h1>Manage learning packages</h1>
        <%-- AdminManagementController supplies the supported course types and accepts new packages. --%>
        <section class="ds-card mb-8" aria-labelledby="add-package-heading">
            <h2 id="add-package-heading">Add a learning package</h2>
            <form class="mt-5 grid gap-4 sm:grid-cols-2" method="post" action="<c:url value='/admin/packages' />">
<input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />">
<div><label for="name-new">Name</label><input class="block w-full" id="name-new" name="name" type="text" minlength="2" maxlength="100" required></div>
<div class="sm:col-span-2"><label for="description-new">Description</label><textarea class="block w-full" id="description-new" name="description" minlength="10" maxlength="1000" required></textarea></div>
<div><label for="price-new">Price (LKR)</label><input class="block w-full" id="price-new" name="price" type="number" min="0.01" max="1000000" step="0.01" required></div>
<div><label for="lessonCount-new">Lessons</label><input class="block w-full" id="lessonCount-new" name="lessonCount" type="number" min="1" max="100" required></div>
<div><label for="validityDays-new">Validity (days)</label><input class="block w-full" id="validityDays-new" name="validityDays" type="number" min="1" max="365" required></div>
<div><label for="type-new">Course type</label><select class="block w-full" id="type-new" name="type" required><c:forEach items="${packageTypes}" var="packageType"><option value="<c:out value='${packageType}' />"><c:out value="${publicDisplay.label(packageType)}" /></option></c:forEach></select></div>
<div><label for="durationHours-new">Duration (hours)</label><input class="block w-full" id="durationHours-new" name="durationHours" type="number" min="1" max="1000" required></div>
                <label class="flex items-center gap-2"><input name="active" type="checkbox" value="true" checked> Available to students</label>
                <div class="sm:col-span-2"><button class="ds-button" type="submit">Add package</button></div>
            </form>
        </section>
        <%-- AdminManagementController lists the existing packages and accepts their original update fields. --%>
        <section aria-labelledby="package-records-heading">
            <h2 id="package-records-heading" class="mb-5">Package records</h2>
            <c:choose>
                <c:when test="${empty packages}"><div class="ds-empty-state"><p>No packages have been added yet. Use the form above to add the first course.</p><a class="ds-home-text-link" href="#add-package-heading">Add a package</a></div></c:when>
                <c:otherwise><div class="grid gap-5"><c:forEach items="${packages}" var="learningPackage">
                    <article class="ds-card">
                        <div class="mb-5 flex flex-wrap items-center justify-between gap-3"><h3 class="text-xl"><c:out value="${learningPackage.name}" /></h3><span class="status-badge" data-status="${learningPackage.active ? 'ACTIVE' : 'INACTIVE'}">${learningPackage.active ? 'Available' : 'Inactive'}</span></div>
                        <form class="grid gap-4 sm:grid-cols-2" method="post" action="<c:url value='/admin/packages/update' />">
<input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="<c:out value='${learningPackage.id}' />">
<div><label for="name-${learningPackage.id}">Name</label><input class="block w-full" id="name-${learningPackage.id}" name="name" type="text" minlength="2" maxlength="100" required value="<c:out value='${learningPackage.name}' />"></div>
<div class="sm:col-span-2"><label for="description-${learningPackage.id}">Description</label><textarea class="block w-full" id="description-${learningPackage.id}" name="description" minlength="10" maxlength="1000" required><c:out value="${learningPackage.description}" /></textarea></div>
<div><label for="price-${learningPackage.id}">Price (LKR)</label><input class="block w-full" id="price-${learningPackage.id}" name="price" type="number" min="0.01" max="1000000" step="0.01" required value="<c:out value='${learningPackage.price}' />"></div>
<div><label for="lessonCount-${learningPackage.id}">Lessons</label><input class="block w-full" id="lessonCount-${learningPackage.id}" name="lessonCount" type="number" min="1" max="100" required value="<c:out value='${learningPackage.lessonCount}' />"></div>
<div><label for="validityDays-${learningPackage.id}">Validity (days)</label><input class="block w-full" id="validityDays-${learningPackage.id}" name="validityDays" type="number" min="1" max="365" required value="<c:out value='${learningPackage.validityDays}' />"></div>
<div><label for="type-${learningPackage.id}">Course type</label><select class="block w-full" id="type-${learningPackage.id}" name="type" required><c:forEach items="${packageTypes}" var="packageType"><option value="<c:out value='${packageType}' />" <c:if test="${learningPackage.type eq packageType}">selected</c:if>><c:out value="${publicDisplay.label(packageType)}" /></option></c:forEach></select></div>
<div><label for="durationHours-${learningPackage.id}">Duration (hours)</label><input class="block w-full" id="durationHours-${learningPackage.id}" name="durationHours" type="number" min="1" max="1000" required value="<c:out value='${learningPackage.durationHours}' />"></div>
                            <div class="sm:col-span-2"><button class="ds-button" type="submit">Save package</button></div>
                        </form>
                        <%-- AdminManagementController changes availability separately from package details. --%>
                        <div class="mt-5 flex flex-wrap gap-3 border-t border-white/10 pt-5">
                            <form method="post" action="<c:url value='/admin/packages/status' />">
<input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="<c:out value='${learningPackage.id}' />">
                                <input type="hidden" name="active" value="${not learningPackage.active}">
                                <button class="ds-button ds-button-outline" type="submit">${learningPackage.active ? 'Deactivate' : 'Activate'}</button>
                            </form>
                            <%-- AdminManagementController refuses deletion when learning history refers to this course. --%>
                            <form method="post" action="<c:url value='/admin/packages/delete' />" data-confirm="Delete this package?">
<input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="<c:out value='${learningPackage.id}' />">
                                <button class="ds-button ds-button-danger" type="submit">Delete package</button>
                            </form>
                        </div>
                    </article>
                </c:forEach></div></c:otherwise>
            </c:choose>
        </section>
    </div>
</main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body></html>
