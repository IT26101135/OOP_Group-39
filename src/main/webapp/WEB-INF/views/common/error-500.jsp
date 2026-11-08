<%--
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Shared foundation and student accounts
FILE     : error-500.jsp
WHAT IT DOES (simple words): Shows an accessible recovery page when a request cannot be completed.
REAL-LIFE EXAMPLE: A reception desk starts the school day and helps people find the right page.
OOP USED : Shared setup, inheritance or exception presentation as explained below
CRUD     : Supports account operations; does not add a new storage operation
============================================================
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><c:url value="/css/tailwind.css" var="css"/><!doctype html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="theme-color" content="#050505"><meta name="description" content="A temporary error occurred at DriveSpark Academy."><title>500 - Something went wrong</title><link rel="icon" href="<c:url value='/images/favicon.svg' />"><link rel="stylesheet" href="<c:out value='${css}' />"></head><body><a class="skip-link" href="#main-content">Skip to error message</a>
<%-- Spring error dispatch supplies the error status; this page keeps account details private. --%>
<%-- The error handler supplies this message; a focusable main region lets the skip link work without scripts. --%>
<main class="ds-error-shell" id="main-content" tabindex="-1"><div class="ds-error-art"><img src="<c:url value='/images/error-road.svg' />" width="360" height="220" alt="A quiet road illustration"></div><p class="ds-eyebrow">ERROR 500</p><h1>We hit a bump in the road</h1><p>Please try again in a moment. Your account and lesson data are not shown on this page.</p><a class="ds-button" href="<c:url value='/' />">Back to home</a></main></body></html>
