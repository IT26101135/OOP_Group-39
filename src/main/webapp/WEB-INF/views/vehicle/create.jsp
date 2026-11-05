<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url value="/css/app.css" var="appCssUrl" />
<c:url value="/css/vehicle.css" var="vehicleCssUrl" />
<c:url value="/admin/vehicles/create" var="createVehicleUrl" />
<c:url value="/admin/vehicles" var="vehicleListUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Add vehicle | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
    <link rel="stylesheet" href="<c:out value='${vehicleCssUrl}' />">
</head>
<body>

<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main class="ds-container ds-section ds-vehicle">

    <div class="ds-vehicle-form-heading">
        <p class="ds-vehicle-eyebrow">VEHICLE MANAGEMENT</p>
        <h1>Add a vehicle</h1>
        <p class="ds-vehicle-description">
            Enter the vehicle details to create a new vehicle record.
        </p>
    </div>

    <section class="ds-vehicle-form-panel">

        <c:if test="${not empty formError}">
            <p class="ds-vehicle-error" role="alert">
                <c:out value="${formError}" />
            </p>
        </c:if>

        <form class="ds-vehicle-form"
              method="post"
              action="<c:out value='${createVehicleUrl}' />">

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
                       autocomplete="off"
                       placeholder="Example: CAB-1234"
                       value="<c:out value='${vehicleCreateForm.registrationNumber}' />">
                <small>Enter a valid vehicle registration number.</small>
            </div>

            <div class="ds-vehicle-field">
                <label for="vehicle-make">Make</label>
                <input id="vehicle-make"
                       name="make"
                       type="text"
                       required
                       maxlength="50"
                       value="<c:out value='${vehicleCreateForm.make}' />">
            </div>

            <div class="ds-vehicle-field">
                <label for="vehicle-model">Model</label>
                <input id="vehicle-model"
                       name="model"
                       type="text"
                       required
                       maxlength="50"
                       value="<c:out value='${vehicleCreateForm.model}' />">
            </div>

            <div class="ds-vehicle-field">
                <label for="vehicle-transmission">Transmission</label>
                <select id="vehicle-transmission"
                        name="transmissionType"
                        required>
                    <option value="">Select transmission</option>
                    <c:forEach items="${transmissionTypes}" var="transmission">
                        <option value="<c:out value='${transmission}' />"
                            <c:if test="${vehicleCreateForm.transmissionType eq transmission}">
                                selected
                            </c:if>>
                            <c:out value="${transmission}" />
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="ds-vehicle-field">
                <label for="vehicle-category">Vehicle category</label>
                <select id="vehicle-category"
                        name="vehicleCategory"
                        required>
                    <option value="">Select category</option>
                    <c:forEach items="${vehicleCategories}" var="category">
                        <option value="<c:out value='${category}' />"
                            <c:if test="${vehicleCreateForm.vehicleCategory eq category}">
                                selected
                            </c:if>>
                            <c:out value="${category}" />
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="ds-vehicle-field">
                <label for="vehicle-status">Initial operational status</label>
                <select id="vehicle-status"
                        name="operationalStatus"
                        required>
                    <option value="">Select operational status</option>
                    <c:forEach items="${operationalStatuses}" var="status">
                        <option value="<c:out value='${status}' />"
                            <c:if test="${vehicleCreateForm.operationalStatus eq status}">
                                selected
                            </c:if>>
                            <c:out value="${status}" />
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="ds-vehicle-form-actions">
                <button class="ds-button" type="submit">
                    Create vehicle
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