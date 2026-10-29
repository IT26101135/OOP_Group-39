<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url value="/css/app.css" var="appCssUrl" />
<c:url value="/" var="homeUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description"
          content="DriveSpark Academy — a learning-focused driving school experience.">
    <title>DriveSpark Academy | Learn to Drive</title>
    <link rel="stylesheet" href="${appCssUrl}">
</head>
<body>
    <%@ include file="/WEB-INF/views/common/navbar.jspf" %>

    <main>
        <section class="ds-hero">
            <div class="ds-container ds-hero-grid">
                <div class="ds-hero-content">
                    <p class="ds-eyebrow">YOUR JOURNEY STARTS HERE</p>
                    <h1>Build confidence. Learn to drive.</h1>
                    <p class="ds-hero-description">
                        Welcome to DriveSpark Academy. Develop your driving
                        knowledge, build good habits, and take the next step
                        towards becoming a responsible driver.
                    </p>
                    <a class="ds-button" href="#our-approach">
                        Discover our approach
                    </a>
                </div>

                <div class="ds-hero-panel" aria-label="Learning at DriveSpark">
                    <div class="ds-panel-icon" aria-hidden="true">D</div>
                    <p class="ds-panel-label">DRIVE WITH PURPOSE</p>
                    <h2>Every journey begins with learning.</h2>
                    <p>
                        Grow your knowledge, practise safe habits, and
                        approach the road with care.
                    </p>
                    <div class="ds-panel-line" aria-hidden="true"></div>
                    <p class="ds-panel-note">Learn · Practise · Progress</p>
                </div>
            </div>
        </section>

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

                <div class="ds-feature-grid">
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
                </div>
            </div>
        </section>

        <section class="ds-cta-section">
            <div class="ds-container ds-cta-content">
                <div>
                    <p class="ds-eyebrow">THE ROAD AHEAD</p>
                    <h2>Make safe driving part of your journey.</h2>
                </div>
                <a class="ds-button ds-button-light" href="${homeUrl}">
                    Back to home
                </a>
            </div>
        </section>
    </main>

    <%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
