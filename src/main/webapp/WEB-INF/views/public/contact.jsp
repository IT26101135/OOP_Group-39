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
    <header class="ds-public-heading"><p class="ds-eyebrow">CONTACT US</p><h1>Ask us about<br>driving lessons.</h1><p>Tell us which lesson type you're interested in, or ask about your booking.</p></header>
    <c:if test="${messageSent}"><p role="status">Your message has been saved for our team to read. This website does not send an email confirmation. <a href="<c:url value='/packages' />">View lesson packages</a></p></c:if>
    <c:if test="${not empty formError}"><p role="alert"><c:out value="${formError}" /></p></c:if>
    <div class="ds-contact-public-grid">
        <%-- PublicPagesController opens these clearly marked placeholder contact details. --%>
        <aside><p class="ds-eyebrow">HOW TO REACH US</p><h2>Speak to our team.</h2><%@ include file="/WEB-INF/views/common/components/school-contact.jspf" %><p>Use the form to ask us about lessons or your booking.</p></aside>
        <%-- PublicPagesController receives the original name, email and message fields; CSRF stays in place. --%>
        <form class="ds-public-form" method="post" action="<c:url value='/contact' />">
            <label for="contact-name">Name</label><input id="contact-name" name="name" value="<c:out value='${contactName}' />" required maxlength="100" autocomplete="name">
            <label for="contact-email">Email</label><input id="contact-email" name="email" value="<c:out value='${contactEmail}' />" type="email" required autocomplete="email">
            <label for="contact-message">Message</label><textarea id="contact-message" name="message" minlength="10" maxlength="2000" required rows="5" aria-describedby="contact-message-hint"><c:out value="${contactMessage}" /></textarea><small id="contact-message-hint">Tell us how we can help. Use 10 to 2,000 characters.</small>
            <input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />">
            <button class="ds-button" type="submit">Send message</button>
        </form>
    </div>
</main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body></html>
