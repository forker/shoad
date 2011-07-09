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
            
            (function($) {
                        
                var UserEditUI = function(elementId) {
                    this.title = "Edit User";
                    this.buttonText = "Update";
                    this.root = $("div.template.user-edit-dialog").clone().attr("id", elementId).appendTo($(document.body));
                    this.showMembershipEdit = true;
                    
                    this.mode = function(mode) {
                        if(mode == "edit") {
                            this.title = "Edit User";
                            this.buttonText = "Update";
                            this.root.find("div.user-membership-edit-ui-wrap").show();
                        } else if(mode == "create") {
                            this.title = "Create User";
                            this.buttonText = "Create";
                            this.root.find("div.user-membership-edit-ui-wrap").hide();
                        }
                        this.root.find("div.user-edit-ui > button").text(this.buttonText);                            
                    };
                    
                    this.start = function(data) {
                        $(this.root).find(".user-edit-ui").mapenform(data);
                    };
                    
                    this.showNotice = function(message, success) {
                        var noticeClass = success ? "success" : "error";
                        var statusLine = this.root.find(".status-line");
                        statusLine.empty()
                        $("<span/>").addClass("status-text").addClass(noticeClass).text(message).appendTo(statusLine).delay(2000).fadeOut(200);
                    }
                    
                    
                };
                        
                var data = new Map();
                var attrDefs = null;
                var showFullEntryId = false;
                var rpcService = new RPCService("rpc");
                var allGroups = null;
                var methods = {
                    
                    loadAttrDefs : function(callback) {
                        rpcService.call({
                            action : "attr.defs.get",
                            callback : function(data) {
                                attrDefs = new Map(data);
                                callback();
                            }
                        });
                    },
                    
                    showNotice : function(elData, message, success) {
                        var noticeClass = success ? "success" : "error";
                        var statusLine = elData.get("userEditRoot").find(".status-line");
                        statusLine.empty()
                        $("<span/>").addClass("status-text").addClass(noticeClass).text(message).appendTo(statusLine);
                        statusLine.show().delay(2000).slideUp(200);
                    },
                    
                    showEntryId : function(fullUserId) {
                        return showFullEntryId ? fullUserId.toString() : fullUserId.split("@")[0];
                    },
                   
                    addGroupToAddList : function(userMembershipEditRoot, groupId) {
                        $("<option/>").val(groupId).text( methods.showEntryId( groupId ) ).appendTo(userMembershipEditRoot.children("select"));
                    },
                    
                    addGroup : function(userMembershipEditRoot, groupId) {
                        $("<tr for=\"" + groupId + "\"><td>" + methods.showEntryId( groupId ) + "</td><td><a href=\"javascript:;\" class=\"group-membership-del\">[-]</a></td></tr>")
                        .appendTo(userMembershipEditRoot.children("table")).children("td").children("a").click(function() {
                            methods.addGroupToAddList(userMembershipEditRoot, $(this).parent("td").parent("tr").attr("for") );
                            $(this).parent("td").parent("tr").remove();
                        });
                    }
                };

                $.fn.extend({
                    
                    loadGroups : function(groups) {
                        allGroups = groups;
                    },
                                   
                    editUserDialog : function(action) {
   
                        if(attrDefs == null) {
                            var elements = $(this);
                            methods.loadAttrDefs(function() {
                                elements.editUserDialog(action);
                            });
                            return this;
                        }
                    
                        var isNewUser = (action == "create");
                        
                        var user = $(this).first()[0];
                            
                        var elementId = user.getUserId() + "-"  + guid();
                        var userEditUI = new UserEditUI(elementId);
                        userEditUI.mode(action);
                        userEditUI.start({ source : user.getAttrs(),
                            schema : attrDefs,
                            sbTitle : userEditUI.buttonText,
                            callback : function(newMap) {
                                if(isNewUser) {
                                    user.userId = newMap.get("shortUid") + "@users.example.com";
                                    
                                    var request = $.extend(true, {}, newMap);
                                    
                                    request.put("userId", user.getUserId());

                                    rpcService.call({
                                        action : "user.add",
                                        data : request.getAsJsObj(),
                                        callback : function(data) {
                                            var noticeText = data.success ? "Successfully Created" : "Create Failed";
                                            userEditUI.showNotice( noticeText, data.success);
                                            userEditUI.mode("edit");
                                            isNewUser = false;
                                            
                                            user.setAttrs(newMap);
                                        }
                                    });
                                    
                                } else {
                                    
                                    var request = new Map();
                                    request.put("mod", new Map());
                                    request.put("add", new Map());
                                    request.put("remove", new Map());
                        
                                    var beforeKeys = user.getAttrs().getKeys();
                                    var afterKeys = [];
                                    
                                    $.each(newMap.getData(), function(apiKey, value) {
                                        afterKeys.push(apiKey);
                                        if(attrDefs.get(apiKey).multiValue == true) {
                                            var newValArray = value;
                                            
                                            if(!user.hasAttr(apiKey) || typeof(user.getAttr(apiKey)) == "undefined") { user.setAttr(apiKey, []); }
                                            
                                            if(!areArraysEqual(newValArray, user.getAttr(apiKey))) {
                                                var arrDiff = new ArrayDiff(newValArray, user.getAttr(apiKey));
                                                request.get("add").put( apiKey, arrDiff.firstOnly);
                                                request.get("remove").put( apiKey, arrDiff.secondOnly);
                                            }
                                
                                        } else if(attrDefs.get(apiKey).type == "boolean" ) {
                                            var newVal = value;
                             
                                            if(newVal != user.getAttr(apiKey)) {
                                                request.get("mod").put( apiKey, newVal);
                                            }
                                
                                        } else {
                                            var newVal = value;
                            
                                            if(newVal != user.getAttr(apiKey)) {
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
                                        data : { userId : user.getUserId(),
                                            mod : request.get("mod").getAsJsObj(),
                                            add : request.get("add").getAsJsObj(),
                                            remove : request.get("remove").getAsJsObj(),
                                            removeAttr : request.get("removeAttr") },
                                        callback : function(data) {
                                            var noticeText = data.success ? "Successfully Updated" : "Update Failed";
                                            userEditUI.showNotice(noticeText, data.success);
                                        }
                                    });
                                }
                            }
                        });
                                                       
                        userEditUI.mode(isNewUser ? "create" : "edit");
                        
                        
                        userEditUI.root.dialog({
                            close : function() {
                                userEditUI.root.remove();
                            },
                            title : userEditUI.title,
                            width : "auto",
                            modal : true
                        });
                        
                        userEditUI.root.find("a.user-edit-ui-remove").click(function() {
                            rpcService.call({
                                action : "user.remove",
                                data : { userId : user.getUserId() },
                                callback : function(data) {                       
                                    if(data.success) {
                                        userEditUI.root.remove();
                                    }
                                }
                            });
                        });
                        
                        
                        userEditUI.root.find(".user-edit-ui-collapse-control").click(function() {
                            
                            $(this).unbind("click");
                            var nextDiv = $(this).next();
                            
                            rpcService.call({
                                action : "user.membership.get",
                                data : { userId : user.getUserId() },
                                callback : function(data) {
                                    user.setGroups([]);
                                    $(data.groups).each(function() {
                                        user.getGroups().push(this.groupId);
                                    });
                                    
                                    nextDiv.show('slow');
                                    
                                    userEditUI.root.find(".user-membership-edit-ui").listenform({
                                        membership : user.getGroups(),
                                        available : allGroups,
                                        getLabel : function(groupId) { return methods.showEntryId(groupId) },
                                        callback : function(newGroupSet, oldGroupSet) {
                                            if(!areArraysEqual(newGroupSet, oldGroupSet)) {
                                                var arrDiff = new ArrayDiff(newGroupSet, oldGroupSet);
                                                rpcService.call({
                                                    action : "user.membership.mod",
                                                    data : { userId : user.getUserId(), add : arrDiff.firstOnly, remove : arrDiff.secondOnly },
                                                    callback : function(data) {
                                                        var noticeText = data.success ? "Successfully Updated" : "Update Failed";
                                                        userEditUI.showNotice(noticeText, data.success);
                                                    }
                                                });
                                                        
                                                user.setGroups(newGroupSet);
                                            }
                                        }
                                    });
                                }
                            });
                            
                            return false;
                        }).next().hide();
                        
                        
                        
                    }
                });

            })(jQuery);
            
            $(function() {
                /*
                 * Initializing in an isolated scope
                 */
                (function() {

                    var rpcService = new RPCService("rpc");

                    var adminDomains = null;
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
                                adminDomains = data.domains;
                            }
                        }
                    });

                    rpcService.call({
                        action : "group.list",
                        data : { domain : "example.com" },
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
                            data : { domain : "example.com", "groups" : groups },
                            callback : function(data) {
                                
                                //groupNavyDisplayTable.children("tbody").empty();
                                displayDataTable.fnClearTable();
                        
                                $(data.users).each(function() {
                                    //groupNavyDisplayTable.append("<tr id=\"" + this.userId + "\"><td><span>" + showEntryId(this.userId) + "</span></td><td>" + this.fullName + "</td></tr>");
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
                            var groupId = shortGroupId + "@groups.example.com";
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

            .user-edit-ui,.user-membership-edit-ui {
                border-top: 1px solid gray;
                margin: 0.5em 0em;
                padding: 0.5em 0em;
            }

            .user-membership-edit-ui-wrap,.user-edit-ui-wrap {
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

        </style>
    </head>
    <body>
        <div id="group-navy-ui">
            <div class="group-navy-ui-selector">
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
            <a class="user-edit-ui-remove" href="javascript:;">Remove this user</a>
            <div class="user-membership-edit-ui-wrap">
                <a class="user-edit-ui-collapse-control" href="javascript:;">Membership</a>
                <div class="user-membership-edit-ui">
                </div>
            </div>
            <div class="user-edit-ui-wrap">
                <a  href="javascript:;">Attributes</a>
                <div class="user-edit-ui"></div>
            </div>
        </div>

    </body>
</html>