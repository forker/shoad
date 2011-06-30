<%-- 
    Document   : plugin-example
    Created on : Jun 30, 2011, 1:40:53 PM
    Author     : forker
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <script type="text/javascript">
            if(typeof($)!="undefined") { (function($) {
                    $.tooltip = {

                        tooltipcss : {
                            'position': 'absolute',
                            'display' : 'block',
                            'top' : '0',
                            'left' : '0',
                            'padding' : '1',
                            'width' : '200px',
                            'background-color' : '#EDEDED',
                            'border-width': '1px',
                            'border-style': 'solid',
                            'border-color': '#696969'
                        },

                        smfr : 0,
                        classname : "tooltip",
                        current : new Object(),

                        showtooltip : function(event, el) {
                            $.tooltip.hidetooltip.unlock();

                            $("." + $.tooltip.classname).hide();
                            el = $(this);
                            var tooltip = $("#" + el.attr("id") + " ~ ." + $.tooltip.classname + ":first");
                            tooltip.css($.tooltip.tooltipcss);
                            tooltip.css("top", event.pageY + 10);
                            tooltip.css("left", event.pageX);
                        },

                        hidetooltip : function(id) {
                            var tooltip = $("#" + id + " ~ ." + $.tooltip.classname + ":first");
                            tooltip.hide();
                        }
                    }

                    $.fn.extend({
                        tooltip : function(classname) {

                            if(classname!=null) $.tooltip.classname = classname;
                            this.each(function(el) {
                                el = $(this);
                                el.mouseover($.tooltip.showtooltip);
                                el.mouseout(function() {
                                    window.setTimeout("$.tooltip.hidetooltip.pexec('"+ this.id +"')", 500);
                                });

                                var tooltip = $("#" + el.attr("id") + " ~ ." + $.tooltip.classname + ":first");
                                tooltip.hide();
                                tooltip.mouseover(
                                function() {
                                    $.tooltip.hidetooltip.lock();
                                    $(this).mouseout(function(event) {
                                        var tly = $(this).position().top;
                                        var bry = $(this).position().top + $(this).height();
                                        var tlx = $(this).position().left;
                                        var brx = $(this).position().left + $(this).width();
                                        if( tlx < event.pageX && brx > event.pageX && tly < event.pageY && bry > event.pageY) 
                                            return;

                                        $(this).hide(); $(this).die();
                                    });
                                }
                            );
                            });
                            return this;
                        }
                    });

                })(jQuery);}
        </script>
    </head>
    <body>
        <h1>Hello World!</h1>
    </body>
</html>
