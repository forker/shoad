<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Test Page</title>
        <script type="text/javascript" src="http://code.jquery.com/jquery-1.6.1.min.js"></script>
        <script>
            $(function() {
                $("#button1").click(function() {
                    $.ajax({
                        type: 'POST',
                        url: "/LDAPManager/rpc",
                        data: "{ rpcAction : \"getUser\", userId : \"morgan@users.example.com\" }",
                        success: function(data) { alert(data); }
                    });
                });
            }); 
        </script>
    </head>
    <body>
        <h1>Hello World!</h1>
        <button type="button" id="button1" value="Button">Button</button>
    </body>
</html>
