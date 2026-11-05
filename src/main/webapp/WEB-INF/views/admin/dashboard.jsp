<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url value="/css/app.css" var="appCssUrl" />
<c:url value="/css/admin-dashboard.css" var="dashboardCssUrl" />
<c:url value="/admin/vehicles" var="dashboardVehiclesUrl" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Administrator dashboard | DriveSpark Academy</title>
    <link rel="stylesheet" href="<c:out value='${appCssUrl}' />">
    <link rel="stylesheet" href="<c:out value='${dashboardCssUrl}' />">
</head>
<body>

<%@ include file="/WEB-INF/views/common/navbar.jspf" %>

<main class="ds-container ds-section ds-admin-dashboard">
    <header class="ds-admin-dashboard-heading">
        <p class="ds-admin-dashboard-eyebrow">ADMINISTRATION</p>
        <h1>Administrator dashboard</h1>
        <p>
            Welcome, <c:out value="${navDisplayName}" />.
            Manage your driving school from here.
        </p>
    </header>

    <section aria-labelledby="dashboard-tools-title">
        <h2 id="dashboard-tools-title">Management tools</h2>

        <div class="ds-admin-dashboard-grid">
            <article class="ds-admin-dashboard-card">
                <span class="ds-admin-dashboard-label">VEHICLES</span>
                <h3>Manage vehicles</h3>
                <p>
                    View your vehicles, add new records, and update
                    registration numbers, makes and models.
                </p>
                <a class="ds-button"
                   href="<c:out value='${dashboardVehiclesUrl}' />">
                    Open vehicle management
                </a>
            </article>
        </div>
    </section>
</main>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>

</body>
</html>