<%--
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : instructors.jsp
WHAT IT DOES (simple words): Introduces instructors and lets learners sort their profiles.
REAL-LIFE EXAMPLE: A learner compares teachers before choosing who can guide their practice.
OOP USED : Not applicable (JSP presentation; the instructor catalog supplies profiles)
CRUD     : READ (shows active instructor profiles in the requested order)
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="Meet DriveSpark Academy instructors. Compare teaching experience, vehicle types and approved learner ratings.">
<meta name="theme-color" content="#050505"><title>Our instructors | DriveSpark Academy</title>
<link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
<link rel="stylesheet" href="<c:url value='/css/tailwind.css' />">
</head><body class="ds-vignette-page">
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-public ds-container ds-instructors-public">
    <%-- PublicInstructorController supplies active instructor profiles and the chosen sort settings. --%>
    <%-- The shared edge shade stays behind the heading and keeps the next content block in place. --%>
    <header class="ds-public-heading ds-photo-surface ds-page-photo ds-edge-vignette"><c:set var="photoKey" value="promo-lesson" /><c:set var="photoAlt" value="An instructor guiding a learner during a driving lesson" /><c:set var="photoEager" value="true" /><%@ include file="/WEB-INF/views/common/components/local-photo.jspf" %><p class="ds-eyebrow">OUR TEACHING TEAM</p><h1>Our instructors.</h1><p>Compare teaching experience, lesson types and student ratings.</p></header>
    <%-- PublicInstructorController passes these original parameters to the hand-written bubble sort. --%>
    <form class="ds-instructor-sort" method="get" action="<c:url value='/instructors' />">
        <label for="instructor-sort">Sort by<select id="instructor-sort" name="sortBy"><option value="name" <c:if test="${sortBy eq 'name'}">selected</c:if>>Name</option><option value="experience" <c:if test="${sortBy eq 'experience'}">selected</c:if>>Experience</option><option value="rating" <c:if test="${sortBy eq 'rating'}">selected</c:if>>Average rating</option></select></label>
        <label for="instructor-order">Order<select id="instructor-order" name="direction"><option value="asc" <c:if test="${direction eq 'asc'}">selected</c:if>>Ascending</option><option value="desc" <c:if test="${direction eq 'desc'}">selected</c:if>>Descending</option></select></label>
        <button class="ds-button ds-button-outline" type="submit">Sort</button>
    </form>
    <c:choose><c:when test="${empty instructors}"><section class="ds-public-empty"><h2>No instructors are listed right now.</h2><p>Contact us to ask about lesson availability.</p><a class="ds-home-text-link" href="<c:url value='/contact' />">Ask about lesson times</a></section></c:when><c:otherwise>
        <div class="ds-instructor-list">
            <c:forEach items="${instructors}" var="instructor">
                <%-- PublicInstructorController supplies this saved profile. Only local portraits are loaded. --%>
                <article class="ds-instructor-row" data-reveal>
                    <c:choose><c:when test="${not empty instructor.photoPath and fn:startsWith(instructor.photoPath, '/') and not fn:startsWith(instructor.photoPath, '//')}">
                        <c:url value="${instructor.photoPath}" var="instructorPhotoUrl" /><img class="ds-avatar-photo" src="<c:out value='${instructorPhotoUrl}' />" width="96" height="96" loading="lazy" alt="Portrait of <c:out value='${instructor.name}' />">
                    </c:when><c:otherwise><div class="ds-avatar" aria-hidden="true"><c:out value="${fn:substring(instructor.name,0,1)}" /></div></c:otherwise></c:choose>
                    <div class="ds-instructor-copy"><h2><c:out value="${instructor.name}" /></h2><p><c:out value="${instructor.bio}" /></p></div>
                    <div class="ds-instructor-summary"><p><c:out value="${instructor.yearsOfExperience}" /> years of experience</p><p class="ds-instructor-rating"><c:set var="ratingValue" value="${instructor.averageRating}" /><%@ include file="/WEB-INF/views/common/components/star-display.jspf" %><span><c:out value="${publicDisplay.rating(instructor.averageRating)}" /> / 5</span></p><p>Lesson types: <c:forEach items="${instructor.vehicleTypes}" var="vehicleType" varStatus="vehicleLoop"><c:out value="${publicDisplay.label(vehicleType)}" /><c:if test="${not vehicleLoop.last}">, </c:if></c:forEach></p></div>
                </article>
            </c:forEach>
        </div>
    </c:otherwise></c:choose>
    <%-- PublicInstructorController keeps the next step as a normal link, without submitting a booking. --%>
    <section class="ds-public-closing"><div><p class="ds-eyebrow">LESSON PACKAGES</p><h2>Choose your course.</h2></div><a class="ds-button ds-button-outline" href="<c:url value='/packages' />">Explore packages</a></section>
</main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body></html>
