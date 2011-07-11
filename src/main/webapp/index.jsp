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
        <script src="js/adfront.js" type="text/javascript" ></script>
        <link rel="stylesheet" href="css/overcast/jquery-ui-1.8.13.custom.css" type="text/css" />
        <link rel="stylesheet" href="css/user_list_table.css" type="text/css" />
        <script type="text/javascript">
            
            
            $(function() {
                /*
                 * Initializing in an isolated scope
                 */
                (function() {

                    var rpcService = new RPCService("rpc");

                    var adminDomains = null;
                    var urlHash = window.location.hash;
                    var currentDomain = urlHash.length > 0 ? urlHash.substr(1) : null;

                    var groupNavyRoot = $("#group-navy-ui");

                    var groupNavySelector= groupNavyRoot.children("div.group-navy-ui-selector").children("ol.group-navy-ui-selectable");
                    var groupNavyToolbar = groupNavyRoot.children("div.group-navy-ui-toolbar");
                    var groupNavyDisplayTable = groupNavyRoot.children("div.group-navy-ui-display-area").children("table.group-navy-ui-display-table");
                    var displayDataTable = null;
                    var allGroups = [];
                    
                    var showEntryId = function(fullUserId) {
                        return false ? fullUserId.toString() : fullUserId.split("@")[0]
                    }
                    
                    displayDataTable = groupNavyDisplayTable.dataTable({
                        "fnRowCallback": function( nRow, aData, iDisplayIndex ) {
                            $(nRow).attr("id", aData[0]);
                            $("td:eq(0)", nRow).text(showEntryId(aData[0]));
                            return nRow;
                        }
                    });

                    var addGroup = function(groupId) {
                        groupNavySelector.append("<li id=\"" + groupId + "\" class=\"group-navy-ui-select-li\">" + showEntryId(groupId) + "</li>");
                        allGroups.push(groupId);
                    }
                    
                    rpcService.call({
                        action : "domain.list",
                        data : {},
                        callback : function(data) {
                            if(data.success) {
                                if(currentDomain == null) {
                                    if(data.domains.length > 0) {
                                        window.location.href = window.location.href + "#" + data.domains[0];
                                    }
                                }
                                adminDomains = data.domains;
                                var $domainSelect = $("select.domain-list");
                                $(data.domains).each(function(key, value) {
                                    //alert(this);
                                    $("<option/>").val(value).text(value).appendTo($domainSelect);
                                });
                                
                                $domainSelect.val(currentDomain).change(function() {
                                    window.location.href = "#" + $(this).val();
                                    location.reload();
                                });
                            }
                        }
                    });

                    rpcService.call({
                        action : "group.list",
                        data : { domain : currentDomain },
                        callback : function(data) {                       
                            groupNavySelector.append("<li id=\"all\" class=\"group-navy-ui-select-li\">All Users</li>");
                            $(data.groups).each(function(index, group) {
                                addGroup(group.groupId);
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
                        
                            if(ui.selecting.id == "all") {
                                listUsers([]);
                            } else {
                                listUsers(displayGroups);
                            }
                        
                        }
                    });
                
                    var listUsers = function(groups) {
                        
                        rpcService.call({
                            action : "user.list",
                            data : { domain : currentDomain, "groups" : groups },
                            callback : function(data) {
                                
                                displayDataTable.fnClearTable();
                        
                                $(data.users).each(function() {
                                    displayDataTable.fnAddData([this.userId, this.fullName]);
                                    
                                });
                                
                                groupNavyDisplayTable.children("tbody").children("tr").click(function() {
                                    displayUser( $(this).attr("id") );
                                });
                            }
                        });
                    
                    }
                    
                    $("button.remove-group").click(function() {
                        rpcService.call({
                            action : "group.remove",
                            data : { groupId : groupNavySelector.children("li.ui-selected").first().attr("id") },
                            callback : function(data) {                       
                                if(data.success) {
                                    //alert("Successfully removed!");
                                    $().customAlert({
                                        alertTitle : 'Successfully removed!',
                                        alertOk	 : 'OK',
                                        draggable : false
                                    });
                                }
                            }
                        });
                    });

                    $.fn.loadGroups(allGroups);
                    $.fn.setDomain(currentDomain);
                    var displayUser = function(userId) {
                    
                        rpcService.call({
                            action : "user.get",
                            data : { "userId" : userId },
                            callback : function(user) {
                                $(new UdUser(userId, new Map(user))).editUserDialog();
                            }
                        });
                    }
                
                    $("button.create-new-user").click(function() {
                        var attrs = new Map();
                        attrs.put("fullName", "");
                        attrs.put("lastName", "");
                        attrs.put("shortUid", "");
                        $(new UdUser("new", attrs)).editUserDialog("create");
                    });
                    
                    
                    $("button.create-new-group").click(function() {
                        var createGroupDialog = $("div.template.create-group-dialog").clone();
                        createGroupDialog.children("form").submit(function(data) {
                            var shortGroupId = $(this).find("input[name=\"shortGroupId\"]").val();
                            var description = $(this).find("input[name=\"description\"]").val();
                            var groupId = shortGroupId + "@groups." + currentDomain;
                            rpcService.call({
                                action : "group.add",
                                data : {
                                    "groupId" : groupId,
                                    "description" : description
                                },
                                callback : function(response) {
                                    if(bool(response.success)) {
                                        addGroup(groupId);
                                        createGroupDialog.dialog('destroy');
                                    }
                                }
                            });
                            
                            return false;
                        });
                        createGroupDialog.dialog({ title : "Create Group" });
                        
                    });
                    
                    $("button.edit-group").click(function() {
                        
                        var groupId = groupNavySelector.children("li.ui-selected").first().attr("id");
                    
                        rpcService.call({
                            action : "group.get",
                            data : { "groupId" : groupId },
                            callback : function(response) {
                                var updateGroupDialog = $("div.template.create-group-dialog").clone();
                                var form = updateGroupDialog.children("form");
                                form.find("button").text("Update");
                                form.find("input[name=\"description\"]").val(response.description);
                                form.find("input[name=\"shortGroupId\"]").val(showEntryId(groupId)).attr("disabled", true);
                                form.submit(function(data) {
                                    var description = $(this).find("input[name=\"description\"]").val();
                                    rpcService.call({
                                        action : "group.mod",
                                        data : {
                                            "groupId" : groupId,
                                            "description" : description
                                        },
                                        callback : function(response) {
                                            alert(response.success);
                                        }
                                    });
                                    return false;
                                });
                                updateGroupDialog.dialog({ 
                                    title : "Edit Group",
                                    close : function() {
                                        $(this).dialog("destroy");
                                    }
                                });
                            }
                        });
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

            #group-navy-ui {
                margin-left: 21%;
                margin-top: 3%;
                float : left;
                background-color: #dddddd;
            }

            .group-navy-ui-selector {
                float: left;
                background-color: #dddddd;
            }

            .group-navy-ui-selector div {
                padding: 0.15em 1.5em 0.15em 1em;
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
            .group-navy-ui-selectable { list-style-type: none; margin-top: 5.5em; padding: 0; width: auto; cursor: pointer; }
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

            .template {
                display : none;
            }

            .status-line {
                width: 100%;
                height: 1.5em;
                text-align: center;
            }

            .status-text {
                -moz-border-radius: 6px;
                -webkit-border-radius: 6px;
                border-radius: 6px;
            }

            .status-text.success {
                background-color: green;
                padding : 2px 10px;
            }

            .status-text.error {
                background-color: red;
                padding : 2px 10px;
            }

            .user-edit-ui,.user-membership-edit-ui,.collapsible {
                border-top: 1px solid gray;
                margin: 0.5em 0em;
                padding: 0.5em 0em;
            }

            .user-membership-edit-ui-wrap,.user-edit-ui-wrap, .collapsible-wrap {
                border: 1px solid gray;
                padding: 0.5em 0.5em;
                margin: 0.5em 0.2em;
                -moz-border-radius: 6px;
                -webkit-border-radius: 6px;
                border-radius: 6px;
            }

            .user-edit-dialog {
                min-width: 25em;
            }

            .create-group-dialog button {
                float : right;
            }

            .group-toolbar {
                padding : 5px;
                margin : 0 0 10px 0;
                background-color: #AAAAAA;
            }
            
            .user-edit-ui-remove {
                
            }
            
            .user-remove-ui {
                text-align: center;
            }

        </style>
    </head>
    <body>
        <div id="group-navy-ui">
            <div class="group-navy-ui-selector">
                <select class="domain-list"></select>
                <ol class="group-navy-ui-selectable">
                </ol>
            </div>
            <div class="group-navy-ui-display-area">
                <div class="group-toolbar">
                    <button class="create-new-user" type="button">New User</button>
                    <button  class="create-new-group" type="button">New Group</button>
                    <button class="edit-group">Edit Group</button>
                    <button class="remove-group">Remove Group</button>
                    <button class="domain-settings">Domain Settings</button>
                </div>
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

        <div class="template create-group-dialog">
            <form>
                <table>
                    <tr>
                        <td>Group ID</td><td><input type="text" name="shortGroupId" value="" /></td>
                    </tr>
                    <tr>
                        <td>Description</td><td><input type="text" name="description" value="" /></td>
                    </tr>
                </table>
                <button type="submit">Create</button>
            </form>
        </div>


        <div class="template user-edit-dialog" >
            <div class="status-line"></div>
            
            <div class="user-membership-edit-ui-wrap collapsible-wrap">
                <a class="collapse-control" href="javascript:;">Membership</a>
                <div class="collapsible user-membership-edit-ui">
                </div>
            </div>
            <div class="user-edit-ui-wrap">
                <a  href="javascript:;">Attributes</a>
                <div class="user-edit-ui"></div>
            </div>
            <div class="user-edit-ui-remove-ui-wrap collapsible-wrap">
                <a class="collapse-control" href="javascript:;">Remove this user</a>
                <div class="collapsible user-remove-ui">
                    Are you sure?<br/>
                    <a class="user-edit-ui-remove" href="javascript:;">Remove!</a>
                </div>
            </div>
        </div>

    </body>
</html>