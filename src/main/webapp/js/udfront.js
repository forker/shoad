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




(function($) {
    var methods = {
        newDelLink : function() {
            return $("<a href=\"javascript:;\">[-]</a>");
        },
        newMVInput : function(value) {
            var delLink = $("<a href=\"javascript:;\">[-]</a>");
            var li = $("<li/>")
            $("<input/>").attr("type", "text").val(value).appendTo( li );
            li.append(this.newDelLink().click(function() {
                var li = $(this).parent("li");
                var ol = li.parent("ol");
                var row = ol.parent("td").parent("tr");
                if(ol.find("li").length == 1) {
                    row.remove();
                } else {
                    li.remove();
                }
            }));
            return li;
        },
        newSVInput : function(type, value) {
            var input = $("<input/>").attr("type", type);
            type == "checkbox" ? input.attr("checked", value) : input.val(value);
            return $("<span/>")
            .append( input )
            .append(this.newDelLink().click(function() {
                $(this).parent("span").parent("td").parent("tr").remove();
            }));
        }
    }

    $.fn.extend({
        mapenform : function(data) {
            var map = data.source;
            var target = $(this).first();
            var mapTable = $("<table></table>").appendTo(target);
            for(var key in map.getData()) {
                var currentRow = $("<tr/>").attr("for", key).appendTo(mapTable);
                currentRow.append("<td>"+ data.getLabel(key) +":</td>");
                var valueCell = $("<td/>").appendTo(currentRow);
                                
                                
                var delLink = $("<a href=\"javascript:;\">[-]</a>");
                if(map.get(key) instanceof Array) {
                    var valueList = $("<ol/>").appendTo(valueCell);
                    for(var value in map.get(key)) {
                        methods.newMVInput(map.get(key)[value]).appendTo(valueList);
                                        
                    }
                                    
                    $("<a href=\"javascript:;\">Add value</a>").appendTo(valueCell).click(function() {
                        methods.newMVInput("").appendTo( $(this).siblings("ol") );
                    });
                                    
                } else {
                    if(typeof(map.get(key)) == "boolean") {
                        methods.newSVInput("checkbox", map.get(key)).appendTo(valueCell);
                    } else {
                        methods.newSVInput("text", map.get(key)).appendTo(valueCell);
                    }
                }
            }
                            
                            
            $("<button/>").attr("type", "button").text("Click").appendTo(target).click(function() {
                var newMap = new Map()
                mapTable.find("tr").each(function() {
                    var key = $(this).attr("for");
                                    
                    if(map.get(key) instanceof Array) {
                        var values = [];
                        $(this).find("input").each(function() {
                            values.push($(this).val());
                        });
                        newMap.put(key, values);
                    } else if(typeof(map.get(key)) == "boolean") {
                        newMap.put(key, $(this).find("input").attr("checked") == "checked");
                    } else {
                        newMap.put(key,  $(this).find("input").val());
                    }          
                });
                data.callback(newMap);
            });
                            
                            
                        
        }
    });

})(jQuery);