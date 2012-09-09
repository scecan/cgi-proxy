<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Test</title>
</head>
<body>
    Method: <%=request.getMethod()%>
    <br/>param1: <%=request.getParameter("param1")%>
    <br/>param2: <%=request.getParameter("param2")%>

    <br/>
    <a href="test2.jsp">test2.jsp</a>
    <br/>
    <form action="test.jsp" method="POST">
        <label for="param1"></label><input id="param1" name="param1" type="text">
        <br/>
        <label for="param2"></label><input id="param2" name="param2" type="text">
        <br/>
        <input type="submit" name="submit" value="Submit"/>
    </form>
</body>
</html>