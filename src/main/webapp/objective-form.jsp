<%-- 
    Document   : objective-form
    Created on : Jul 4, 2011, 8:42:26 PM
    Author     : forker
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Demo Page</title>
        <script src="js/jquery-1.6.1.min.js" type="text/javascript" ></script>
        <script src="js/utils.js" type="text/javascript" ></script>
        <script src="js/udfront.js" type="text/javascript" ></script>
        <script type="text/javascript">
            $(function() {
                

                
                var lenin = new Map();
                lenin.put("firstName", "Roman");
                lenin.put("lastName", "Sergey");
                lenin.put("email", ["test", "test2", "test3"]);
                lenin.put("pseudos", ["test", "test2", "test3"]);
                lenin.put("active", true)
                
                var keyDef = {
                    firstName : "First Name",
                    lastName : "Last Name",
                    email : "Email",
                    active : "Active",
                    pseudos : "Pseudos"
                };
                               
                $("#target").mapenform(
                    {source : lenin,
                    getLabel : function(key) { return keyDef[key] },
                    callback : function(newMap) { 
                        for(var key in newMap.getData()) {
                            
                        }
                    }
                });
                
            });
        </script>
    </head>
    <body>
        <div id="target" />
    </body>
</html>
