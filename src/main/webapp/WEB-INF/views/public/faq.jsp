<%--
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and shared UI
FILE     : faq.jsp
WHAT IT DOES (simple words): Presents frequently asked questions with clear links and readable details.
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
<meta name="description" content="Find clear answers about DriveSpark Academy accounts, packages, lesson requests and manual payments.">
<title>Frequently asked questions | DriveSpark Academy</title>
<link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
<link rel="stylesheet" href="<c:url value='/css/tailwind.css' />">
</head><body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main id="main-content" class="ds-public ds-container ds-faq-public">
    <%-- PublicPagesController supplies this introduction and the static answers below. --%>
    <header class="ds-public-heading"><p class="ds-eyebrow">BEFORE YOU BOOK</p><h1>Common questions.</h1><p>Find answers about packages, payments and lesson requests.</p></header>
    <%-- Native details keeps the accordion usable with a keyboard and without JavaScript. --%>
    <section class="ds-faq-questions" aria-label="Common questions">
        <details><summary>How do I start?</summary><div><p>Create an account and choose a package. You can request lessons after we confirm your payment.</p><a href="<c:url value='/register' />">Create your student account</a></div></details>
        <details><summary>How are lessons booked?</summary><div><p>With a paid package and lessons remaining, you can request an available one-hour slot. We'll confirm your instructor and vehicle.</p><a href="<c:url value='/packages' />">Explore learning packages</a></div></details>
        <details><summary>Can I pay online?</summary><div><p>Online payment is not available. Open your package payment instructions for the amount and reference. Contact us to arrange payment.</p></div></details>
        <details><summary>Where can I see my progress?</summary><div><p>After signing in as a student, open your progress page to review completed lessons and instructor feedback.</p><a href="<c:url value='/student/progress' />">View student progress</a></div></details>
        <details><summary>Where is the academy?</summary><div><p>Send us a message before visiting so we can help you plan your visit.</p><a href="<c:url value='/contact' />">Contact the academy</a></div></details>
    </section>
    <%-- PublicPagesController keeps the existing contact page available for other questions. --%>
    <section class="ds-public-closing"><div><p class="ds-eyebrow">LET US HELP</p><h2>Still have a question?</h2></div><a class="ds-button ds-button-outline" href="<c:url value='/contact' />">Get in touch</a></section>
</main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body></html>
