<%--
============================================================
MEMBER   : IT26101132 - [Name]
MODULE   : Vehicles and Sample Data
FILE     : create.jsp
WHAT IT DOES (simple words): Keeps vehicles and sample data details clear and safe for the school.
REAL-LIFE EXAMPLE: Like a fleet logbook showing which teaching cars need attention.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for vehicles
============================================================
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url value="/css/tailwind.css" var="appCssUrl" />
<c:url value="/admin/vehicles/create" var="createVehicleUrl" />
<c:url value="/admin/vehicles" var="vehicleListUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="description" content="Add vehicle | DriveSpark Academy Manage teaching vehicle details, insurance and service dates.">
    <meta name="theme-color" content="#0B1120">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Add vehicle | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
</head>
<body>

<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main id="main-content" class="ds-container ds-section ds-dashboard-shell"><%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content">

    <div class="ds-vehicle-form-heading mb-8">
        <p class="ds-eyebrow">VEHICLE MANAGEMENT</p>
        <h1>Add a vehicle</h1>
        <p class="text-slate-400">
            Enter the vehicle details to create a new vehicle record.
        </p>
    </div>

    <%-- VehicleController supplies this form and the enum choices; validation errors keep typed values. --%>
    <section class="ds-card">

        <c:if test="${not empty formError}">
            <p class="ds-vehicle-error mb-5 rounded-xl border border-rose-300/30 bg-rose-400/10 p-4 text-rose-100" role="alert">
                <c:out value="${formError}" />
            </p>
        </c:if>

        <form class="ds-vehicle-form grid gap-5 sm:grid-cols-2"
              method="post"
              action="<c:out value='${createVehicleUrl}' />">

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
                       autocomplete="off"
                       placeholder="Example: CAB-1234"
                       value="<c:out value='${vehicleCreateForm.registrationNumber}' />">
                <small class="text-slate-400">Enter a valid vehicle registration number.</small>
            </div>

            <div class="ds-vehicle-field min-w-0 flex flex-col gap-2">
                <label for="vehicle-make">Make</label>
                <input class="w-full" id="vehicle-make"
                       name="make"
                       type="text"
                       required
                       maxlength="50"
                       value="<c:out value='${vehicleCreateForm.make}' />">
            </div>

            <div class="ds-vehicle-field min-w-0 flex flex-col gap-2">
                <label for="vehicle-model">Model</label>
                <input class="w-full" id="vehicle-model"
                       name="model"
                       type="text"
                       required
                       maxlength="50"
                       value="<c:out value='${vehicleCreateForm.model}' />">
            </div>

            <div class="ds-vehicle-field min-w-0 flex flex-col gap-2">
                <label for="vehicle-transmission">Transmission</label>
                <select class="w-full" id="vehicle-transmission"
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

            <div class="ds-vehicle-field min-w-0 flex flex-col gap-2">
                <label for="vehicle-category">Vehicle category</label>
                <select class="w-full" id="vehicle-category"
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

            <div class="ds-vehicle-field min-w-0 flex flex-col gap-2">
                <label for="vehicle-status">Initial operational status</label>
                <select class="w-full" id="vehicle-status"
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

            <div class="ds-vehicle-field min-w-0 flex flex-col gap-2"><label for="insuranceExpiry">Insurance expiry</label><input class="w-full" id="insuranceExpiry" type="date" name="insuranceExpiry" value="<c:out value='${vehicleCreateForm.insuranceExpiry}' />"></div>
            <div class="ds-vehicle-field min-w-0 flex flex-col gap-2"><label for="lastServiceDate">Last service date</label><input class="w-full" id="lastServiceDate" type="date" name="lastServiceDate" value="<c:out value='${vehicleCreateForm.lastServiceDate}' />"></div>
            <div class="ds-vehicle-field min-w-0 flex flex-col gap-2"><label for="nextServiceDate">Next service date</label><input class="w-full" id="nextServiceDate" type="date" name="nextServiceDate" value="<c:out value='${vehicleCreateForm.nextServiceDate}' />"></div>

            <div class="ds-vehicle-form-actions flex flex-wrap items-center gap-4 sm:col-span-2">
                <button class="ds-button" type="submit">
                    Create vehicle
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
