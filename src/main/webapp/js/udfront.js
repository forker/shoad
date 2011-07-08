/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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