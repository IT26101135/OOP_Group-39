<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url value="/css/app.css" var="appCssUrl" />
<c:url value="/css/vehicle.css" var="vehicleCssUrl" />
<c:url value="/admin/vehicles/${vehicle.id}/edit" var="editVehicleUrl" />
<c:url value="/admin/vehicles" var="vehicleListUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Edit vehicle | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
    <link rel="stylesheet" href="<c:out value='${vehicleCssUrl}' />">
</head>
<body>

<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main class="ds-container ds-section ds-vehicle">

    <div class="ds-vehicle-form-heading">
        <p class="ds-vehicle-eyebrow">VEHICLE MANAGEMENT</p>
        <h1>Edit vehicle</h1>
        <p class="ds-vehicle-description">
            Update the registration number, make, and model.
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
                    <dd><c:out value="${vehicle.operationalStatus}" /></dd>
                </div>
            </dl>

            <p class="ds-vehicle-readonly-note">
                Category, transmission, and operational status cannot
                be changed from this form.
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
</main>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>

</body>
</html>