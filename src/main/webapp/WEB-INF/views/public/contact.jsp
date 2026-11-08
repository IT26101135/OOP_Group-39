<%--
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and shared UI
FILE     : contact.jsp
WHAT IT DOES (simple words): Presents contact us with clear links and readable details.
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
<meta name="description" content="Talk to DriveSpark Academy about learning packages, registration and lesson planning.">
<title>Contact us | DriveSpark Academy</title>
<link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
<link rel="stylesheet" href="<c:url value='/css/tailwind.css' />">
</head><body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main id="main-content" class="ds-public ds-container ds-contact-public">
    <%-- PublicPagesController supplies the result messages and the existing contact form token. --%>
    <header class="ds-public-heading"><p class="ds-eyebrow">EVERY JOURNEY STARTS WITH A CONVERSATION</p><h1>Let us talk<br>about your next step.</h1><p>Questions about learning to drive? Tell us what you have in mind.</p></header>
    <c:if test="${messageSent}"><p role="status">Your message has been received.</p></c:if>
    <c:if test="${not empty formError}"><p role="alert"><c:out value="${formError}" /></p></c:if>
    <div class="ds-contact-public-grid">
        <%-- PublicPagesController opens these clearly marked placeholder contact details. --%>
        <aside><p class="ds-eyebrow">COME SAY HELLO</p><h2>Your local<br>starting point.</h2><dl class="ds-public-facts"><div><dt>Location</dt><dd>Colombo, Sri Lanka<br>Street address to be confirmed.</dd></div><div><dt>Opening hours</dt><dd>Monday–Saturday<br>8:00 AM–6:00 PM</dd></div></dl><p class="ds-public-note">Location and hours are project placeholders. Confirm details before travelling.</p></aside>
        <%-- PublicPagesController receives the original name, email and message fields; CSRF stays in place. --%>
        <form class="ds-public-form" method="post" action="<c:url value='/contact' />">
            <label for="contact-name">Name</label><input id="contact-name" name="name" required maxlength="100" autocomplete="name">
            <label for="contact-email">Email</label><input id="contact-email" name="email" type="email" required autocomplete="email">
            <label for="contact-message">Message</label><textarea id="contact-message" name="message" minlength="10" maxlength="2000" required rows="5" aria-describedby="contact-message-hint"></textarea><small id="contact-message-hint">Tell us a little about your goals. Use 10–2,000 characters.</small>
            <input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />">
            <button class="ds-button" type="submit">Send message</button>
        </form>
    </div>
</main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body></html>
