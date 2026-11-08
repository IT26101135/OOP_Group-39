<%--
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and enrollment
FILE     : enrollments.jsp
WHAT IT DOES (simple words): Shows unpaid requests for the administrator to check.
REAL-LIFE EXAMPLE: The cashier checks the student's reference before confirming payment.
OOP USED : Reads Enrollment getters and the admin operations interface
CRUD     : READ and UPDATE enrollment payment requests
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en"><head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Review enrollment requests and manually confirm received payments.">
    <title>Awaiting payment | DriveSpark Academy</title>
    <link rel="icon" href="<c:url value='/images/favicon.svg' />" type="image/svg+xml">
    <link rel="stylesheet" href="<c:url value='/css/tailwind.css' />">
</head><body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-container ds-section ds-dashboard-shell">
    <%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %>
    <div class="ds-dashboard-content">
        <%-- AdminEnrollmentController supplies only requests still awaiting a decision. --%>
        <header><p class="ds-eyebrow">THE CASHIER DESK</p><h1>Awaiting payment</h1>
            <p class="mt-4">Check the receipt outside this system before confirming payment.</p></header>
        <c:choose>
            <c:when test="${empty payments}"><section class="ds-empty-state mt-8"><p>No payments waiting.</p></section></c:when>
            <c:otherwise><div class="grid gap-6 mt-8">
                <c:forEach items="${payments}" var="payment">
                    <%-- AdminEnrollmentController supplies names; the receipt keeps its original amount. --%>
                    <article class="ds-feature-card" data-payment-id="<c:out value='${payment.id}' />">
                        <h2><c:out value="${payment.referenceCode}" /></h2>
                        <p class="mt-3"><span class="status-badge ds-payment-status" data-status="AWAITING_PAYMENT">Awaiting payment</span></p>
                        <dl class="grid gap-4 my-6 sm:grid-cols-2">
                            <div><dt>Student</dt><dd><c:out value="${studentNames[payment.studentId]}" /> (<c:out value="${payment.studentId}" />)</dd></div>
                            <div><dt>Package</dt><dd><c:out value="${packageNames[payment.packageId]}" /></dd></div>
                            <div><dt>Amount</dt><dd>LKR <c:out value="${payment.amount}" /></dd></div>
                            <div><dt>Requested</dt><dd><c:out value="${payment.enrolledAt.toLocalDate()}" /></dd></div>
                        </dl>
                        <%-- The existing shared dialog confirms each decision; the service rechecks it under a lock. --%>
                        <form method="post" action="<c:url value='/admin/enrollments/mark-paid' />" data-confirm="Confirm that payment for ${payment.referenceCode} has been received?">
                            <input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />">
                            <input type="hidden" name="enrollmentId" value="<c:out value='${payment.id}' />">
                            <button class="ds-button" type="submit">Mark as Paid</button>
                        </form>
                        <%-- An optional reason stays in the form so cancellation also works without JavaScript. --%>
                        <form class="mt-6" method="post" action="<c:url value='/admin/enrollments/cancel-request' />" data-confirm="Cancel unpaid request ${payment.referenceCode}?">
                            <input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />">
                            <input type="hidden" name="enrollmentId" value="<c:out value='${payment.id}' />">
                            <label for="reason-${payment.id}">Cancellation reason (optional)</label>
                            <textarea class="block w-full" id="reason-${payment.id}" name="reason" maxlength="300" rows="2"></textarea>
                            <button class="ds-button ds-button-danger mt-3" type="submit">Cancel request</button>
                        </form>
                    </article>
                </c:forEach>
            </div></c:otherwise>
        </c:choose>
    </div>
</main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body></html>
