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
            
            (function($) {
                        
                var UserEditUI = function(elementId) {
                    this.title = "Update";
                    this.root = $("div.template.user-edit-dialog").clone().attr("id", elementId).appendTo($(document.body));
                    this.submit = this.root.find("button.user-edit-submit");
                    this.membershipEditRoot = this.root.find("div.user-membership-edit");
                    this.addAttrSelect = this.root.children("select");
                    this.membershipEditSelect = this.root.find("div.user-membership-edit select");
                    this.showMembershipEditAnchor = this.root.find("div.user-membership-edit-wrap a");
                    this.membershipEditSubmit = this.membershipEditRoot.find("button.user-membership-edit-submit")
                    
                    this.getControl = function(name) {
                        switch(name) {
                            case "attr-value-del" :
                                return this.root.find("a.attribute-value-del");
                            case "attr-lines" :
                                return this.root.children("table").children("tbody").children("tr");
                        };
                    };
                    
                    this.mode = function(mode) {
                        if(mode == "edit") {
                            this.title = "Edit User";
                            this.submit.text("Update");
                            this.showMembershipEditAnchor.show();
                        } else {
                            this.title = "Create User";
                            this.submit.text("Create User");
                            this.showMembershipEditAnchor.hide();
                        }
                    };
                    
                    
                };
                        
                var data = new Map();
                var attrDefs = null;
                var showFullEntryId = false;
                var rpcService = new RPCService("/LDAPManager/rpc");
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
                    
                    newAttributeValueInput : function(value) {
                        return $("<input size=\"30\" type=\"text\" value=\"" + value + "\" /><a class=\"attribute-value-del\" href=\"javascript:;\" title=\"delete\">[-]</a><br/>");
                    },
                    
                    newAttributeValueCheckbox : function() {
                        return $("<input type=\"checkbox\" />");
                    },
                    
                    newAddAttributeValueLink : function() {
                        return $("<a class=\"add-attribute-value\" href=\"javascript:;\" >Add more..</a>").click(function() {
                            methods.newAttributeValueInput("").insertBefore($(this));
                        });
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
                    },

                    addAttribute : function(elData, key, isNew) {
                        var userEditRoot = elData.get("userEditRoot");
                        var user = elData.get("user");

                        userEditRoot.children("table").append("<tr for=\"" + key + "\"><td style=\"white-space: nowrap;\">" + attrDefs.get(key).displayName + ":</td><td></td></tr>")
                        var lastTd = userEditRoot.children("table").children("tbody").children("tr:last").children("td:last");
                        if(attrDefs.get(key).multiValue == true) {
                            if(isNew) {
                                lastTd.append(this.newAttributeValueInput(""));
                            } else {
                                for(var i in user.getAttr(key)) {
                                    lastTd.append(this.newAttributeValueInput( user.getAttr(key)[i] ));
                                }
                            }
                            this.newAddAttributeValueLink().appendTo(lastTd);
                        } else if(attrDefs.get(key).type == "boolean") {
                            this.newAttributeValueCheckbox().prop("checked", bool(user.getAttr(key)) ).appendTo(lastTd);
                            lastTd.append("<a class=\"attribute-value-del\" href=\"javascript:;\" title=\"delete\">[-]</a>");
                        } else {
                            if(isNew) {
                                lastTd.append(this.newAttributeValueInput(""));
                            } else {
                                lastTd.append(this.newAttributeValueInput(user.getAttr(key)));
                            }
                        }
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

                        $(this).each(function() {
                            var user = this;
                            
                            var elementId = user.getUserId() + "-"  + guid();
                            var userEditUI = new UserEditUI(elementId);
                            
                            var elData = data.put(user.getUserId(), new Map());
                            elData.put("user", user);
                            elData.put("elementId", elementId);
                            elData.put("userEditRoot", userEditUI.root);
                                
                            for(var key in user.getAttrs().getAsJsObj()) {
                                methods.addAttribute(elData, key);
                            }
                                
                            for(var key in attrDefs.getAsJsObj()) {
                                if(!user.hasAttr(key)) {
                                    $("<option/>").val(key).text(attrDefs.get(key).displayName).appendTo(userEditUI.addAttrSelect);
                                }
                            }
                            
                            userEditUI.getControl("attr-value-del").click(function() {
                                $(this).prev("input").remove();
                                $(this).next("br").remove();
                                if($(this).siblings("input").length == 0) {
                                    $(this).parent("td").parent("tr").remove();
                                } else {
                                    $(this).remove();
                                }
                            });
                            
                            userEditUI.mode(isNewUser ? "create" : "edit");

                            userEditUI.submit.click(function() {
                                if(isNewUser) {
                                    var request = new Map();
                                    
                                    userEditUI.getControl("attr-lines").each(function() {
                                        var apiKey = $(this).attr("for");
                                        var valueInput = $(this).find("td:last input");
                                                                   
                                        if(attrDefs.get(apiKey).multiValue == true) {
                                            var newValArray = [];
                                            valueInput.each(function() {
                                                newValArray.push($(this).val());
                                            });
                            
                                            request.put(apiKey, newValArray);
                                            
                                        } else if(attrDefs.get(apiKey).type == "boolean" ) {
                                            var newVal = valueInput.attr("checked") == "checked" ? "true" : "false";
                                            request.put(apiKey, newVal);
                                        } else {
                                            var newVal = valueInput.val();
                                            request.put(apiKey, newVal);
                                            
                                            if(apiKey == "shortUid") {
                                                user.userId = newVal + "@users.example.com";
                                            }
                                        }
                                    });
                                    
                                    request.put("userId", user.getUserId());

                                    rpcService.call({
                                        action : "user.add",
                                        data : request.getAsJsObj(),
                                        callback : function(data) {
                                            var noticeText = data.success ? "Successfully Created" : "Create Failed";
                                            methods.showNotice(elData, noticeText, data.success);
                                            userEditUI.mode("edit");
                                            isNewUser = false;
                                        }
                                    });
                                    
                                } else {
                                    
                                    var request = new Map();
                                    request.put("mod", new Map());
                                    request.put("add", new Map());
                                    request.put("remove", new Map());
                        
                                    var beforeKeys = user.getAttrs().getKeys();
                        
                                    var afterKeys = [];
                                    userEditUI.getControl("attr-lines").each(function() {
                                                     
                            
                                        var apiKey = $(this).attr("for");
                                        var valueInput = $(this).find("td:last input");
                                        afterKeys.push(apiKey);
                            
                                        if(attrDefs.get(apiKey).multiValue == true) {
                                            var newValArray = [];
                                            valueInput.each(function() {
                                                newValArray.push($(this).val());
                                            });
                            
                                    
                                            if(!user.hasAttr(apiKey) || typeof(user.getAttr(apiKey)) == "undefined") { user.setAttr(apiKey, []); }
                                            if(!areArraysEqual(newValArray, user.getAttr(apiKey))) {
                                                var arrDiff = new ArrayDiff(newValArray, user.getAttr(apiKey));
                                                request.get("add").put( apiKey, arrDiff.firstOnly);
                                                request.get("remove").put( apiKey, arrDiff.secondOnly);
                                            }
                                
                                        } else if(attrDefs.get(apiKey).type == "boolean" ) {
                                            var newVal = valueInput.attr("checked") == "checked" ? "true" : "false";
                             
                                            if(newVal != user.getAttr(apiKey)) {
                                                request.get("mod").put( apiKey, newVal);
                                            }
                                
                                        } else {
                                            var newVal = valueInput.val();
                            
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
                                            methods.showNotice(elData, noticeText, data.success);
                                        }
                                    });
                                }
                                    
                            });
                                               
                            userEditUI.membershipEditSelect.change(function() {
                                methods.addGroup(userEditUI.membershipEditRoot, $(this).val());
                                $(this).children("option:selected").remove();
                            });
                    
                            userEditUI.addAttrSelect.change(function() {
                                methods.addAttribute(elData, $(this).val(), true);
                                $(this).children("option:selected").remove();
                            });
                    
                            userEditUI.showMembershipEditAnchor.click(function() {
                                $(this).hide();
                                userEditUI.membershipEditRoot.show();
                    
                                rpcService.call({
                                    action : "user.membership.get",
                                    data : { userId : user.getUserId() },
                                    callback : function(data) {
                                    
                                        user.setGroups([]);
                                        $(data.groups).each(function() {
                                            methods.addGroup(userEditUI.membershipEditRoot, this.groupId);
                                            user.getGroups().push(this.groupId);
                                        });
                                
                                        if(!areArraysEqual(allGroups, user.getGroups())) {
                                            var arrDiff = new ArrayDiff(allGroups, user.getGroups());
                                            $(arrDiff.firstOnly).each(function() {
                                                methods.addGroupToAddList(userEditUI.membershipEditRoot, this);
                                            });
                                        }
                                    }
                                });
                            });
                        
                            userEditUI.membershipEditSubmit.click(function() {
                                var newGroupSet = [];
                                userEditUI.membershipEditRoot.children("table").children("tbody").children("tr").each(function() {
                                    newGroupSet.push( $(this).attr("for") );
                                });
                        
                                if(!areArraysEqual(newGroupSet, user.getGroups())) {
                                    var arrDiff = new ArrayDiff(newGroupSet, user.getGroups());
                            
                                    rpcService.call({
                                        action : "user.membership.mod",
                                        data : { userId : user.getUserId(), add : arrDiff.firstOnly, remove : arrDiff.secondOnly },
                                        callback : function(data) {
                                            var noticeText = data.success ? "Successfully Updated" : "Update Failed";
                                            methods.showNotice(elData, noticeText, data.success);
                                        }
                                    });                            
                                }
                            });
                        
                            userEditUI.root.dialog({
                                close : function() {
                                    userEditUI.root.remove();
                                },
                                title : userEditUI.title,
                                width : "auto",
                                modal : true
                            });
                        
                        });
                    }
                });

            })(jQuery);
            
            $(function() {
                /*
                 * Initializing in an isolated scope
                 */
                (function() {

                    var rpcService = new RPCService("/LDAPManager/rpc");

                    var groupNavyRoot = $("#group-navy-ui");

                    var groupNavySelector= groupNavyRoot.children("div.group-navy-ui-selector").children("ol.group-navy-ui-selectable");
                    var groupNavyToolbar = groupNavyRoot.children("div.group-navy-ui-toolbar");
                    var groupNavyDisplayTable = groupNavyRoot.children("div.group-navy-ui-display-area").children("table.group-navy-ui-display-table");
                    var displayDataTable = null;
                    var allGroups = [];
                    
                    var showEntryId = function(fullUserId) {
                        return false ? fullUserId.toString() : fullUserId.split("@")[0]
                    }
                
                
                    //                rpcService.call({
                    //                    action : "user.add",
                    //                    data : { userId : "meganfox@users.example.com", fullName : "Megan Fox", lastName : "Fox" },
                    //                    callback : function(data) {                       
                    //                        alert(data.success);
                    //                    }
                    //                });
                
                    rpcService.call({
                        action : "group.list",
                        data : { domain : "example.com" },
                        callback : function(data) {                       
                            groupNavySelector.append("<li id=\"all\" class=\"group-navy-ui-select-li\">All Users</li>");
                            $(data.groups).each(function(index, group) {
                                groupNavySelector.append("<li id=\"" + group.groupId + "\" class=\"group-navy-ui-select-li\">" + showEntryId(group.groupId) + "</li>");
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
                                
                                if(displayDataTable != null) { 
                                    displayDataTable.fnDestroy();
                                }
                                
                                groupNavyDisplayTable.children("tbody").empty();
                        
                                $(data.users).each(function() {
                                    groupNavyDisplayTable.append("<tr id=\"" + this.userId + "\"><td><span>" + showEntryId(this.userId) + "</span></td><td>" + this.fullName + "</td></tr>");
                                });
                                                              
                                displayDataTable = groupNavyDisplayTable.dataTable();
                                
                                groupNavyDisplayTable.children("tbody").children("tr").click(function() {
                                    displayUser( $(this).attr("id") );
                                });
                            }
                        });
                    
                    }

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
                        attrs.put("fullName", "Naomi Watts");
                        attrs.put("lastName", "Watts");
                        attrs.put("shortUid", "nw");
                        //attrs.put("uid", "naomiwatts");
                        $(new UdUser("nw@users.example.com", attrs)).editUserDialog("create");
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

            .template {
                display : none;
            }

            .status-line {
                width: 100%;
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

        </style>
    </head>
    <body>
        <div id="group-navy-ui">

            <div class="group-navy-ui-selector">
                <div><button class="create-new-user" type="button">New User</button><button type="button">New Group</button></div>
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

        <div class="template user-edit-dialog" >
            <div class="status-line"></div>
            <div class="user-membership-edit-wrap">
                <a href="javascript:;">Group membership...</a>
                <div style="display: none;" class="user-membership-edit">
                    <table class="membership-groups-listing" /></table>
                    <select class="add-group-control"><option value="-">Add group...</option></select><br/>
                    <button type="button" class="user-membership-edit-submit">Update Membership</button>
                </div>
                <hr/>
            </div>
            <table class="attribute-listing" ></table>
            <select><option value="-">Add attribute...</option></select><br/>
            <button style="float: right;" type="button" class="user-edit-submit">Update</button>
        </div>

    </body>
</html>