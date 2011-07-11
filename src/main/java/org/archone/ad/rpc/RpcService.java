/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.rpc;

import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.http.HttpSession;

/**
 *
 * @author romansergey
 */
public interface RpcService {
    public String process(String body);
    public void process(HttpSession session, String body, Writer writer) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;
}
