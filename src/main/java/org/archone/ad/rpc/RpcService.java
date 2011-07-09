/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.ad.rpc;

import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.http.HttpSession;

/**
 *
 * @author forker
 */
public interface RpcService {
    public String process(String body);
    public void process(HttpSession session, String body, Writer writer) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;
}
