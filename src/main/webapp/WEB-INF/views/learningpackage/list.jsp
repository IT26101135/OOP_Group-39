<%--
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and shared UI
FILE     : list.jsp
WHAT IT DOES (simple words): Presents learning packages with clear links and readable details.
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
<meta name="description" content="Explore lesson types, prices and validity periods to find your DriveSpark Academy learning path.">
<title>Learning packages | DriveSpark Academy</title>
<link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
<link rel="stylesheet" href="<c:url value='/css/tailwind.css' />">
</head><body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main id="main-content" class="ds-public ds-catalog ds-container">
    <%-- LearningPackageController supplies active packages and their saved prices. --%>
    <header class="ds-public-heading"><p class="ds-eyebrow">THE ROAD IS YOURS TO LEARN</p><h1>Find your<br>learning path.</h1><p>Choose your vehicle type. Compare the lessons, time and support for your next chapter.</p></header>
    <%-- The existing shared JavaScript filters these records; without it all packages remain visible. --%>
    <div class="ds-filter-pills ds-catalog-filters" role="group" aria-label="Filter packages by type">
        <button type="button" class="ds-filter-pill is-active" data-package-filter="all" aria-pressed="true">All types</button>
        <button type="button" class="ds-filter-pill" data-package-filter="MANUAL" aria-pressed="false">Manual</button>
        <button type="button" class="ds-filter-pill" data-package-filter="AUTOMATIC" aria-pressed="false">Automatic</button>
        <button type="button" class="ds-filter-pill" data-package-filter="MOTORCYCLE" aria-pressed="false">Motorcycle</button>
        <button type="button" class="ds-filter-pill" data-package-filter="REFRESHER" aria-pressed="false">Refresher</button>
    </div>
    <noscript><p>All available packages are shown below. Choose a package to view its details.</p></noscript>
    <c:choose><c:when test="${empty packages}"><section class="ds-public-empty"><h2>Your next chapter is on its way.</h2><p>No active packages are available right now.</p><a class="ds-home-text-link" href="<c:url value='/contact' />">Ask about learning options</a></section></c:when><c:otherwise>
        <div class="ds-catalog-grid">
            <c:forEach items="${packages}" var="learningPackage">
                <%-- LearningPackageController provides each package; c:out keeps saved text safe. --%>
                <article class="ds-catalog-panel" data-package-card data-package-type="<c:out value='${learningPackage.type}' />" data-reveal>
                    <div class="ds-catalog-photo ds-photo-surface"><c:set var="photoVehicleType" value="${learningPackage.type}" /><c:choose><c:when test="${photoVehicleType eq 'AUTOMATIC'}"><c:set var="photoKey" value="vehicle-automatic" /></c:when><c:when test="${photoVehicleType eq 'MOTORCYCLE'}"><c:set var="photoKey" value="vehicle-motorcycle" /></c:when><c:otherwise><c:set var="photoKey" value="vehicle-manual" /></c:otherwise></c:choose><c:set var="photoAlt" value="Vehicle illustrating ${photoVehicleType} driving lessons" /><c:set var="photoEager" value="false" /><%@ include file="/WEB-INF/views/common/components/local-photo.jspf" %></div>
                    <p class="ds-eyebrow"><c:out value="${learningPackage.type}" /> / LEARNING PACKAGE</p>
                    <h2><c:out value="${learningPackage.displayName}" /></h2>
                    <p class="ds-catalog-description"><c:out value="${learningPackage.description}" /></p>
                    <p class="ds-catalog-price"><span>LKR</span> <c:out value="${learningPackage.price}" /></p>
                    <dl class="ds-public-facts"><div><dt>Lessons</dt><dd><c:out value="${learningPackage.lessonCount}" /></dd></div><div><dt>Driving time</dt><dd><c:out value="${learningPackage.durationHours}" /> hours</dd></div><div><dt>Validity after payment</dt><dd><c:out value="${learningPackage.validityDays}" /> days</dd></div></dl>
                    <c:url value="/packages/${learningPackage.id}" var="packageDetailUrl" />
                    <a class="ds-button ds-button-outline" href="<c:out value='${packageDetailUrl}' />">View package</a>
                </article>
            </c:forEach>
        </div><p class="ds-filter-empty" hidden>No packages match this type.</p>
    </c:otherwise></c:choose>
    <p class="ds-public-note">Prices are from the current project data. Pay outside the system by bank transfer or at our office. Lessons unlock after payment confirmation.</p>
</main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body></html>
