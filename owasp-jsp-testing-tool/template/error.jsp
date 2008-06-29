<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ page isErrorPage="true"%>
<%@page import="org.owasp.jsptester.report.ErrorHandler"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Error Page</title>
</head>
<body>
<%
    // need to reset status code since isErrorPage automatically makes this
    // a 500 error which some browsers interpret
    response.setStatus( HttpServletResponse.SC_OK );

    String msg = "Unexpected Arrival at Error Page";
    if ( exception != null )
    {
        msg = ErrorHandler.buildStackTrace( exception.getCause() );
    }
%>
<input id="error" type="text" value="<%= msg %>" />
<h1>Error!</h1>
<%=msg%>
</body>
</html>