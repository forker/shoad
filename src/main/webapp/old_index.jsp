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
        <script src="js/utils.js" type="text/javascript" ></script>
        <script src="js/udfront.js" type="text/javascript" ></script>
        <link rel="stylesheet" href="css/overcast/jquery-ui-1.8.13.custom.css" type="text/css" />
        <link rel="stylesheet" href="css/user_list_table.css" type="text/css" />
        <script type="text/javascript">
            $(function() {
                /*
                 * Initializing in an isolated scope
                 */
                (function() {
                
                    function rpcQuery(query, callback) {
                        $.ajax({
                            type: 'POST',
                            url: "/LDAPManager/rpc",
                            dataType: 'json',
                            data: query,
                            success: callback
                        });
                    }
                    
                    var rpcService = new RPCService("/LDAPManager/rpc");
                    
                
                    var groupNavyRoot = $("#group-navy-ui");
                    var userEditRoot = $("#user-edit-ui");
                    var userMembershipEditRoot = userEditRoot.children("div").children("div");
                    var groupNavySelector= groupNavyRoot.children("div.group-navy-ui-selector").children("ol.group-navy-ui-selectable");
                    var groupNavyToolbar = groupNavyRoot.children("div.group-navy-ui-toolbar");
                    var groupNavyDisplayTable = groupNavyRoot.children("div.group-navy-ui-display-area").children("table.group-navy-ui-display-table");
                    var displayDataTable = null;
                    var attrDefs = new Map();
                    var showFullUserId = false;
                    var allGroups = [];
                    
                    var showUserId = function(fullUserId) {
                        return showFullUserId ? fullUserId.toString() : fullUserId.split("@")[0]
                    }
                    
                
                    rpcService.call({
                        action : "attr.defs.get",
                        callback : function(data) {                       
                            attrDefs.setData(data);
                        }
                    });
                    
                    rpcService.call({
                        action : "group.list",
                        data : { domain : "example.com" },
                        callback : function(data) {                       
                            $(data.groups).each(function(index, group) {
                                groupNavySelector.append("<li id=\"" + group.groupId + "\" class=\"group-navy-ui-select-li\">" + showUserId(group.groupId) + "</li>");
                                allGroups.push(group.groupId);
                            }); 
                        }
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
                        
                        rpcService.call({
                            action : "user.list",
                            data : { domain : "example.com", "groups" : groups },
                            callback : function(data) {
                                
                                if(displayDataTable != null) { 
                                    displayDataTable.fnDestroy();
                                }
                                
                                groupNavyDisplayTable.children("tbody").empty();
                        
                                $(data.users).each(function() {
                                    groupNavyDisplayTable.append("<tr id=\"" + this.userId + "\"><td><span>" + showUserId(this.userId) + "</span></td><td>" + this.fullName + "</td></tr>");
                                });
                                                              
                                displayDataTable = groupNavyDisplayTable.dataTable();
                                
                                groupNavyDisplayTable.children("tbody").children("tr").click(function() {
                                    displayUser($(this).attr("id"));
                                });
                            }
                        });
                    
                    }
                    
                    var addAttribute = function(key, isNew) {
                        userEditRoot.children("table").append("<tr for=\"" + key + "\"><td style=\"white-space: nowrap;\">" + attrDefs.get(key).displayName + ":</td><td></td></tr>")
                        var lastTd = userEditRoot.children("table").children("tbody").children("tr:last").children("td:last");
                        if(attrDefs.get(key).multiValue == true) {
                            if(isNew) {
                                lastTd.append(newAttributeValueInput(""));
                            } else {
                                for(var i in UdUser.current.getAttr(key)) {
                                    lastTd.append(newAttributeValueInput( UdUser.current.getAttr(key)[i] ));
                                }
                            }
                            newAddAttributeValueLink().appendTo(lastTd);
                        } else if(attrDefs.get(key).type == "boolean") {
                            newAttributeValueCheckbox().prop("checked", bool(UdUser.current.getAttr(key)) ).appendTo(lastTd);
                            lastTd.append("<a class=\"attribute-value-del\" href=\"javascript:;\" title=\"delete\">[-]</a>");
                        } else {
                            if(isNew) {
                                lastTd.append(newAttributeValueInput(""));
                            } else {
                                lastTd.append(newAttributeValueInput(UdUser.current.getAttr(key)));
                            }
                        }
                    }
                    
                    var newAttributeValueInput = function(value) {
                        return $("<input size=\"30\" type=\"text\" value=\"" + value + "\" /><a class=\"attribute-value-del\" href=\"javascript:;\" title=\"delete\">[-]</a><br/>");
                    }
                    
                    var newAttributeValueCheckbox = function() {
                        return $("<input type=\"checkbox\" />");
                    }
                    
                    var newAddAttributeValueLink = function() {
                        return $("<a class=\"add-attribute-value\" href=\"javascript:;\" >Add more..</a>").click(function() {
                            newAttributeValueInput("").insertBefore($(this));
                        });
                    }
                    
                    
                      
                    var displayUser = function(userId) {
                    
                        rpcService.call({
                            action : "user.get",
                            data : { "userId" : userId },
                            callback : function(user) {
                                
                                UdUser.current = new UdUser(userId, new Map(user));
                                
                                for(var key in user) {
                                    addAttribute(key);
                                }
                                
                                for(var  key in attrDefs.getAsJsObj()) {
                                    if(user[key] == null || typeof(user[key]) == "undefined") {
                                        $("<option/>").val(key).text(attrDefs.get(key).displayName).appendTo(userEditRoot.children("select"));
                                    }
                                }
                            
                            
                                $("a.attribute-value-del").click(function() {
                                    $(this).prev("input").remove();
                                    $(this).next("br").remove();
                                    if($(this).siblings("input").length == 0) {
                                        $(this).parent("td").parent("tr").remove();
                                    } else {
                                        $(this).remove();
                                    }
                                });
                            
                                
                        
                                userEditRoot.dialog({
                                    close : function() { 
                                        $(this).dialog("destroy");
                                        $(this).children("table").empty();
                                        userMembershipEditRoot.children("table").empty();
                                        userMembershipEditRoot.children("select").children("option:gt(0)").remove();
                                        userEditRoot.children("select").children("option:gt(0)").remove();
                                        userMembershipEditRoot.hide();
                                        $("#user-edit-ui div a").show();
                                    },
                                    title : "User Edit (" + showUserId( userId ) + ")",
                                    width : "auto",
                                    modal : true
                                });
                            }
                        });
                    }
                
                    $("#user-edit-submit").click(function() {
                        var request = new Map();
                        request.put("mod", new Map());
                        request.put("add", new Map());
                        request.put("remove", new Map());
                        
                        var beforeKeys = UdUser.current.getAttrs().getKeys();
                        
                        var afterKeys = [];
                        userEditRoot.children("table").children("tbody").children("tr").each(function() {
                                                     
                            
                            var apiKey = $(this).attr("for");
                            afterKeys.push(apiKey);
                            
                            if(attrDefs.get(apiKey).multiValue == true) {
                                var newValArray = [];
                                $(this).children("td:last").children("input").each(function() {
                                    newValArray.push($(this).val());
                                });
                            
                                if(typeof(UdUser.current.getAttr(apiKey)) == "undefined") { UdUser.current.getAttr(apiKey) = []; }
                                if(!areArraysEqual(newValArray, UdUser.current.getAttr(apiKey))) {
                                    var arrDiff = new ArrayDiff(newValArray, UdUser.current.getAttr(apiKey));
                                    request.get("add").put( apiKey, arrDiff.firstOnly);
                                    request.get("remove").put( apiKey, arrDiff.secondOnly);
                                }
                                
                            } else if(attrDefs.get(apiKey).type == "boolean" ) {
                                var newVal = $(this).children("td:last").children("input").attr("checked") == "checked" ? "true" : "false";
                             
                                if(newVal != UdUser.current.getAttr(apiKey)) {
                                    request.get("mod").put( apiKey, newVal);
                                }
                                
                            } else {
                                var newVal = $(this).children("td:last").children("input").val();
                            
                                if(newVal != UdUser.current.getAttr(apiKey)) {
                                    request.get("mod").put( apiKey, newVal);
                                }
                            }
                        });
                        
                        if(!areArraysEqual(beforeKeys, afterKeys)) {
                            var arrDiff = new ArrayDiff(beforeKeys, afterKeys);
                            request.put("removeAttr", arrDiff.firstOnly);
                        }
                        
                        
                    
                        rpcService.call({
                            action : "user.mod",
                            data : { userId : UdUser.current.getUserId(),
                                mod : request.get("mod").getAsJsObj(),
                                add : request.get("add").getAsJsObj(),
                                remove : request.get("remove").getAsJsObj(),
                                removeAttr : request.get("removeAttr") },
                            callback : function(data) {
                                alert(data.success);
                            }
                        });
                    });
                    
                    
                    
                    $("#user-edit-ui div div select").change(function() {
                        addGroup($(this).val());
                        $(this).children("option:selected").remove();
                    });
                    
                    userEditRoot.children("select").change(function() {
                        addAttribute($(this).val(), true);
                        $(this).children("option:selected").remove();
                    });
                    
                    $("#user-edit-ui div a").click(function() {
                        $(this).hide();
                        userMembershipEditRoot.show();
                    
                        rpcService.call({
                            action : "user.membership.get",
                            data : { userId : UdUser.current.getUserId() },
                            callback : function(data) {
                                UdUser.current.setGroups([]);
                                $(data.groups).each(function() {
                                    addGroup(this.groupId);
                                    UdUser.current.getGroups().push(this.groupId);
                                });
                                
                                if(!areArraysEqual(allGroups, UdUser.current.getGroups())) {
                                    var arrDiff = new ArrayDiff(allGroups, UdUser.current.getGroups());
                                    $(arrDiff.firstOnly).each(function() {
                                        addGroupToAddList(this);
                                    });
                                }
                                
                            }
                        });
                    });
                    
                    var addGroupToAddList = function(groupId) {
                        $("<option/>").val(groupId).text( showUserId( groupId ) ).appendTo(userMembershipEditRoot.children("select"));
                    }
                    
                    var addGroup = function(groupId) {
                        $("<tr for=\"" + groupId + "\"><td>" + showUserId( groupId ) + "</td><td><a href=\"javascript:;\" class=\"group-membership-del\">[-]</a></td></tr>")
                        .appendTo(userMembershipEditRoot.children("table")).children("td").children("a").click(function() {
                            addGroupToAddList( $(this).parent("td").parent("tr").attr("for") );
                            $(this).parent("td").parent("tr").remove();
                        });
                    }
                    
                    
                    $("#user-membership-edit-submit").click(function() {
                        var newGroupSet = [];
                        userMembershipEditRoot.children("table").children("tbody").children("tr").each(function() {
                            newGroupSet.push( $(this).attr("for") );
                        });
                        
                        if(!areArraysEqual(newGroupSet, UdUser.current.getGroups())) {
                            var arrDiff = new ArrayDiff(newGroupSet, UdUser.current.getGroups());
                            
                            rpcService.call({
                                action : "user.membership.mod",
                                data : { userId : UdUser.current.getUserId(), add : arrDiff.firstOnly, remove : arrDiff.secondOnly },
                                callback : function(data) {
                                    alert(data.success);
                                }
                            });                            
                        }
                    }); 
                })();
                                
                /*
                 * 
                 */
            });
        </script>
        <style type="text/css">

            a.add-attribute-value {
                font-size: 0.8em;
            }

            .group-navy-ui-selector {
                float: left;
                background-color: #dddddd;

            }

            .group-navy-ui-selector div {
                padding: 0.15em 1.5em 0.15em 1em;
            }

            #group-navy-ui {

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
                background-color: #BBBBBB;
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
                padding: 0px 0px 5px 0px;

            }

            .group-navy-ui-toolbar button {
                height: 25px;
            }



            .group-navy-ui-selectable .ui-selecting { background: #AAAAAA; }
            .group-navy-ui-selectable .ui-selected { background: #777777; color: white; }
            .group-navy-ui-selectable { list-style-type: none; margin: 0px 0px 0px 0px; padding: 0; width: auto; cursor: pointer; }
            .group-navy-ui-selectable li { margin: 0px 0px; padding: 0.15em 1.5em 0.15em 1em; font-size: 1.0em; background-color: #dddddd;  }

            .group-membership-del {
                text-decoration: none;
            }

            .attribute-value-del {
                text-decoration: none;
            }

            a.attribute-value-del:hover, a.group-membership-del:hover {
                text-decoration: none;
                color: red;
            }

            body {
                font-family: "Sans Serif";
                background-color: #eeeeee;
            }

        </style>
    </head>
    <body>
        <div id="group-navy-ui">

            <div class="group-navy-ui-selector">
                <div><button type="button">New User</button><button type="button">New Group</button></div>
                <hr/>
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
            <div>
                <a href="javascript:;">Group membership >></a>
                <div style="display: none;">
                    <table></table>
                    <select><option value="-">Add group...</option></select><br/>
                    <button type="button" id="user-membership-edit-submit">Update Membership</button>
                    <hr/>
                </div>
            </div>
            <table></table>
            <select><option value="-">Add attribute...</option></select><br/>
            <button style="float: right;" type="button" id="user-edit-submit">Update</button>
        </div>
    </body>
</html>
