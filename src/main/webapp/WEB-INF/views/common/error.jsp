<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>DriveSpark Academy - Error</title>
</head>
<body>
<main>
    <h1><c:out value="${title}"/></h1>

    <p>
        Error code:
        <strong><c:out value="${statusCode}"/></strong>
    </p>

    <p><c:out value="${message}"/></p>

    <p>Please return to the previous page using your browser.</p>
</main>
</body>
</html>