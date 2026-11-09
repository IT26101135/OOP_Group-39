<%--
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and shared UI
FILE     : detail.jsp
WHAT IT DOES (simple words): Explains a package and shows how a student can enrol.
REAL-LIFE EXAMPLE: A welcome desk helps learners choose their next step.
OOP USED : Not applicable (JSP presentation; controllers supply saved data)
CRUD     : READ (displays page information); existing forms keep their original operations
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="theme-color" content="#050505">
<meta name="description" content="Explore this DriveSpark Academy package, its lesson count and enrolment details.">
<title><c:out value='${learningPackage.displayName}' /> | DriveSpark Academy</title>
<link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
<link rel="stylesheet" href="<c:url value='/css/tailwind.css' />">
</head><body class="ds-vignette-page">
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main id="main-content" class="ds-public ds-detail ds-container">
    <%-- LearningPackageController supplies this package and the navigation role. --%>
    <nav class="ds-breadcrumbs" aria-label="Breadcrumb"><a href="<c:url value='/packages' />">Packages</a><span aria-hidden="true">/</span><span aria-current="page"><c:out value="${learningPackage.displayName}" /></span></nav>
    <%-- The shared edge shade stays behind the heading and keeps the next content block in place. --%>
    <header class="ds-public-heading ds-detail-heading ds-photo-surface ds-page-photo ds-edge-vignette"><c:set var="photoVehicleType" value="${learningPackage.type}" /><c:choose><c:when test="${photoVehicleType eq 'AUTOMATIC'}"><c:set var="photoKey" value="vehicle-automatic" /></c:when><c:when test="${photoVehicleType eq 'MOTORCYCLE'}"><c:set var="photoKey" value="vehicle-motorcycle" /></c:when><c:otherwise><c:set var="photoKey" value="vehicle-manual" /></c:otherwise></c:choose><c:set var="photoAlt" value="Vehicle illustrating ${photoVehicleType} driving lessons" /><c:set var="photoEager" value="true" /><%@ include file="/WEB-INF/views/common/components/local-photo.jspf" %><p class="ds-eyebrow"><c:out value="${learningPackage.type}" /> / YOUR NEXT CHAPTER</p><h1><c:out value="${learningPackage.displayName}" /></h1><p><c:out value="${learningPackage.description}" /></p></header>
    <div class="ds-detail-grid">
        <%-- LearningPackageController provides the saved duration and validity used in these details. --%>
        <div class="ds-detail-story"><section><p class="ds-eyebrow">PURPOSEFUL PRACTICE</p><h2>One lesson closer<br>to confidence.</h2><p>A structured learning path with instructor-guided practice and feedback after your lessons.</p><ul class="ds-detail-inclusions"><li>Individual driving lessons</li><li>Instructor feedback and skill notes</li><li>Lesson balance in your student account after payment confirmation</li></ul></section><section><p class="ds-eyebrow">MAKE TIME TO LEARN</p><h2>Room to build<br>good habits.</h2><p>This package is valid for <c:out value="${learningPackage.validityDays}" /> days from payment confirmation. Plan your lessons within that period and request available times through your account.</p><a class="ds-home-text-link" href="<c:url value='/faq' />">Questions before you start? <span aria-hidden="true">↗</span></a></section></div>
        <%-- LearningPackageController supplies this summary; the existing enrollment POST and CSRF fields stay unchanged. --%>
        <aside class="ds-detail-enrol" aria-label="Package enrolment"><p class="ds-eyebrow">YOUR LEARNING PLAN</p><p class="ds-catalog-price"><span>LKR</span> <c:out value="${learningPackage.price}" /></p>
            <dl class="ds-public-facts"><div><dt>Course type</dt><dd><c:out value="${learningPackage.type}" /></dd></div><div><dt>Lessons</dt><dd><c:out value="${learningPackage.lessonCount}" /></dd></div><div><dt>Driving time</dt><dd><c:out value="${learningPackage.durationHours}" /> hours</dd></div><div><dt>Validity</dt><dd><c:out value="${learningPackage.validityDays}" /> days</dd></div></dl>
            <c:choose><c:when test="${navRole eq 'STUDENT'}">
                <form method="post" action="<c:url value='/enrollments' />"><input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />"><input type="hidden" name="packageId" value="<c:out value='${learningPackage.id}' />"><button class="ds-button" type="submit">Enroll in this package</button></form><p class="ds-public-note">Enrollment creates an unpaid request. Pay by bank transfer or at our office; lessons unlock after payment confirmation.</p>
            </c:when><c:when test="${navRole eq 'PUBLIC'}"><a class="ds-button" href="<c:url value='/login' />">Log in to enroll</a><p class="ds-public-note">Sign in or create a student account to start enrolment.</p></c:when></c:choose>
        </aside>
    </div>
</main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body></html>
