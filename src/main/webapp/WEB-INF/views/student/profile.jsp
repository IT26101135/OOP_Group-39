<%--
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : profile.jsp
WHAT IT DOES (simple words): Welcomes the student and shows their saved details and learning plan.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url value="/css/tailwind.css" var="appCssUrl" />
<c:url value="/student/profile" var="studentProfileUrl" />
<c:url value="/student/profile/edit" var="studentProfileEditUrl" />
<c:url value="/student/password" var="studentPasswordUrl" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="description" content="Manage your student profile, check your lesson balance and plan your next DriveSpark Academy lesson.">
    <meta name="theme-color" content="#050505">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>My profile | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
</head>
<body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-container ds-section ds-profile ds-dashboard-shell"><%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content">
    <%-- StudentProfileController supplies this welcome message and the saved learning summary. --%>
    <header class="ds-workspace-heading"><p class="ds-eyebrow">YOUR LEARNING JOURNEY</p>
    <h1>Welcome, <c:out value="${profile.name}" /></h1>
    <p class="mb-6 text-slate-400">Keep your details up to date and plan your next lesson.</p></header>
    <%-- This block shows a page section. StudentProfileController, StudentPasswordController or StudentProgressController supplies the page data. --%>
<section class="ds-workspace-overview ds-student-overview" aria-label="Learning overview">
        <%-- This block shows one saved record. StudentProfileController, StudentPasswordController or StudentProgressController supplies the page data. --%>
<article class="ds-card"><p class="ds-eyebrow">LESSONS REMAINING</p><strong class="ds-workspace-number" data-counter="<c:out value='${profile.lessonsRemaining}' />"><c:out value="${profile.lessonsRemaining}" /></strong><p><a href="<c:url value='/bookings' />">Plan a lesson</a></p></article>
        <%-- This block shows one saved record. StudentProfileController, StudentPasswordController or StudentProgressController supplies the page data. --%>
<article class="ds-card"><p class="ds-eyebrow">CURRENT PACKAGE</p><strong class="ds-workspace-package"><c:out value="${profile.activePackageId}" default="No active package" /></strong><p><a href="<c:url value='/enrollments' />">View my enrollments</a></p></article>
    </section>
    <c:if test="${passwordChanged eq true}"><p role="status">Password changed.</p></c:if>
    <c:set var="tabsLabel" value="Profile sections" />
    <c:set var="firstTabTitle" value="Personal details" />
    <c:set var="secondTabTitle" value="Account settings" />
    <c:set var="firstPanelId" value="profile-details" />
    <c:set var="secondPanelId" value="profile-settings" />
    <%@ include file="/WEB-INF/views/common/components/tabs.jspf" %>
    <%-- This block shows a page section. StudentProfileController, StudentPasswordController or StudentProgressController supplies the page data. --%>
<section id="profile-details" class="ds-card mt-4">
    <h2>Personal details</h2>
    <c:if test="${profileUpdated eq true}">
        <p class="ds-profile-message" role="status">Your profile has been saved.</p>
    </c:if>
    <%-- This block shows the saved details. StudentProfileController, StudentPasswordController or StudentProgressController supplies the page data. --%>
<dl class="ds-profile-details">
        <dt>Student ID</dt><dd><c:out value="${profile.id}" /></dd>
        <dt>Full name</dt><dd><c:out value="${profile.name}" /></dd>
        <dt>Email</dt><dd><c:out value="${profile.email}" /></dd>
        <dt>Phone number</dt><dd><c:out value="${profile.phone}" /></dd>
        <dt>NIC</dt><dd><c:out value="${profile.nic}" /></dd>
        <dt>Date of birth</dt><dd><c:out value="${profile.dateOfBirth}" /></dd>
        <dt>Address</dt><dd><c:out value="${profile.address}" /></dd>
        <dt>License type wanted</dt><dd><c:out value="${profile.licenseTypeWanted}" /></dd>
        <dt>Emergency contact</dt><dd><c:out value="${profile.emergencyContact}" /></dd>
        <dt>Active package</dt><dd><c:out value="${profile.activePackageId}" /></dd>
        <dt>Lessons remaining</dt><dd><c:out value="${profile.lessonsRemaining}" /></dd>
    </dl>
    <a class="ds-button" href="<c:out value='${studentProfileEditUrl}' />">Edit profile</a>
    </section>
    <%-- This block shows a page section. StudentProfileController, StudentPasswordController or StudentProgressController supplies the page data. --%>
<section id="profile-settings" class="ds-card mt-4">
    <h2>Account settings</h2>
    <p class="mb-4 text-slate-400">Choose a strong password. Accounts with learning history are protected from deletion.</p>
    <a class="ds-button" href="<c:out value='${studentPasswordUrl}' />">Change password</a>
    <%-- This block shows a form with its existing field names. StudentProfileController, StudentPasswordController or StudentProgressController supplies the page data. --%>
<form class="mt-6 border-t border-white/10 pt-6" method="post" action="<c:url value='/student/account/delete' />"
          data-confirm="Delete this account? This cannot be undone.">
        <input type="hidden" name="<c:out value='${csrfParameterName}' />"
               value="<c:out value='${csrfToken}' />">
        <button class="ds-button ds-button-danger" type="submit">Delete my account</button>
    </form>
    </section>
</div></main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
