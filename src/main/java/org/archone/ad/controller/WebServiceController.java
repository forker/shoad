/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.ad.controller;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.http.HttpSession;
import org.archone.ad.rpc.RpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author forker
 */
@Controller
public class WebServiceController {
    
    @Autowired
    private RpcService rpcService;

    @RequestMapping("/rpc")
    public void rpc(HttpSession session, @RequestBody String body, Writer writer) throws IOException,
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        rpcService.process(session, body, writer);
    }
}