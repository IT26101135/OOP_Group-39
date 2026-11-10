<%--
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : progress.jsp
WHAT IT DOES (simple words): Shows progress information and controls.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Review completed lessons, driving hours and your instructor's feedback.">
    <title>My learning progress | DriveSpark Academy</title>
    <link rel="icon" href="<c:url value='/images/favicon.svg' />" type="image/svg+xml">
    <link rel="stylesheet" href="<c:url value='/css/tailwind.css' />">
</head>
<body class="ds-account-page">
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-container ds-section ds-dashboard-shell">
    <%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %>
    <div class="ds-dashboard-content">
        <p class="ds-eyebrow">YOUR LESSON RECORD</p>
        <h1>My learning progress</h1>
        <%-- This block shows a page section. StudentProfileController, StudentPasswordController or StudentProgressController supplies the page data. --%>
<section class="mb-8 grid gap-4 sm:grid-cols-2" aria-label="Completed learning">
            <%-- This block shows one saved record. StudentProfileController, StudentPasswordController or StudentProgressController supplies the page data. --%>
<article class="ds-card"><h2 class="text-sm">Lessons completed</h2><p class="mt-3 text-3xl text-indigo-200" data-counter="<c:out value='${progress.lessonsCompleted}' />"><c:out value="${progress.lessonsCompleted}" /></p></article>
            <%-- This block shows one saved record. StudentProfileController, StudentPasswordController or StudentProgressController supplies the page data. --%>
<article class="ds-card"><h2 class="text-sm">Hours driven</h2><p class="mt-3 text-3xl text-amber-200"><c:out value="${progress.hoursDriven}" /></p></article>
        </section>
        <%-- This block shows a page section. StudentProfileController, StudentPasswordController or StudentProgressController supplies the page data. --%>
<section aria-labelledby="feedback-heading">
            <h2 id="feedback-heading" class="mb-5">Instructor feedback</h2>
            <c:choose>
                <c:when test="${empty progress.lessonRecords}"><div class="ds-empty-state"><h3>No lesson feedback yet</h3><p>Your instructor can add notes and skill ratings after a completed lesson.</p><a class="ds-button mt-4" href="<c:url value='/bookings' />">View my bookings</a></div></c:when>
                <c:otherwise>
                    <div class="grid gap-5">
                    <c:forEach items="${progress.lessonRecords}" var="record">
                        <%-- This block shows one saved record. StudentProfileController, StudentPasswordController or StudentProgressController supplies the page data. --%>
<article class="ds-feature-card">
                            <h3>Lesson <c:out value="${accountDisplay.lesson(record.bookingId)}" /></h3>
                            <p class="whitespace-pre-line"><c:out value="${record.notes}" /></p>
                            <%-- This block shows the saved details. StudentProfileController, StudentPasswordController or StudentProgressController supplies the page data. --%>
<dl class="mt-5 grid gap-4 sm:grid-cols-2">
                            <c:forEach items="${record.skillRatings}" var="skill">
                                <div><dt class="text-sm text-slate-300"><c:out value="${skill.key}" /></dt><dd class="text-indigo-200"><c:out value="${skill.value}" /> / 5<progress class="block h-2 w-full accent-indigo-400" max="5" value="<c:out value='${skill.value}' />" aria-label="<c:out value='${skill.key}' /> rating"></progress></dd></div>
                            </c:forEach>
                            </dl>
                            <p class="mt-5"><c:choose><c:when test="${record.readyForTest}"><span class="status-badge" data-status="COMPLETED">Instructor marked ready for test</span></c:when><c:otherwise><span class="status-badge">Keep practising with your instructor</span></c:otherwise></c:choose></p>
                        </article>
                    </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </div>
</main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body></html>
