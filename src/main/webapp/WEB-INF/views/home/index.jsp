<%-- Module owner: IT26101135 --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url value="/css/tailwind.css" var="appCssUrl" />
<c:url value="/" var="homeUrl" />
<c:url value="/packages" var="packagesUrl" />
<c:url value="/instructors" var="instructorsUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="theme-color" content="#0B1120">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description"
          content="DriveSpark Academy — a learning-focused driving school experience.">
    <title>DriveSpark Academy | Learn to Drive</title>
    <link rel="stylesheet" href="${appCssUrl}">
</head>
<body>
    <%@ include file="/WEB-INF/views/common/navbar.jspf" %>

    <main id="main-content">
        <section class="ds-hero">
            <div class="ds-container ds-hero-grid">
                <div class="ds-hero-content">
                    <p class="ds-eyebrow">YOUR JOURNEY STARTS HERE</p>
                    <h1>Learn to drive with confidence.</h1>
                    <p class="ds-hero-description">
                        Welcome to DriveSpark Academy. Develop your driving
                        knowledge, build good habits, and take the next step
                        towards becoming a responsible driver.
                    </p>
                    <div class="ds-hero-actions">
                        <a class="ds-button" href="<c:out value='${packagesUrl}' />">Explore packages</a>
                        <a class="ds-button ds-button-accent" href="#our-approach">How it works</a>
                    </div>
                </div>

                <div class="ds-hero-art">
                    <img src="<c:url value='/images/hero-night-road.svg' />" width="720" height="520" alt="A learner car travelling safely along a quiet night road" fetchpriority="high">
                    <div class="ds-float-chip ds-chip-top">Learn at your pace</div>
                    <div class="ds-float-chip ds-chip-bottom">Safety comes first</div>
                </div>
            </div>
        </section>

        <section class="ds-stats-strip" aria-label="DriveSpark demo highlights">
            <div class="ds-container ds-stats-grid"><div><strong>4</strong><span>Learning paths</span></div><div><strong>1:1</strong><span>Instructor support</span></div><div><strong>Safe</strong><span>Skills-first approach</span></div><p>Illustrative highlights for this project demo.</p></div>
        </section>

        <c:if test="${not empty featuredPackages}">
            <section class="ds-section">
                <div class="ds-container">
                    <h2>Featured learning packages</h2>
                    <div class="ds-feature-grid">
                        <c:forEach items="${featuredPackages}" var="learningPackage" varStatus="packageLoop">
                            <article class="ds-feature-card <c:if test='${packageLoop.index eq 1}'>ds-featured-package</c:if>">
                                <c:if test="${packageLoop.index eq 1}"><span class="ds-popular-ribbon">Most popular</span></c:if>
                                <h3><c:out value="${learningPackage.name}" /></h3>
                                <p><c:out value="${learningPackage.type}" /> ·
                                    <c:out value="${learningPackage.lessonCount}" /> lessons</p>
                                <p>LKR <c:out value="${learningPackage.price}" /></p>
                                <a href="<c:out value='${packagesUrl}' />">View packages</a>
                            </article>
                        </c:forEach>
                    </div>
                    <a href="<c:out value='${instructorsUrl}' />">Meet our instructors</a>
                </div>
            </section>
        </c:if>

        <section class="ds-section" id="our-approach">
            <div class="ds-container">
                <div class="ds-section-heading">
                    <p class="ds-eyebrow">THE DRIVE SPARK APPROACH</p>
                    <h2>A strong foundation for every driver</h2>
                    <p>
                        Good driving starts with knowledge, responsibility,
                        and a commitment to safety.
                    </p>
                </div>

                <div class="ds-feature-grid ds-four-grid">
                    <article class="ds-feature-card">
                        <span class="ds-feature-number">01</span>
                        <h3>Learn the essentials</h3>
                        <p>
                            Build your understanding of driving principles
                            and road safety.
                        </p>
                    </article>

                    <article class="ds-feature-card">
                        <span class="ds-feature-number">02</span>
                        <h3>Develop safe habits</h3>
                        <p>
                            Understand the importance of awareness,
                            patience, and responsible decisions.
                        </p>
                    </article>

                    <article class="ds-feature-card">
                        <span class="ds-feature-number">03</span>
                        <h3>Grow your confidence</h3>
                        <p>
                            Take a learning-focused approach to developing
                            your knowledge and skills.
                        </p>
                    </article>

                    <article class="ds-feature-card">
                        <span class="ds-feature-number">04</span>
                        <h3>Progress with purpose</h3>
                        <p>Build confidence step by step with a clear lesson plan and supportive guidance.</p>
                    </article>
                </div>
            </div>
        </section>

        <section class="ds-section ds-how">
            <div class="ds-container"><div class="ds-section-heading"><p class="ds-eyebrow">A SIMPLE START</p><h2>How your learning journey works</h2></div>
                <ol class="ds-steps"><li><span>01</span><h3>Choose a package</h3><p>Explore the learning path that fits your goals.</p></li><li><span>02</span><h3>Meet your instructor</h3><p>Find an instructor and request a lesson time.</p></li><li><span>03</span><h3>Practise safely</h3><p>Build practical skills through structured lessons.</p></li><li><span>04</span><h3>Track your progress</h3><p>Review notes and keep moving toward your goals.</p></li></ol>
            </div>
        </section>

        <section class="ds-section ds-reviews-teaser"><div class="ds-container ds-reviews-banner"><div><p class="ds-eyebrow">LEARNER STORIES</p><h2>Progress is better when you can see it.</h2><p>Read feedback shared by students and explore their learning experiences.</p></div><a class="ds-button ds-button-outline" href="<c:url value='/public/reviews' />">Read approved reviews</a></div></section>

        <section class="ds-section"><div class="ds-container ds-faq-teaser"><div><p class="ds-eyebrow">GOOD TO KNOW</p><h2>Questions before you start?</h2><p>Find out how enrollment and lesson requests work.</p></div><details><summary>When can I book a lesson?</summary><p>Once your student account is approved and you enroll in an active package, you can request an available lesson time.</p></details><a class="ds-button ds-button-outline" href="<c:url value='/faq' />">Visit the FAQ</a></div></section>

        <section class="ds-section ds-cta-section">
            <div class="ds-container ds-cta-content">
                <div>
                    <p class="ds-eyebrow">THE ROAD AHEAD</p>
                    <h2>Make safe driving part of your journey.</h2>
                </div>
                <a class="ds-button ds-button-light" href="${packagesUrl}">
                    View learning packages
                </a>
            </div>
        </section>
    </main>

    <%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
