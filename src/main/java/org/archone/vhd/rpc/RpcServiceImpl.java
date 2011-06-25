/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.vhd.rpc;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Session;
import javax.servlet.http.HttpSession;
import org.archone.vhd.model.RPCAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 *
 * @author forker
 */
public class RpcServiceImpl implements RpcService {
    
    private List<String> actionClasses;
    private HashMap<String, HashMap<String, Object>> callMap = new HashMap<String, HashMap<String, Object>>();
    
    @Autowired
    private ApplicationContext applicationContext;
    
    public void init() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        for(String className : actionClasses) {
            Object object = applicationContext.getBean(Class.forName(className));
            for(Method method : Class.forName(className).getMethods()) {
                if(method.isAnnotationPresent(RPCAction.class)) {
                    
                    //Setting up method invocation vars
                    HashMap<String, Object> rpcCall = new HashMap<String, Object>();
                    rpcCall.put("method", method);
                    rpcCall.put("object", object);
                    rpcCall.put("required", method.getAnnotation(RPCAction.class).required());
                    rpcCall.put("optional", method.getAnnotation(RPCAction.class).optional());
                    
                    callMap.put(method.getAnnotation(RPCAction.class).name(), rpcCall);
                    
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Registered rpc call with name {0}", method.getAnnotation(RPCAction.class).name());
                }
            }
        }
    }

    @Override
    public String process(String body) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void process(HttpSession session, String body, Writer writer) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        HashMap<String, Object> request = (HashMap<String, Object>) new JSONDeserializer().deserialize(body);        
        new JSONSerializer().deepSerialize(process(session, request), writer);
    }
    
    public HashMap<String, Object> process(HttpSession session, HashMap<String, Object> request) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        HashMap<String, Object> response = new HashMap<String, Object>();
        
        
        HashMap rpcCall = this.callMap.get( (String) request.get("rpcAction") );
        Assert.notNull(rpcCall, "Action not found");
        
        Method method = (Method) rpcCall.get("method");
        response = (HashMap<String, Object>) method.invoke( rpcCall.get("object"), new OperationContext(session, request) );
        
        return response; 
    }

    /**
     * @return the actionClasses
     */
    public List<String> getActionClasses() {
        return actionClasses;
    }

    /**
     * @param actionClasses the actionClasses to set
     */
    public void setActionClasses(List<String> actionClasses) {
        this.actionClasses = actionClasses;
    }
    
}
