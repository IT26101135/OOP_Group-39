<%--
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and enrollment
FILE     : payment-instructions.jsp
WHAT IT DOES (simple words): Shows how to pay for one owned enrollment request.
REAL-LIFE EXAMPLE: A reception slip gives the price and reference to write on a payment.
OOP USED : Displays the Enrollment model through public getters
CRUD     : READ enrollment payment instructions
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="View your enrollment reference, amount and manual payment instructions.">
    <title>Payment instructions | DriveSpark Academy</title>
    <link rel="icon" href="<c:url value='/images/favicon.svg' />" type="image/svg+xml">
    <link rel="stylesheet" href="<c:url value='/css/tailwind.css' />">
</head>
<body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-container ds-section ds-dashboard-shell">
    <%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %>
    <div class="ds-dashboard-content">
        <%-- EnrollmentController checks ownership and supplies this receipt and package name. --%>
        <header><p class="ds-eyebrow">YOUR NEXT STEP</p><h1>Payment instructions</h1>
            <p class="mt-4">Keep this reference with your payment receipt.</p></header>
        <section class="ds-feature-card mt-8" aria-labelledby="payment-summary">
            <h2 id="payment-summary"><c:out value="${packageName}" /></h2>
            <p class="my-4"><span class="status-badge ds-payment-status" data-status="<c:out value='${enrollment.status}' />"><c:out value="${enrollment.statusLabel}" /></span></p>
            <dl class="grid gap-5 sm:grid-cols-2">
                <div><dt>Amount</dt><dd class="text-2xl">LKR <c:out value="${enrollment.amount}" /></dd></div>
                <div><dt>Reference code</dt><dd class="text-2xl"><c:out value="${enrollment.referenceCode}" /></dd></div>
            </dl>
            <%-- EnrollmentController reads the contact address from application.properties. --%>
            <c:choose>
                <c:when test="${enrollment.status eq 'AWAITING_PAYMENT'}">
                    <p class="mt-6 ds-payment-instructions">Pay by bank transfer or at our office, then email your payment receipt and reference code to <strong><c:out value="${paymentAdminEmail}" /></strong>.</p>
                    <p class="mt-4">Contact the school for bank details. Lessons become available after an administrator confirms payment. Your package validity starts on that day.</p>
                </c:when>
                <c:when test="${enrollment.status eq 'PAID'}"><p class="mt-6">Payment is confirmed. Check your enrollments for your available lessons.</p></c:when>
                <c:otherwise><p class="mt-6">This request is cancelled. Do not pay using this reference. You may choose a package again.</p></c:otherwise>
            </c:choose>
            <p class="mt-4">This system does not send email or upload receipts. Please send the receipt using your own email service.</p>
            <a class="ds-button ds-button-outline mt-6" href="<c:url value='/enrollments' />">My enrollments</a>
        </section>
    </div>
</main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
