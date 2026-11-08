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
        <c:if test="${enrolled}"><p role="status">Enrollment complete. Simulated payment was recorded as paid.</p></c:if>
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
                        <p>Package <c:out value="${enrollment.packageId}" /> · Enrolled <c:out value="${enrollment.enrolledAt.toLocalDate()}" /></p>
                        <div class="my-4 flex flex-wrap gap-2">
                            <span class="status-badge" data-status="<c:out value='${enrollment.paymentStatus}' />"><c:out value="${enrollment.paymentStatus}" /></span>
                            <span class="status-badge" data-status="${expiryLabels[enrollment.id] eq 'Expired' ? 'CANCELLED' : 'PENDING'}"><c:out value="${expiryLabels[enrollment.id]}" /></span>
                        </div>
                        <%-- This block shows the saved details. EnrollmentController supplies the page data. --%>
<dl class="mb-5 grid gap-3 sm:grid-cols-2">
                            <div><dt class="text-sm text-slate-400">Simulated amount</dt><dd class="text-lg text-indigo-200">LKR <c:out value="${enrollment.amount}" /></dd></div>
                            <div><dt class="text-sm text-slate-400">Valid through</dt><dd><c:out value="${enrollment.validUntil}" default="Not recorded" /></dd></div>
                        </dl>
                        <%-- This block shows a form with its existing field names. EnrollmentController supplies the page data. --%>
<form method="post" action="<c:url value='/enrollments/update' />">
                            <input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />">
                            <input type="hidden" name="enrollmentId" value="<c:out value='${enrollment.id}' />">
                            <label for="goal-<c:out value='${enrollment.id}' />">My learning goal</label>
                            <textarea class="block w-full" id="goal-<c:out value='${enrollment.id}' />" name="learningGoal" maxlength="300" placeholder="For example: practise hill starts and confident parking."><c:out value="${enrollment.learningGoal}" /></textarea>
                            <button class="ds-button mt-3" type="submit">Save learning goal</button>
                        </form>
                        <c:choose>
                            <c:when test="${enrollment.paymentStatus eq 'UNPAID'}">
                                <%-- This block shows a form with its existing field names. EnrollmentController supplies the page data. --%>
<form class="mt-5 border-t border-white/10 pt-4" method="post" action="<c:url value='/enrollments/delete' />" data-confirm="Remove this unused unpaid enrollment? Active enrollments and booking history are protected.">
                                    <input type="hidden" name="<c:out value='${csrfParameterName}' />" value="<c:out value='${csrfToken}' />">
                                    <input type="hidden" name="enrollmentId" value="<c:out value='${enrollment.id}' />">
                                    <button class="ds-button ds-button-danger" type="submit">Remove unused enrollment</button>
                                </form>
                            </c:when>
                            <c:otherwise><p class="mt-5 text-sm text-slate-400">Your paid receipt is kept safely with your learning history.</p></c:otherwise>
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
