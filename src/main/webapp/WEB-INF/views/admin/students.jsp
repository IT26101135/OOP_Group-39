<%--
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : students.jsp
WHAT IT DOES (simple words): Shows students information and controls.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html><html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Find student accounts, edit contact details and manage account approval.">
    <title>Manage students | DriveSpark Academy</title>
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <link rel="stylesheet" href="<c:url value='/css/tailwind.css' />">
</head>
<body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-container ds-section ds-dashboard-shell">
    <%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %>
    <div class="ds-dashboard-content">
        <p class="ds-eyebrow">STUDENT ADMINISTRATION</p><h1>Manage student accounts</h1>
        <%-- AdminManagementController supplies the search query and matching students. --%>
        <form class="ds-card mb-6 flex flex-wrap items-end gap-3" method="get" action="<c:url value='/admin/students' />">
            <div class="min-w-0 flex-1"><label for="student-search">Search by ID, name, or email</label><input class="block w-full" id="student-search" name="q" value="<c:out value='${query}' />"></div>
            <button class="ds-button" type="submit">Search</button>
        </form>
        <c:choose>
            <c:when test="${empty students}"><div class="ds-empty-state"><h2>No matching students</h2><p>Try a different name, email or student ID.</p></div></c:when>
            <c:otherwise>
                <div class="grid gap-5">
                <c:forEach items="${students}" var="student">
                    <%-- This card shows one account from AdminManagementController. --%>
                    <article class="ds-feature-card">
                        <h2><c:out value="${student.name}" /> <span class="text-sm text-slate-400">(<c:out value="${student.id}" />)</span></h2>
                        <p class="mb-5"><span class="status-badge" data-status="<c:out value='${student.status}' />"><c:out value="${student.status}" /></span> Created <c:out value="${fn:substring(student.createdAt, 0, 10)}" /></p>
                        <%-- AdminManagementController accepts only the existing contact fields. --%>
                        <form class="grid gap-4 sm:grid-cols-2" method="post" action="<c:url value='/admin/students/update' />">
                            <input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="<c:out value='${student.id}' />">
                            <div><label for="name-<c:out value='${student.id}' />">Name</label><input class="block w-full" id="name-<c:out value='${student.id}' />" name="name" value="<c:out value='${student.name}' />" required minlength="2" maxlength="100"></div>
                            <div><label for="email-<c:out value='${student.id}' />">Email</label><input class="block w-full" id="email-<c:out value='${student.id}' />" name="email" type="email" value="<c:out value='${student.email}' />" required maxlength="254"></div>
                            <div><label for="phone-<c:out value='${student.id}' />">Phone</label><input class="block w-full" id="phone-<c:out value='${student.id}' />" name="phone" type="tel" value="<c:out value='${student.phone}' />" required maxlength="16"></div>
                            <div class="flex items-end"><button class="ds-button" type="submit">Save student</button></div>
                        </form>
                        <%-- AdminManagementController checks approval and suspension requests. --%>
                        <form class="mt-5 flex flex-wrap items-end gap-3 border-t border-white/10 pt-5" method="post" action="<c:url value='/admin/students/status' />">
                            <input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="<c:out value='${student.id}' />">
                            <div><label for="status-<c:out value='${student.id}' />">Account status</label><select class="block" id="status-<c:out value='${student.id}' />" name="status"><c:forEach items="${statuses}" var="status"><option value="<c:out value='${status}' />" <c:if test="${student.status eq status}">selected</c:if>><c:out value="${status}" /></option></c:forEach></select></div>
                            <button class="ds-button ds-button-outline" type="submit">Update status</button>
                        </form>
                        <%-- AdminManagementController protects accounts with attached learning history. --%>
                        <form class="mt-5" method="post" action="<c:url value='/admin/students/delete' />" data-confirm="Delete this student account?">
                            <input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="id" value="<c:out value='${student.id}' />">
                            <button class="ds-button ds-button-danger" type="submit">Delete student</button>
                        </form>
                    </article>
                </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body></html>
