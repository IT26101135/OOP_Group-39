<%-- Module owner: IT26101132 --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url value="/css/tailwind.css" var="appCssUrl" />
<c:url value="/admin/vehicles/create" var="createVehicleUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="description" content="Manage vehicles | DriveSpark Academy at DriveSpark Academy. Explore lessons, learning resources and driving school information.">
    <meta name="theme-color" content="#0B1120">
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/favicon.svg' />">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Manage vehicles | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
</head>
<body>

<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main id="main-content" class="ds-container ds-section ds-vehicle"><%@ include file="/WEB-INF/views/common/dashboard-sidebar.jspf" %><div class="ds-dashboard-content">

    <c:if test="${vehicleDeleted}"><p role="status">Vehicle deleted.</p></c:if>

    <div class="ds-vehicle-heading">
        <div>
            <p class="ds-vehicle-eyebrow">ADMINISTRATION</p>
            <h1>Manage vehicles</h1>
            <p class="ds-vehicle-description">
                View and manage the vehicle records in the system.
            </p>
        </div>

        <a class="ds-button"
           href="<c:out value='${createVehicleUrl}' />">
            Add vehicle
        </a>
    </div>

    <c:if test="${vehicleCreated}">
        <p class="ds-vehicle-message" role="status">
            Vehicle created successfully.
        </p>
    </c:if>

    <c:if test="${vehicleUpdated}">
        <p class="ds-vehicle-message" role="status">
            Vehicle updated successfully.
        </p>
    </c:if>

    <section class="ds-vehicle-panel" aria-labelledby="vehicle-list-title">

        <div class="ds-vehicle-panel-heading">
            <h2 id="vehicle-list-title">Vehicle records</h2>
            <p>Vehicle registration and operational information</p>
        </div>

        <c:choose>
            <c:when test="${empty vehicles}">
                <div class="ds-vehicle-empty">
                    <h3>No vehicles to display</h3>
                    <p>
                        There are currently no vehicle records available
                        in this list.
                    </p>
                    <a href="<c:out value='${createVehicleUrl}' />">
                        Add the first vehicle
                    </a>
                </div>
            </c:when>

            <c:otherwise>
                <div class="ds-vehicle-table-wrapper">
                    <div class="safe-scroll rounded-2xl border border-white/10 bg-surface"><table class="ds-vehicle-table">
                        <thead>
                        <tr>
                            <th scope="col">Registration number</th>
                            <th scope="col">Make</th>
                            <th scope="col">Model</th>
                            <th scope="col">Category</th>
                            <th scope="col">Transmission</th>
                            <th scope="col">Insurance/service</th>
                            <th scope="col">Operational status</th>
                            <th scope="col">Action</th>
                        </tr>
                        </thead>

                        <tbody>
                        <c:forEach items="${vehicles}" var="vehicle">
                            <tr>
                                <td data-label="Registration number">
                                    <strong>
                                        <c:out value="${vehicle.registrationNumber}" />
                                    </strong>
                                </td>

                                <td data-label="Make">
                                    <c:out value="${vehicle.make}" />
                                </td>

                                <td data-label="Model">
                                    <c:out value="${vehicle.model}" />
                                </td>

                                <td data-label="Category">
                                    <c:out value="${vehicle.vehicleCategory}" />
                                </td>

                                <td data-label="Transmission">
                                    <c:out value="${vehicle.transmissionType}" />
                                </td>

                                <td data-label="Insurance/service">
                                    <div class="flex flex-col gap-1 items-start">
                                        <c:if test="${vehicle.insuranceWarning}">
                                            <span class="ds-status-badge" data-status="WARNING">Insurance due: <c:out value="${vehicle.insuranceExpiry}" default="Check expiry" /></span>
                                        </c:if>
                                        <c:if test="${vehicle.serviceWarning}">
                                            <span class="ds-status-badge" data-status="WARNING">Service due: <c:out value="${vehicle.nextServiceDate}" default="Check date" /></span>
                                        </c:if>
                                        <c:if test="${not vehicle.insuranceWarning and not vehicle.serviceWarning}">
                                            <span class="ds-status-badge" data-status="COMPLETED">Up to date</span>
                                        </c:if>
                                    </div>
                                </td>
                                <td data-label="Operational status">
                                    <span class="ds-vehicle-status">
                                        <span class="status-badge" data-status="<c:out value='${vehicle.operationalStatus}' />"><c:out value="${vehicle.operationalStatus}" /></span>
                                    </span>
                                </td>

                                <td data-label="Action">
                                    <c:url value="/admin/vehicles/${vehicle.id}/edit"
                                           var="editVehicleUrl" />
                                    <a class="ds-vehicle-edit-link"
                                       href="<c:out value='${editVehicleUrl}' />">
                                        Edit
                                    </a>
                                    <form method="post"
                                          action="<c:url value='/admin/vehicles/${vehicle.id}/delete' />">
                                        <input type="hidden"
                                               name="<c:out value='${csrfParameterName}' />"
                                               value="<c:out value='${csrfToken}' />">
                                        <button type="submit">Delete</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table></div>
                </div>
            </c:otherwise>
        </c:choose>

    </section>
</div></main>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>

</body>
</html>
