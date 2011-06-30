<%-- 
    Document   : index
    Created on : Jun 25, 2011, 8:12:28 PM
    Author     : forker
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Manage Entries</title>
        <script src="js/jquery-1.6.1.min.js" type="text/javascript" ></script>
        <script src="js/jquery-ui-1.8.13.custom.min.js" type="text/javascript" ></script>
        <script src="js/jquery.dataTables.min.js" type="text/javascript" ></script>
        <link rel="stylesheet" href="css/overcast/jquery-ui-1.8.13.custom.css" type="text/css" />
        <link rel="stylesheet" href="http://www.datatables.net/release-datatables/media/css/demo_table.css" type="text/css" />
        <script type="text/javascript">
            $(function() {
                /*
                 * 
                 */
                
                function areArraysEqual(array1, array2) {
                    var temp = new Array();
                    if ( (!array1[0]) || (!array2[0]) ) { // If either is not an array
                        return false;
                    }
                    if (array1.length != array2.length) {
                        return false;
                    }
                    // Put all the elements from array1 into a "tagged" array
                    for (var i=0; i<array1.length; i++) {
                        key = (typeof array1[i]) + "~" + array1[i];
                        // Use "typeof" so a number 1 isn't equal to a string "1".
                        if (temp[key]) { temp[key]++; } else { temp[key] = 1; }
                        // temp[key] = # of occurrences of the value (so an element could appear multiple times)
                    }
                    // Go through array2 - if same tag missing in "tagged" array, not equal
                    for (var i=0; i<array2.length; i++) {
                        key = (typeof array2[i]) + "~" + array2[i];
                        if (temp[key]) {
                            if (temp[key] == 0) { return false; } else { temp[key]--; }
                            // Subtract to keep track of # of appearances in array2
                        } else { // Key didn't appear in array1, arrays are not equal.
                            return false;
                        }
                    }
                    // If we get to this point, then every generated key in array1 showed up the exact same
                    // number of times in array2, so the arrays are equal.
                    return true;
                }


                function rpcQuery(query, callback) {
                    $.ajax({
                        type: 'POST',
                        url: "/LDAPManager/rpc",
                        dataType: 'json',
                        data: query,
                        success: callback
                    });
                }
                
                var groupNavyRoot = $("#group-navy-ui");
                var userEditRoot = $("#user-edit-ui");
                var groupNavySelector= groupNavyRoot.children("div.group-navy-ui-selector").children("ol.group-navy-ui-selectable");
                var groupNavyToolbar = groupNavyRoot.children("div.group-navy-ui-toolbar");
                var groupNavyDisplayTable = groupNavyRoot.children("div.group-navy-ui-display-area").children("table.group-navy-ui-display-table");
                var displayDataTable = null;
                var attrDefs = null;
                var currentUser = null;
                var currentUserId = null;
                
                rpcQuery("{ rpcAction: \"attr.defs.get\" }", function(data) {
                    attrDefs = data;
                });
                                                      
                rpcQuery("{ rpcAction: \"group.list\", domain: \"example.com\" }", function(data) {
                        
                    $(data.groups).each(function(index, group) {
                        groupNavySelector.append("<li id=\"" + group.groupId + "\" class=\"group-navy-ui-select-li\">" + group.groupId.split("@")[0] + "</li>");
                    }); 
                        
                });

                groupNavySelector.selectable({
                    selecting: function(event, ui) {

                        var displayGroups = [];
                        var selectedGroups = groupNavySelector.children("li.ui-selected").toArray();
                        
                        for(var group in selectedGroups) {
                            displayGroups.push( selectedGroups[group].id );
                        }
                            
                        displayGroups.push( ui.selecting.id );
                        
                        listUsers(displayGroups);
                    }
                });
                
                var listUsers = function(groups) {
                    rpcQuery("{ rpcAction: \"user.list\", domain: \"example.com\", groups: " + JSON.stringify(groups) + " }", function(data) {
                                
                        if(displayDataTable != null) { 
                            displayDataTable.fnDestroy();
                        }
                                
                        groupNavyDisplayTable.children("tbody").empty();
                        
                        $(data.users).each(function() {
                            groupNavyDisplayTable.append("<tr id=\"" + this.userId + "\"><td><span>" + this.userId.split("@")[0]+ "</span></td><td>" + this.fullName + "</td></tr>");
                        });
                                                              
                        displayDataTable = groupNavyDisplayTable.dataTable();
                                
                        groupNavyDisplayTable.children("tbody").children("tr").click(function() {
                            displayUser($(this).attr("id"));
                        });
                    });
                }
                      
                var displayUser = function(userId) {
                    rpcQuery("{ rpcAction: \"user.get\", userId: \"" + userId + "\" }", function(user) {
                        currentUser = user;
                        currentUserId = userId;
                        for(var key in user) {
                            userEditRoot.children("table").append("<tr for=\"" + key + "\"><td style=\"white-space: nowrap;\">" + attrDefs[key].displayName + ":</td><td></td></tr>")
                            if(attrDefs[ key ].multiValue == true) {
                                for(var i in user[key]) {
                                    userEditRoot.children("table").children("tbody").children("tr:last").children("td:last").append("<input size=\"30\" type=\"text\" name=\"" + key + "\" value=\""+user[key][i]+"\" ><br/>");
                                }
                            } else if(attrDefs[ key ].type == "boolean") {
                                userEditRoot.children("table").children("tbody").children("tr:last").children("td:last").append("<input type=\"checkbox\" name=\"" + key + "\" value=\""+user[key]+"\" >");
                            } else {
                                userEditRoot.children("table").children("tbody").children("tr:last").children("td:last").append("<input type=\"text\" name=\"" + key + "\" value=\""+user[key]+"\" >");
                            }                            
                        }
                        
                        userEditRoot.dialog({
                            close : function() { 
                                $(this).dialog("destroy");
                                $(this).children("table").empty();
                            },
                            title : "User Attributes",
                            width : "auto",
                            modal : true
                        });
                    });
                }
                
                $("#user-edit-submit").click(function() {
                    var request = {};
                    request["mod"] = {};
                    
                    userEditRoot.children("table").children("tbody").children("tr").each(function() {
                        if(attrDefs[ $(this).attr("for") ].multiValue == true) {
                            var newValArray = [];
                            $(this).children("td:last").children("input").each(function() {
                                newValArray.push($(this).val());
                            });
                            
                            if(!areArraysEqual(newValArray, currentUser[$(this).attr("for")])) {
                                request["mod"][ $(this).attr("for") ] = newValArray;
                            }
                        } else if( attrDefs[ $(this).attr("for") ].type == "boolean" ) {
                            var newVal = $(this).children("td:last").children("input").attr("checked") == "checked" ? "true" : "false";
                             
                            if(newVal != currentUser[$(this).attr("for")]) {
                                request["mod"][ $(this).attr("for") ] = newVal;
                            }
                                
                        } else {
                            var newVal = $(this).children("td:last").children("input").val();
                            
                            if(newVal != currentUser[$(this).attr("for")]) {
                                request["mod"][ $(this).attr("for") ] = newVal;
                            }
                        }
                    }
                    
                    
                    
                );
                    alert(request["userId"]);
                    rpcQuery("{ rpcAction: \"user.mod\", userId : \"" + currentUserId + "\", mod: " + JSON.stringify(request["mod"]) + " }", function(data) {
                        alert(data.success);
                    });
                });
                
                
                /*
                 * 
                 */
            });
        </script>
        <style type="text/css">
            .group-navy-ui-selector {
                float: left;
                background-color: #ffffff;

            }

            .group-navy-ui-select-li {

            }

            .group-navy-ui-display-area {
                background-color: #777777;
                min-width: 40em;
                min-height: 18em;
                float: left;
                padding: 1em 1em;
            }


            .group-navy-ui-display-table {
                margin: 0 auto;
                clear: both;
                width: 100%;
                background-color: lightskyblue;
            }

            .group-navy-ui-display-table thead th {
                padding: 3px 18px 3px 10px;
                border-bottom: 1px solid black;
                font-weight: bold;
                cursor: pointer;
                * cursor: hand;
            }

            .group-navy-ui-display-table tfoot th {
                padding: 3px 18px 3px 10px;
                border-top: 1px solid black;
                font-weight: bold;
            }

            .group-navy-ui-display-table tr.heading2 td {
                border-bottom: 1px solid #aaa;
            }

            .group-navy-ui-display-table td {
                padding: 3px 10px;
                white-space: nowrap;
                cursor: pointer;
            }

            .group-navy-ui-display-table td.center {
                text-align: center;
            }

            .group-navy-ui-toolbar {
                height: 3em;
                padding: 0em 0em 1em 1em;
            }

            .group-navy-ui-selectable .ui-selecting { background: #AAAAAA; }
            .group-navy-ui-selectable .ui-selected { background: #777777; color: white; }
            .group-navy-ui-selectable { list-style-type: none; margin: 0; padding: 0; width: auto; cursor: pointer; }
            .group-navy-ui-selectable li { margin: 0px 0px; padding: 0.15em 1.5em 0.15em 1em; font-size: 1.0em; background-color: #dddddd;  }

            body {
                font-family: "Sans Serif";
                background-color: #eeeeee;
            }

        </style>
    </head>
    <body>
        <div id="group-navy-ui">
            <div class="group-navy-ui-toolbar"><h1>User Account Directory</h1></div>
            <div class="group-navy-ui-selector">
                <ol class="group-navy-ui-selectable">
                </ol>
            </div>
            <div class="group-navy-ui-display-area">

                <table class="group-navy-ui-display-table">
                    <thead> 
                        <tr>
                            <th>User ID</th>
                            <th>Full Name</th>
                        </tr> 
                    </thead>
                    <tbody>

                    </tbody>
                </table>
            </div>
        </div>
        <div style="display: none;" id="user-edit-ui">
            <table></table>
            <button style="float: right;" type="button" id="user-edit-submit">Update</button>
        </div>
    </body>
</html>
