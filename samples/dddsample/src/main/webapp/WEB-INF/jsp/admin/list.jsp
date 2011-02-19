<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<html>
<head>
    <title>Cargo Administration</title>
</head>
<body>
<table>
    <caption>All cargos</caption>
    <thead>
    <tr>
        <td>Tracking ID</td>
        <td>Origin</td>
        <td>Destination</td>
        <td>Routed</td>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${cargoList}" var="cargo">
        <tr>
            <td>
                <c:url value="/admin/show.html" var="showUrl">
                    <c:param name="trackingId" value="${cargo.trackingId}"/>
                </c:url>
                <a href="${showUrl}">${cargo.trackingId}</a>
            </td>
            <td>${cargo.origin}</td>
            <td>${cargo.finalDestination}</td>
            <td>${cargo.routed ? "Yes" : "No"}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>