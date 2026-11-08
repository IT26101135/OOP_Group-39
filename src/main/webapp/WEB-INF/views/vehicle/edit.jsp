<%-- Module owner: IT26101132 --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url value="/css/tailwind.css" var="appCssUrl" />
<c:url value="/admin/vehicles/${vehicle.id}/edit" var="editVehicleUrl" />
<c:url value="/admin/vehicles" var="vehicleListUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="description" content="Edit vehicle | DriveSpark Academy at DriveSpark Academy. Explore lessons, learning resources and driving school information.">
    <meta name="theme-color" content="#0B1120">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Edit vehicle | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
</head>
<body>

<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main id="main-content" class="ds-container ds-section ds-vehicle"><%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content">

    <div class="ds-vehicle-form-heading">
        <p class="ds-vehicle-eyebrow">VEHICLE MANAGEMENT</p>
        <h1>Edit vehicle</h1>
        <p class="ds-vehicle-description">
            Update the vehicle identity, type, status, insurance, and service dates.
        </p>
    </div>

    <section class="ds-vehicle-form-panel">

        <div class="ds-vehicle-readonly">
            <h2>Vehicle information</h2>

            <dl class="ds-vehicle-details">
                <div>
                    <dt>Vehicle ID</dt>
                    <dd><c:out value="${vehicle.id}" /></dd>
                </div>

                <div>
                    <dt>Category</dt>
                    <dd><c:out value="${vehicle.vehicleCategory}" /></dd>
                </div>

                <div>
                    <dt>Transmission</dt>
                    <dd><c:out value="${vehicle.transmissionType}" /></dd>
                </div>

                <div>
                    <dt>Operational status</dt>
                    <dd><span class="status-badge" data-status="<c:out value='${vehicle.operationalStatus}' />"><c:out value="${vehicle.operationalStatus}" /></span></dd>
                </div>
            </dl>

            <p class="ds-vehicle-readonly-note">
                Changes are validated when this form is saved.
            </p>
        </div>

        <c:if test="${not empty formError}">
            <p class="ds-vehicle-error" role="alert">
                <c:out value="${formError}" />
            </p>
        </c:if>

        <form class="ds-vehicle-form"
              method="post"
              action="<c:out value='${editVehicleUrl}' />">

            <input type="hidden"
                   name="<c:out value='${csrfParameterName}' />"
                   value="<c:out value='${csrfToken}' />">

            <div class="ds-vehicle-field">
                <label for="vehicle-registration">Registration number</label>
                <input id="vehicle-registration"
                       name="registrationNumber"
                       type="text"
                       required
                       minlength="4"
                       maxlength="12"
                       value="<c:out value='${vehicleEditForm.registrationNumber}' />">
            </div>

            <div class="ds-vehicle-field">
                <label for="vehicle-make">Make</label>
                <input id="vehicle-make"
                       name="make"
                       type="text"
                       required
                       maxlength="50"
                       value="<c:out value='${vehicleEditForm.make}' />">
            </div>

            <div class="ds-vehicle-field">
                <label for="vehicle-model">Model</label>
                <input id="vehicle-model"
                       name="model"
                       type="text"
                       required
                       maxlength="50"
                       value="<c:out value='${vehicleEditForm.model}' />">
            </div>

            <label>Transmission <select name="transmissionType"><c:forEach items="${transmissionTypes}" var="v"><option value="${v}" <c:if test="${vehicleEditForm.transmissionType eq v}">selected</c:if>><c:out value="${v}" /></option></c:forEach></select></label>
            <label>Category <select name="vehicleCategory"><c:forEach items="${vehicleCategories}" var="v"><option value="${v}" <c:if test="${vehicleEditForm.vehicleCategory eq v}">selected</c:if>><c:out value="${v}" /></option></c:forEach></select></label>
            <label>Status <select name="operationalStatus"><c:forEach items="${operationalStatuses}" var="v"><option value="${v}" <c:if test="${vehicleEditForm.operationalStatus eq v}">selected</c:if>><c:out value="${v}" /></option></c:forEach></select></label>
            <label>Insurance expiry <input type="date" name="insuranceExpiry" value="<c:out value='${vehicleEditForm.insuranceExpiry}' />"></label>
            <label>Last service <input type="date" name="lastServiceDate" value="<c:out value='${vehicleEditForm.lastServiceDate}' />"></label>
            <label>Next service <input type="date" name="nextServiceDate" value="<c:out value='${vehicleEditForm.nextServiceDate}' />"></label>
            <div class="ds-vehicle-form-actions">
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