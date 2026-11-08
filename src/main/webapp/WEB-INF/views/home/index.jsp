<%--
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Home and shared foundation
FILE     : index.jsp
WHAT IT DOES (simple words): Introduces the academy and links learners to their next step.
REAL-LIFE EXAMPLE: Like walking through the school's welcome area before choosing a course.
OOP USED : Not applicable (JSP presentation; controllers supply saved records)
CRUD     : READ (shows active packages and links to approved public reviews)
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url value="/css/tailwind.css" var="appCssUrl" />
<c:url value="/packages" var="packagesUrl" />
<c:url value="/bookings" var="bookingsUrl" />
<c:url value="/instructors" var="instructorsUrl" />
<c:url value="/contact" var="contactUrl" />
<c:url value="/public/reviews" var="reviewsUrl" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="theme-color" content="#050505">
    <meta name="description" content="Learn to drive with confidence at DriveSpark Academy. Explore learning packages, meet instructors and plan your next lesson.">
    <title>DriveSpark Academy | Learn to drive with confidence</title>
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <link rel="stylesheet" href="${appCssUrl}">
</head>
<body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-home">
    <%-- 1. HomeController opens this welcome section; its links keep the existing booking checks. --%>
    <section class="ds-cinematic-section ds-cinematic-hero ds-home-hero" data-cinematic aria-labelledby="welcome-title">
        <div class="ds-cinematic-background ds-scene-road" aria-hidden="true"><div class="ds-road-lines"></div></div>
        <div class="ds-container">
            <div class="ds-home-copy" data-reveal>
                <p class="ds-eyebrow">DRIVESPARK ACADEMY / YOUR ROAD AHEAD</p>
                <h1 id="welcome-title">Learn to drive<br>with confidence.</h1>
                <p>Good habits. Clear guidance. The freedom to take your next turn with confidence.</p>
                <div class="ds-home-actions"><a class="ds-button" href="${bookingsUrl}">Book a lesson</a><a class="ds-button ds-button-outline" href="${packagesUrl}">View packages</a></div>
            </div>
            <a class="ds-scroll-cue" href="#monthly-package">Discover your next chapter <span aria-hidden="true">↓</span></a>
        </div>
    </section>

    <%-- 2. HomeController supplies active packages. The first is an editorial feature, not a claimed discount. --%>
    <section id="monthly-package" class="ds-cinematic-section ds-home-promo" aria-labelledby="promo-title">
        <div class="ds-cinematic-background ds-scene-contours" aria-hidden="true"></div>
        <div class="ds-container"><div class="ds-home-copy" data-reveal>
            <p class="ds-eyebrow">THIS MONTH'S LEARNING SPOTLIGHT</p>
            <c:choose><c:when test="${not empty featuredPackages}">
                <c:set var="spotlight" value="${featuredPackages[0]}" />
                <h2 id="promo-title"><c:out value="${spotlight.name}" /></h2>
                <p><c:out value="${spotlight.lessonCount}" /> lessons to build your next chapter. LKR <c:out value="${spotlight.price}" />. Explore the full package before enrolling.</p>
                <c:url value="/packages/${spotlight.id}" var="spotlightUrl" />
                <div class="ds-home-actions"><a class="ds-button" href="${packagesUrl}">See packages</a><a class="ds-button ds-button-outline" href="<c:out value='${spotlightUrl}' />">Enrol</a></div>
            </c:when><c:otherwise>
                <h2 id="promo-title">Your next chapter<br>starts here.</h2>
                <p>New learning packages will appear when available. Talk to us about your goals in the meantime.</p>
                <div class="ds-home-actions"><a class="ds-button" href="${packagesUrl}">See packages</a><a class="ds-button ds-button-outline" href="${contactUrl}">Ask about enrolment</a></div>
            </c:otherwise></c:choose>
        </div></div>
    </section>

    <%-- 3. HomeController supplies this teaser. LearningPackageController opens the normal catalog. --%>
    <section class="ds-home-section ds-home-builder" aria-labelledby="lesson-title">
        <div class="ds-container" data-reveal><p class="ds-eyebrow">MAKE IT YOUR JOURNEY</p><h2 id="lesson-title">Find your lesson.</h2>
            <p>Choose the kind of driving you want to learn. Find a package that fits.</p>
            <nav class="ds-lesson-choices" aria-label="Choose a lesson type">
                <a href="<c:url value='/packages?type=MANUAL' />">Manual <span aria-hidden="true">↗</span></a>
                <a href="<c:url value='/packages?type=AUTOMATIC' />">Automatic <span aria-hidden="true">↗</span></a>
                <a href="<c:url value='/packages?type=MOTORCYCLE' />">Motorcycle <span aria-hidden="true">↗</span></a>
            </nav>
            <noscript><p>All available types are listed on the packages page.</p></noscript>
        </div>
    </section>

    <%-- 4. HomeController provides the static overview; links open existing controllers. Without JS all panels show. --%>
    <section class="ds-home-section ds-home-overview" aria-labelledby="overview-title">
        <div class="ds-container"><p class="ds-eyebrow">A LITTLE GUIDANCE GOES A LONG WAY</p><h2 id="overview-title">Everything for<br>the road ahead.</h2>
            <div class="ds-home-tabs" data-tabs aria-label="Explore the academy">
                <a href="#home-instructors" data-tab-target="home-instructors">Instructors</a>
                <a href="#home-vehicles" data-tab-target="home-vehicles">Vehicles</a>
                <a href="#home-booking" data-tab-target="home-booking">Booking</a>
            </div>
            <div class="ds-home-tab-panel" id="home-instructors" tabindex="0"><span class="ds-home-panel-number" aria-hidden="true">01</span><div><h3>A calm voice.<br>A clear next step.</h3><p>Meet the people who help you turn practice into confidence. Compare instructor profiles and learner ratings.</p><a class="ds-home-text-link" href="${instructorsUrl}">Meet your instructors <span aria-hidden="true">↗</span></a></div></div>
            <div class="ds-home-tab-panel" id="home-vehicles" tabindex="0"><span class="ds-home-panel-number" aria-hidden="true">02</span><div><h3>Get to know<br>your training vehicle.</h3><p>Explore our approach to inspected vehicles and dual-control training below. Ask the academy which vehicle suits your package.</p><a class="ds-home-text-link" href="#safety">Explore vehicle safety <span aria-hidden="true">↗</span></a></div></div>
            <div class="ds-home-tab-panel" id="home-booking" tabindex="0"><span class="ds-home-panel-number" aria-hidden="true">03</span><div><h3>Your time.<br>Your next lesson.</h3><p>Enrol in an active package, then request an available lesson time. Existing approval and availability checks still apply.</p><a class="ds-home-text-link" href="${bookingsUrl}">Plan a lesson <span aria-hidden="true">↗</span></a></div></div>
        </div>
    </section>

    <%-- 5. HomeController shows the sample school safety story. Public claims are identified in the footnotes. --%>
    <section id="safety" class="ds-cinematic-section ds-home-safety" data-parallax aria-labelledby="safety-title">
        <div class="ds-cinematic-background ds-scene-safety" aria-hidden="true"><div class="ds-safety-orbit"></div></div>
        <div class="ds-container"><div class="ds-home-copy" data-reveal><p class="ds-eyebrow">CONFIDENCE STARTS WITH CARE</p><h2 id="safety-title">Safety first.<br>Every turn.</h2><p>Our training approach puts inspected vehicles, dual controls and patient guidance at the centre of every lesson.<sup><a href="#home-notes" aria-label="Read safety information note">1</a></sup></p><a class="ds-button ds-button-outline" href="${contactUrl}">Talk about vehicle safety</a></div></div>
    </section>

    <%-- 6. HomeController displays a clearly marked placeholder; PublicPagesController handles contact requests. --%>
    <section class="ds-home-section" aria-labelledby="location-title">
        <div class="ds-container ds-home-location"><div data-reveal><p class="ds-eyebrow">YOUR LOCAL STARTING POINT</p><h2 id="location-title">Meet us<br>at the academy.</h2><p>Colombo, Sri Lanka<br>Street address to be confirmed.</p><p>Monday–Saturday · 8:00 AM–6:00 PM<br><small>Demo opening hours. Confirm before travelling.</small></p><p class="ds-location-note" id="directions-note">Directions will be available once the school address is confirmed.</p><div class="ds-home-actions"><a class="ds-button" href="${contactUrl}" aria-describedby="directions-note">Get directions</a><a class="ds-button ds-button-outline" href="${contactUrl}">Contact</a></div></div><div class="ds-location-art" role="img" aria-label="Original abstract street grid illustrating a future academy location, not a real map"><span>COLOMBO / SRI LANKA</span><span class="ds-location-marker" aria-hidden="true">D</span><span>YOUR JOURNEY STARTS HERE</span></div></div>
    </section>

    <%-- 7. HomeController links to BookingController without creating or submitting a booking. --%>
    <section class="ds-cinematic-section ds-home-cta" aria-labelledby="drive-title"><div class="ds-cinematic-background ds-scene-road ds-scene-road-close" aria-hidden="true"><div class="ds-road-lines"></div></div><div class="ds-container"><div class="ds-home-copy" data-reveal><p class="ds-eyebrow">THE FIRST STEP IS YOURS</p><h2 id="drive-title">Get behind<br>the wheel.</h2><p>Start with a plan. Build your skills one lesson at a time.</p><a class="ds-button" href="${bookingsUrl}">Book a lesson</a></div></div></section>

    <%-- 8. HomeController explains scheduling; saved availability remains controlled by the booking module. --%>
    <section class="ds-cinematic-section ds-home-flexible" data-parallax aria-labelledby="schedule-title"><div class="ds-cinematic-background ds-scene-flexible" aria-hidden="true"></div><div class="ds-container"><div class="ds-home-copy" data-reveal><p class="ds-eyebrow">MAKE ROOM FOR PROGRESS</p><h2 id="schedule-title">Flexible schedule.<br>Steady progress.</h2><p>Find your rhythm around the lesson times your instructor makes available.</p></div><ul class="ds-home-three-points"><li><span>01</span><h3>Explore available times</h3><p>Request a slot that works for you.</p></li><li><span>02</span><h3>Keep your plan in view</h3><p>Review lesson status in your account.</p></li><li><span>03</span><h3>Move forward together</h3><p>Follow guidance from your instructor.</p></li></ul></div></section>

    <%-- 9. HomeController links students to StudentProgressController; no sample progress is invented here. --%>
    <section class="ds-home-section" aria-labelledby="progress-title"><div class="ds-container ds-home-progress" data-reveal><div><p class="ds-eyebrow">SMALL STEPS. LASTING HABITS.</p><h2 id="progress-title">Designed<br>to get better.</h2></div><div><p>Every lesson is a chance to learn something new. Review completed lessons, see instructor feedback and keep your next goal in sight.</p><a class="ds-home-text-link" href="<c:url value='/student/progress' />">Explore your progress <span aria-hidden="true">↗</span></a><p class="ds-home-small">Student sign-in required.</p></div></div></section>

    <%-- 10. HomeController offers four plain links to existing public pages. --%>
    <section class="ds-home-section ds-home-exploring" aria-labelledby="explore-title"><div class="ds-container"><p class="ds-eyebrow">THERE IS MORE TO DISCOVER</p><h2 id="explore-title">Keep exploring.</h2><div class="ds-home-tiles"><a href="${packagesUrl}"><span>01 / YOUR PATH</span><h3>Packages</h3><span aria-hidden="true">↗</span></a><a href="${instructorsUrl}"><span>02 / YOUR GUIDES</span><h3>Instructors</h3><span aria-hidden="true">↗</span></a><a href="${reviewsUrl}"><span>03 / THEIR STORIES</span><h3>Reviews</h3><span aria-hidden="true">↗</span></a><a href="<c:url value='/faq' />"><span>04 / YOUR QUESTIONS</span><h3>FAQ</h3><span aria-hidden="true">↗</span></a></div></div></section>

    <%-- 11. HomeController supplies the mission. JS reads PublicReviewController's approved public HTML only. --%>
    <section class="ds-home-section ds-home-mission" aria-labelledby="mission-title"><div class="ds-container"><div class="ds-home-mission-copy" data-reveal><p class="ds-eyebrow">OUR REASON TO DRIVE</p><h2 id="mission-title">More than a licence.<br>A lifetime of<br>better decisions.</h2><p>We believe learning to drive should build awareness, patience and respect for everyone sharing the road.</p></div><div class="ds-home-reviews" role="region" data-home-reviews data-reviews-url="${reviewsUrl}" aria-labelledby="learner-stories-title"><h3 id="learner-stories-title">From our learners</h3><p data-review-fallback>Read the feedback learners have shared with the academy.</p><div data-review-slides aria-live="polite" aria-atomic="true"></div><div class="ds-review-controls" hidden><button type="button" data-review-previous aria-label="Previous approved review">←</button><span data-review-position></span><button type="button" data-review-next aria-label="Next approved review">→</button></div><a class="ds-home-text-link" href="${reviewsUrl}">Read approved reviews <span aria-hidden="true">↗</span></a></div></div></section>

    <%-- 12. HomeController explains project placeholders rather than presenting them as verified business claims. --%>
    <section class="ds-home-footnotes" id="home-notes" aria-label="Project and sample information"><div class="ds-container"><p>1. DriveSpark Academy is a university project. Safety descriptions illustrate the proposed school service; confirm vehicle equipment and inspection details with the academy.</p><p>Package prices and lesson counts come from the current project data. The monthly spotlight is an editorial selection, with no discount implied. Reviews may include labelled demo records.</p><p>Location and opening hours are placeholders. No real address, directions or operating hours have been verified. Original abstract scenery is used in place of photography.</p></div></section>
</main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
