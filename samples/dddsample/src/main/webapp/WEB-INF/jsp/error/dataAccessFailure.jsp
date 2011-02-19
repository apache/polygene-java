<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<html>
<head>
    <title>Data Access Failure</title>
</head>
<body>
<div id="container">
    <h1>
        A DataAccessExcepton occured.
    </h1>

    <h2><span id="error" class="error">${exception.message}</span></h2>
    <a href="<c:url value="/start.html"/>">Start page</a>
</div>
</body>
</html>