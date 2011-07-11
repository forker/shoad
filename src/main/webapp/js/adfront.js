/* 
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */

UdUser = function(userId, attrs) {
    this.userId = userId;
    this.attrs = attrs;
    this.groups = null;
}

UdUser.prototype.setGroups = function(groups) {
    this.groups = groups;
}

UdUser.prototype.getGroups = function() {
    return this.groups;
}

UdUser.prototype.getAttrs = function() {
    return this.attrs;
}

UdUser.prototype.setAttrs = function(attrs) {
    this.attrs = attrs;
}

UdUser.prototype.getAttr = function(key) {
    return this.attrs.get(key);
}

UdUser.prototype.setAttr = function(key, value) {
    return this.attrs.put(key, value);
}

UdUser.prototype.hasAttr = function(key) {
    return this.getAttr(key) != null && typeof(this.getAttr(key)) != "undefined";
}

UdUser.prototype.removeAttr = function(key) {
    this.attrs.remove(key);
}

UdUser.prototype.getUserId = function() {
    return this.userId;
}

UdUser.current = null;
UdUser.attrDefs = null;




(function($) {
    var schema = null;
    var methods = {
        newLink : function(text) {
            return $("<a href=\"javascript:;\"></a>").text(text);  
        },
        newDelLink : function() {
            return this.newLink("[-]");
        },
       
        removeAttribute : function(target, key) {
            target.find("tr[for=\"" + key + "\"]").remove();
            target.siblings("select").append( $("<option/>").val(key).text(schema.get(key).displayName) );
        },

        newMVInput : function(value) {
            return $("<li/>").append( $("<input/>").attr("type", "text").val(value) )
            .append(this.newDelLink().click(function() {
                var li = $(this).parent("li");
                var ol = li.parent("ol");
                var row = ol.parent("td").parent("tr");
                var table = row.parent("tbody").parent("table");
                if(ol.find("li").length == 1) {
                    methods.removeAttribute(table, row.attr("for"));
                } else {
                    li.remove();
                }
            }));
        },
        newSVInput : function(type, value) {
            var input = $("<input/>").attr("type", type);
            type == "checkbox" ? input.attr( "checked", bool(value) ) : input.val(value);
            return $("<span/>")
            .append( input )
            .append(this.newDelLink().click(function() {
                var row = $(this).parent("span").parent("td").parent("tr");
                var table = row.parent("tbody").parent("table");
                methods.removeAttribute(table, row.attr("for"));
            }));
        },
        addAttribute : function(target, key, value) {
            var currentRow = $("<tr/>").attr("for", key).appendTo(target);
            currentRow.append("<td>"+ schema.get(key).displayName +":</td>");
            var valueCell = $("<td/>").appendTo(currentRow);
            if(schema.get(key).multiValue == true) {
                var valueList = $("<ol/>").appendTo(valueCell);
                for(var idx in value) {
                    methods.newMVInput(value[idx]).appendTo(valueList);
                                        
                }
                methods.newLink("Add value").appendTo(valueCell).click(function() {
                    methods.newMVInput("").appendTo( $(this).siblings("ol") );
                });
            } else {
                if(schema.get(key).type == "boolean") {
                    methods.newSVInput("checkbox", value).appendTo(valueCell);
                } else {
                    methods.newSVInput("text", value).appendTo(valueCell);
                }
            }
        },
        addGroup : function(group, label, ol, addSelect) {
            var li = $("<li/>").appendTo(ol);
            li.attr("for", group);
            methods.newDelLink().appendTo(li).click(function() {
                var groupId = $(this).parent("li").attr("for");
                $(this).parent("li").remove();
                addSelect.append( $("<option/>").val(groupId).text(label) );
            });
            li.append(label);
        }
    }

    $.fn.extend({
        mapenform : function(data) {
            var map = data.source;
            schema = data.schema;
            var target = $(this).first();
            var mapTable = $("<table></table>").appendTo(target);
            
            for(var key in map.getData()) {
                methods.addAttribute(mapTable, key, map.get(key));
            }
            
            var addAttrSelect = $("<select><option>Add Attribute</option></select>").appendTo(target).change(function() {
                var key = $(this).val();
                methods.addAttribute(mapTable, key, map.get(key));
                $(this).children("option:selected").remove();
                
            });
            for(var key in data.schema.getData()) {
                if(!map.containsKey(key)) {
                    $("<option/>").val(key).text(data.schema.get(key).displayName).appendTo(addAttrSelect);
                }
            }
                            
            $("<button/>").attr("type", "button").text(data.sbTitle).appendTo(target).click(function() {
                var newMap = new Map()
                mapTable.find("tr").each(function() {
                    var key = $(this).attr("for");
                                    
                    if(data.schema.get(key).multiValue == true) {
                        var values = [];
                        $(this).find("input").each(function() {
                            values.push($(this).val());
                        });
                        newMap.put(key, values);
                    } else if(data.schema.get(key).type == "boolean") {
                        newMap.put(key, $(this).find("input").attr("checked") == "checked");
                    } else {
                        newMap.put(key,  $(this).find("input").val());
                    }          
                });
                data.callback(newMap);
            });
        }
    });
    
    
    $.fn.extend({
        listenform : function(data) {
            var membership = data.membership;
            var available = data.available;
            var target = $(this);
            var ol = $("<ol/>").appendTo(target);
            var addSelect = $("<select/>").appendTo(target).change(function() {
                var value = $(this).val();
                var label = data.getLabel(value);
                $(this).children("option:selected").remove();
                methods.addGroup(value, label, $(this).siblings("ol"), $(this));
            }).append("<option>Add group..</option>");
            
            for(var idx in membership) {
                var label = data.getLabel(membership[idx]);
                methods.addGroup(membership[idx], label, ol, addSelect);
            }
            
            var arrDiff = new ArrayDiff(available, membership);
            
            for(var idx in arrDiff.firstOnly) {
                var label = data.getLabel(arrDiff.firstOnly[idx]);
                $("<option/>").val(arrDiff.firstOnly[idx]).text(label).appendTo(addSelect);
            }
            
            $("<button/>").text("Update").appendTo(target).click(function() {
                var newGroupSet = [];
                $(this).siblings("ol").children("li").each(function() {
                    var groupId = $(this).attr("for");
                    newGroupSet.push(groupId);
                });
                
                data.callback(newGroupSet, membership);
            });
            
        }
    });

})(jQuery);














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
    var currentDomain = null;
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
        
        setDomain : function(domain) {
            currentDomain = domain;
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
            userEditUI.start({
                source : user.getAttrs(),
                schema : attrDefs,
                sbTitle : userEditUI.buttonText,
                callback : function(newMap) {
                    if(isNewUser) {
                        user.userId = newMap.get("shortUid") + "@users." + currentDomain;
                                    
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
                                
                        //var updatedUser = $.extend(true, {}, user);
                                
                        $.each(newMap.getData(), function(apiKey, value) {
                            afterKeys.push(apiKey);
                            if(attrDefs.get(apiKey).multiValue == true) {
                                var newValArray = value;
                                            
                                if(!user.hasAttr(apiKey) || typeof(user.getAttr(apiKey)) == "undefined") {
                                    user.setAttr(apiKey, []);
                                }
                                            
                                if(!areArraysEqual(newValArray, user.getAttr(apiKey))) {
                                    
                                    //updatedUser.setAttr(apiKey, newValArray);
                                    
                                    var arrDiff = new ArrayDiff(newValArray, user.getAttr(apiKey));
                                    request.get("add").put( apiKey, arrDiff.firstOnly);
                                    request.get("remove").put( apiKey, arrDiff.secondOnly);
                                }
                                
                            } else if(attrDefs.get(apiKey).type == "boolean" ) {
                                var newVal = value;
                             
                                if(newVal != user.getAttr(apiKey)) {
                                    //updatedUser.setAttr(apiKey, newVal);
                                    request.get("mod").put( apiKey, newVal);
                                }
                                
                            } else {
                                var newVal = value;
                            
                                if(newVal != user.getAttr(apiKey)) {
                                    //updatedUser.setAttr(apiKey, newVal);
                                    request.get("mod").put( apiKey, newVal);
                                }
                            }
                        });
                        
                        if(!areArraysEqual(beforeKeys, afterKeys)) {
                            
                            var arrDiff = new ArrayDiff(beforeKeys, afterKeys);
                            request.put("removeAttr", arrDiff.firstOnly);
                            
                            $(arrDiff.firstOnly).each(function() {
                                //updatedUser.removeAttr(this);
                            });
                            
                        }

                        rpcService.call({
                            action : "user.mod",
                            data : {
                                userId : user.getUserId(),
                                mod : request.get("mod").getAsJsObj(),
                                add : request.get("add").getAsJsObj(),
                                remove : request.get("remove").getAsJsObj(),
                                removeAttr : request.get("removeAttr")
                            },
                            callback : function(data) {
                                var noticeText = data.success ? "Successfully Updated" : "Update Failed";
                                userEditUI.showNotice(noticeText, data.success);
                                //user = updatedUser;
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
                    data : {
                        userId : user.getUserId()
                    },
                    callback : function(data) {                       
                        if(data.success) {
                            userEditUI.root.remove();
                        }
                    }
                });
            });
                        
                        
            userEditUI.root.find(".collapse-control").click(function() {
                            
                $(this).unbind("click");
                var nextDiv = $(this).next();
                
                if(nextDiv.hasClass("user-membership-edit-ui")) {
                    rpcService.call({
                        action : "user.membership.get",
                        data : {
                            userId : user.getUserId()
                        },
                        callback : function(data) {
                            user.setGroups([]);
                            $(data.groups).each(function() {
                                user.getGroups().push(this.groupId);
                            });
                                    
                            nextDiv.show('slow');
                                    
                            userEditUI.root.find(".user-membership-edit-ui").listenform({
                                membership : user.getGroups(),
                                available : allGroups,
                                getLabel : function(groupId) {
                                    return methods.showEntryId(groupId)
                                },
                                callback : function(newGroupSet, oldGroupSet) {
                                    if(!areArraysEqual(newGroupSet, oldGroupSet)) {
                                        var arrDiff = new ArrayDiff(newGroupSet, oldGroupSet);
                                        rpcService.call({
                                            action : "user.membership.mod",
                                            data : {
                                                userId : user.getUserId(), 
                                                add : arrDiff.firstOnly, 
                                                remove : arrDiff.secondOnly
                                            },
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
                } else {
                    nextDiv.show('slow');
                }
                            
                return false;
            }).next().hide();
                            
            return this;               
        }
    });

})(jQuery);