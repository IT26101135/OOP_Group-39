<%--
============================================================
MEMBER   : IT26101132 - [Name]
MODULE   : Vehicles and Sample Data
FILE     : edit.jsp
WHAT IT DOES (simple words): Keeps vehicles and sample data details clear and safe for the school.
REAL-LIFE EXAMPLE: Like a fleet logbook showing which teaching cars need attention.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for vehicles
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url value="/css/tailwind.css" var="appCssUrl" />
<c:url value="/admin/vehicles/${vehicle.id}/edit" var="editVehicleUrl" />
<c:url value="/admin/vehicles" var="vehicleListUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="description" content="Edit vehicle | DriveSpark Academy Manage teaching vehicle details, insurance and service dates.">
    <meta name="theme-color" content="#0B1120">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Edit vehicle | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
</head>
<body class="ds-account-page">

<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main id="main-content" class="ds-container ds-section ds-dashboard-shell ds-vehicle-editor"><%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content">

    <div class="ds-vehicle-form-heading mb-8">
        <p class="ds-eyebrow">VEHICLE MANAGEMENT</p>
        <h1>Edit vehicle</h1>
        <p class="text-slate-400">
            Update the vehicle identity, type, status, insurance, and service dates.
        </p>
    </div>

    <%-- VehicleController supplies this form and the enum choices; validation errors keep typed values. --%>
    <section class="ds-card">

        <div class="ds-vehicle-readonly mb-8 border-b border-white/10 pb-6">
            <h2>Vehicle information</h2>

            <dl class="ds-vehicle-details my-4 grid gap-4 sm:grid-cols-2">

                <div>
                    <dt>Category</dt>
                    <dd><c:out value="${publicDisplay.label(vehicle.vehicleCategory)}" /></dd>
                </div>

                <div>
                    <dt>Transmission</dt>
                    <dd><c:out value="${publicDisplay.label(vehicle.transmissionType)}" /></dd>
                </div>

                <div>
                    <dt>Vehicle status</dt>
                    <dd><span class="status-badge" data-status="<c:out value='${vehicle.operationalStatus}' />"><c:out value="${publicDisplay.label(vehicle.operationalStatus)}" /></span></dd>
                </div>
            </dl>

            <p class="ds-vehicle-readonly-note">
                Changes are validated when this form is saved.
            </p>
        </div>

        <c:if test="${not empty formError}">
            <p class="ds-vehicle-error mb-5 rounded-xl border border-rose-300/30 bg-rose-400/10 p-4 text-rose-100" role="alert">
                <c:out value="${formError}" />
            </p>
        </c:if>

        <form class="ds-vehicle-form grid gap-5 sm:grid-cols-2"
              method="post"
              action="<c:out value='${editVehicleUrl}' />">

            <input type="hidden"
                   name="<c:out value='${csrfParameterName}' />"
                   value="<c:out value='${csrfToken}' />">

            <div class="ds-vehicle-field min-w-0 flex flex-col gap-2">
                <label for="vehicle-registration">Registration number</label>
                <input class="w-full" id="vehicle-registration"
                       name="registrationNumber"
                       type="text"
                       required
                       minlength="4"
                       maxlength="12"
                       value="<c:out value='${vehicleEditForm.registrationNumber}' />">
            </div>

            <div class="ds-vehicle-field min-w-0 flex flex-col gap-2">
                <label for="vehicle-make">Make</label>
                <input class="w-full" id="vehicle-make"
                       name="make"
                       type="text"
                       required
                       maxlength="50"
                       value="<c:out value='${vehicleEditForm.make}' />">
            </div>

            <div class="ds-vehicle-field min-w-0 flex flex-col gap-2">
                <label for="vehicle-model">Model</label>
                <input class="w-full" id="vehicle-model"
                       name="model"
                       type="text"
                       required
                       maxlength="50"
                       value="<c:out value='${vehicleEditForm.model}' />">
            </div>

            <label class="ds-vehicle-field">Transmission <select name="transmissionType"><c:forEach items="${transmissionTypes}" var="v"><option value="${v}" <c:if test="${vehicleEditForm.transmissionType eq v}">selected</c:if>><c:out value="${publicDisplay.label(v)}" /></option></c:forEach></select></label>
            <label class="ds-vehicle-field">Category <select name="vehicleCategory"><c:forEach items="${vehicleCategories}" var="v"><option value="${v}" <c:if test="${vehicleEditForm.vehicleCategory eq v}">selected</c:if>><c:out value="${publicDisplay.label(v)}" /></option></c:forEach></select></label>
            <label class="ds-vehicle-field">Status <select name="operationalStatus"><c:forEach items="${operationalStatuses}" var="v"><option value="${v}" <c:if test="${vehicleEditForm.operationalStatus eq v}">selected</c:if>><c:out value="${publicDisplay.label(v)}" /></option></c:forEach></select></label>
            <label class="ds-vehicle-field">Insurance expiry <input type="date" name="insuranceExpiry" value="<c:out value='${vehicleEditForm.insuranceExpiry}' />"></label>
            <label class="ds-vehicle-field">Last service <input type="date" name="lastServiceDate" value="<c:out value='${vehicleEditForm.lastServiceDate}' />"></label>
            <label class="ds-vehicle-field">Next service <input type="date" name="nextServiceDate" value="<c:out value='${vehicleEditForm.nextServiceDate}' />"></label>
            <div class="ds-vehicle-form-actions flex flex-wrap items-center gap-4 sm:col-span-2">
                <button class="ds-button" type="submit">
                    Save changes
                </button>

                <a href="<c:out value='${vehicleListUrl}' />">
                    Cancel
                </a>
            </div>

        </form>
    </section>
</div></main>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>

</body>
</html>