<%--
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and shared UI
FILE     : about.jsp
WHAT IT DOES (simple words): Presents about us with clear links and readable details.
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
<meta name="description" content="Learn about our lessons, booking process and instructor feedback.">
<title>About us | DriveSpark Academy</title>
<link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
<link rel="stylesheet" href="<c:url value='/css/tailwind.css' />">
</head><body class="ds-vignette-page">
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main id="main-content" class="ds-public ds-about-public">
    <%-- PublicPagesController opens the academy story; LocalPhotoAdvice supplies the optional landscape photo. --%>
    <header class="ds-public-heading ds-container ds-about-intro"><p class="ds-eyebrow">ABOUT OUR SCHOOL</p><h1>Learn with<br>clear guidance.</h1><p>We help you plan your practice, book lessons and keep track of what you've learned.</p><a class="ds-button" href="<c:url value='/packages' />">Explore learning packages</a></header>
    <%-- PublicPagesController keeps this landscape between the same two sections; only its edges fade. --%>
    <div class="ds-about-landscape ds-page-photo ds-edge-vignette" role="img" aria-label="Mountain road landscape"><c:set var="photoKey" value="mission" /><c:set var="photoAlt" value="A road through the mountains" /><c:set var="photoEager" value="false" /><%@ include file="/WEB-INF/views/common/components/local-photo.jspf" %><div class="ds-scene-road"><div class="ds-road-lines"></div></div></div>
    <%-- PublicPagesController provides this static story and mission section. --%>
    <section class="ds-container ds-editorial-split"><p class="ds-eyebrow">THE DRIVESPARK APPROACH</p><div><h2>Practical lessons.<br>Clear feedback.</h2><p>Choose a course, compare instructors and request a lesson through your account.</p><p>We focus on observation, patient practice and respect for other road users.</p></div></section>
    <%-- PublicPagesController opens these values; no invented performance statistics are shown. --%>
    <section class="ds-container ds-values" aria-labelledby="values-title"><p class="ds-eyebrow">WHAT GUIDES US</p><h2 id="values-title">The details matter.</h2><div class="ds-values-grid"><article><span>01</span><h3>Safety first</h3><p>Every step starts with awareness and care.</p></article><article><span>02</span><h3>Clear progress</h3><p>Feedback makes the next step easier to understand.</p></article><article><span>03</span><h3>Respectful support</h3><p>Practice should be encouraging, focused and personal.</p></article><article><span>04</span><h3>Responsible choices</h3><p>Good driving leaves room for others.</p></article></div></section>
    <%-- PublicPagesController links to the existing instructor catalog. --%>
    <section class="ds-container ds-public-closing"><div><p class="ds-eyebrow">OUR INSTRUCTORS</p><h2>Meet your<br>instructor.</h2></div><a class="ds-button ds-button-outline" href="<c:url value='/instructors' />">Meet the instructors</a></section>
</main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body></html>
