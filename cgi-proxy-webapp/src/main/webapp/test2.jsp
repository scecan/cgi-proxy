<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Test</title>
</head>
<body>
<%
    System.out.println("jsp headers");
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
        String name = headerNames.nextElement();
        Enumeration<String> headerValues = request.getHeaders(name);
        while (headerValues.hasMoreElements()) {
            String value = headerValues.nextElement();
            System.out.println(name + " - " + value);
        }
    }

    System.out.println("jsp parameters");
    Map<String, String[]> parametersMap = request.getParameterMap();
    for (Map.Entry<String, String[]> entry : parametersMap.entrySet()) {
        System.out.println(entry.getKey() + " - " + entry.getValue());
    }

    response.setHeader("Test-Header", "Test-Value");
%>
Hello World2!
<br/>
<a href="test.jsp">test.jsp</a>
</body>
</html>