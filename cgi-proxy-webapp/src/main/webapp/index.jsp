<!DOCTYPE html>
<html>
<head>
    <title>My Proxy Server</title>
    <style>


        .box {
            width: 40em;
            margin: 50px auto;
            padding: 20px;
            text-align: center;
            border: 1px solid #909090;
        }
        .box h1 {
            font-weight: bold;
            font-style: normal;
            font-size: 1.2em;
            border-bottom: 1px solid #cccccc
        }
        .box input.txt {
            width: 39em;
        }


    </style>
</head>
<body>
<div class="box">
    <h1>My Proxy Server</h1>
    <form action="${pageContext.request.contextPath}/guice/proxify" method="POST">
        <input name="url" type="text" class="txt" placeholder="type url here..."/>
        <input type="submit" class="btn" value="Proxify"/>
    </form>
</div>
</body>
</html>
