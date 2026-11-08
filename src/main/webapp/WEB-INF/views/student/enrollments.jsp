<%--
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Learning packages and enrollment
FILE     : enrollments.jsp
WHAT IT DOES (simple words): Shows enrollments information and controls.
REAL-LIFE EXAMPLE: A course folder keeps the learning plan and enrollment details together.
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
    <meta name="description" content="Review your learning packages, enrollment validity and personal learning goals.">
    <title>My enrollments | DriveSpark Academy</title>
    <link rel="icon" href="<c:url value='/images/favicon.svg' />" type="image/svg+xml">
    <link rel="stylesheet" href="<c:url value='/css/tailwind.css' />">
</head>
<body>
<%@ include file="/WEB-INF/views/common/navbar.jspf" %>
<main id="main-content" class="ds-container ds-section ds-dashboard-shell">
    <%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %>
    <div class="ds-dashboard-content">
        <p class="ds-eyebrow">YOUR LEARNING PLANS</p>
        <h1>My enrollments</h1>
        <p class="mb-6 text-slate-400">Keep your goal in sight and check the details of each learning package.</p>
        <%-- EnrollmentController explains duplicate requests without creating another receipt. --%>
        <c:if test="${not empty error}"><p class="ds-form-error mb-6" role="alert"><c:out value="${error}" /></p></c:if>
        <c:choose>
            <c:when test="${empty enrollments}"><%-- This block shows a page section. EnrollmentController supplies the page data. --%>
<section class="ds-empty-state"><h2>Your first lesson starts here</h2><p>Choose a package to begin your learning journey.</p><a class="ds-button mt-4" href="<c:url value='/packages' />">Browse packages</a></section></c:when>
            <c:otherwise>
                <div class="grid gap-5">
                <c:forEach items="${enrollments}" var="enrollment">
                    <%-- This block shows one saved record. EnrollmentController supplies the page data. --%>
<article class="ds-feature-card">
                        <%-- OOP: POLYMORPHISM - LearningRecord supplies the heading for this enrollment. --%>
                        <h2><c:out value="${enrollment.displayName}" /></h2>
                        <p class="mt-2">Reference: <strong><c:out value="${enrollment.referenceCode}" /></strong></p>
                        <p>Package <c:out value="${enrollment.packageId}" /> · Enrolled <c:out value="${enrollment.enrolledAt.toLocalDate()}" /></p>
                        <div class="my-4 flex flex-wrap gap-2">
                            <span class="status-badge ds-payment-status" data-status="<c:out value='${enrollment.status}' />"><c:out value="${enrollment.statusLabel}" /></span>
                            <span class="status-badge ds-payment-status" data-status="${enrollment.status eq 'CANCELLED' or expiryLabels[enrollment.id] eq 'Expired' ? 'CANCELLED' : 'PENDING'}"><c:out value="${expiryLabels[enrollment.id]}" /></span>
                        </div>
                        <%-- This block shows the saved details. EnrollmentController supplies the page data. --%>
<dl class="mb-5 grid gap-3 sm:grid-cols-2">
                            <div><dt class="text-sm text-slate-400">Package amount</dt><dd class="text-lg">LKR <c:out value="${enrollment.amount}" /></dd></div>
                            <div><dt class="text-sm text-slate-400">Valid through</dt><dd><c:out value="${enrollment.validUntil}" default="Not recorded" /></dd></div>
                        </dl>
                        <%-- EnrollmentController supplies the active receipt and balance; unpaid requests never unlock booking. --%>
                        <c:url value="/enrollments/${enrollment.id}/payment" var="paymentUrl" />
                        <div class="mb-6">
                            <c:choose>
                                <c:when test="${enrollment.status eq 'AWAITING_PAYMENT'}">
                                    <button class="ds-button ds-button-outline" type="button" disabled>Book lesson</button>
                                    <p class="mt-3">Available after payment is confirmed</p>
                                    <a class="ds-home-text-link mt-3" href="<c:out value='${paymentUrl}' />">Payment instructions</a>
                                </c:when>
                                <c:when test="${enrollment.status eq 'PAID' and enrollment.id eq activeEnrollmentId and lessonsRemaining gt 0}">
                                    <a class="ds-button" href="<c:url value='/bookings' />">Book lesson</a>
                                </c:when>
                                <c:when test="${enrollment.status eq 'CANCELLED'}"><p>This request is cancelled. You may choose a package again.</p><c:if test="${not empty enrollment.cancellationReason}"><p class="mt-3">Reason: <c:out value="${enrollment.cancellationReason}" /></p></c:if><a class="ds-home-text-link" href="<c:url value='/packages' />">View packages</a></c:when>
                                <c:otherwise><p>No usable lessons remain on this enrollment.</p></c:otherwise>
                            </c:choose>
                        </div>
                        <%-- This block shows a form with its existing field names. EnrollmentController supplies the page data. --%>
<form method="post" action="<c:url value='/enrollments/update' />">
                            <input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />">
                            <input type="hidden" name="enrollmentId" value="<c:out value='${enrollment.id}' />">
                            <label for="goal-<c:out value='${enrollment.id}' />">My learning goal</label>
                            <textarea class="block w-full" id="goal-<c:out value='${enrollment.id}' />" name="learningGoal" maxlength="300" placeholder="For example: practise hill starts and confident parking."><c:out value="${enrollment.learningGoal}" /></textarea>
                            <button class="ds-button mt-3" type="submit">Save learning goal</button>
                        </form>
                        <c:choose>
                            <c:when test="${enrollment.status eq 'AWAITING_PAYMENT'}">
                                <%-- This block shows a form with its existing field names. EnrollmentController supplies the page data. --%>
<form class="mt-5 border-t border-white/10 pt-4" method="post" action="<c:url value='/enrollments/delete' />" data-confirm="Remove this unused unpaid enrollment? Active enrollments and booking history are protected.">
                                    <input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />">
                                    <input type="hidden" name="enrollmentId" value="<c:out value='${enrollment.id}' />">
                                    <button class="ds-button ds-button-danger" type="submit">Remove unused enrollment</button>
                                </form>
                            </c:when>
                            <c:otherwise><p class="mt-5 text-sm text-slate-400">This receipt is kept safely with your learning history.</p></c:otherwise>
                        </c:choose>
                    </article>
                </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</main>
<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body></html>
