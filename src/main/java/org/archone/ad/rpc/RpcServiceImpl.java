/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.ad.rpc;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import org.archone.ad.authentication.BasicUser;
import org.archone.ad.model.MalformedRequestException;
import org.archone.ad.security.SecuredMethod;
import org.archone.ad.security.SecurityConstraint;
import org.archone.ad.security.SecurityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

/**
 *
 * @author forker
 */
public class RpcServiceImpl implements RpcService {

    private List<String> actionClasses;
    private List<String> securityConstraintClasses;
    private HashMap<String, HashMap<String, Object>> callMap = new HashMap<String, HashMap<String, Object>>();
    private HashMap<String, HashMap<String, Object>> scMap = new HashMap<String, HashMap<String, Object>>();
    
    @Autowired
    private ApplicationContext applicationContext;

    public void init() throws ClassNotFoundException, InstantiationException, SecurityViolationException {
        for (String className : actionClasses) {
            Object object = applicationContext.getBean(Class.forName(className));
            for (Method method : Class.forName(className).getMethods()) {
                if (method.isAnnotationPresent(RPCAction.class)) {

                    //Setting up method invocation vars
                    HashMap<String, Object> rpcCall = new HashMap<String, Object>();
                    rpcCall.put("method", method);
                    rpcCall.put("object", object);
                    rpcCall.put("required", method.getAnnotation(RPCAction.class).required());
                    rpcCall.put("optional", method.getAnnotation(RPCAction.class).optional());
                    rpcCall.put("role", method.getAnnotation(RPCAction.class).role());

                    callMap.put(method.getAnnotation(RPCAction.class).name(), rpcCall);

                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Registered rpc call with name {0}", method.getAnnotation(RPCAction.class).name());
                }
            }
        }
        
        for(String className : getSecurityConstraintClasses()) {
            Object object = applicationContext.getBean(Class.forName(className));
            for (Method method : Class.forName(className).getMethods()) {
                if(method.isAnnotationPresent(SecurityConstraint.class)) {
                    HashMap<String, Object> scCheck = new HashMap<String, Object>();
                    scCheck.put("method", method);
                    scCheck.put("object", object);
                    
                    String name = method.getAnnotation(SecurityConstraint.class).name();
                    scMap.put(name, scCheck);
                    
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Registered Security Constraint with name {0}", name);
                }
            }
        }
        
    }

    @Override
    public String process(String body) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void process(HttpSession session, String body, Writer writer) throws SecurityViolationException, IllegalArgumentException, InvocationTargetException, IllegalAccessException {
        HashMap<String, Object> request = (HashMap<String, Object>) new JSONDeserializer().deserialize(body);
        new JSONSerializer().deepSerialize(process(session, request), writer);
    }

    public HashMap<String, Object> process(HttpSession session, HashMap<String, Object> request) throws SecurityViolationException, IllegalArgumentException, InvocationTargetException, IllegalAccessException {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof BasicUser)) {
            throw new RuntimeException("Unexpected principal object");
        }

        BasicUser basicUser = (BasicUser) principal;

        HashMap rpcCall = this.callMap.get((String) request.get("rpcAction"));
        Assert.notNull(rpcCall, "Action not found");

        try {

            String[] roles = (String[]) rpcCall.get("role");
            for (String role : roles) {
                if (basicUser.hasAuthority(role)) {

                    String[] requiredFields = (String[]) rpcCall.get("required");
                    for (String field : requiredFields) {
                        if (!request.containsKey(field)) {
                            throw new MalformedRequestException("Field " + field + " required for this request");
                        }
                    }

                    Method method = (Method) rpcCall.get("method");
                    
                    /*
                     * Performing security checks if method is annotated with SecuredMethod
                     */
                    if(method.isAnnotationPresent(SecuredMethod.class)) {
                        String[] constraints = method.getAnnotation(SecuredMethod.class).constraints();
                        
                        for(String constraint : constraints) {
                            HashMap<String, Object> scUnit = scMap.get(constraint);
                            if(scUnit != null) {
                                Method scMethod = (Method) scUnit.get("method");
                                scMethod.invoke(scUnit.get("object"), new OperationContext(session, (BasicUser) principal, request));
                            } else {
                                throw new RuntimeException("Failed to find a required security constraint");
                            }
                        }
                    }
                    
                    return (HashMap<String, Object>) method.invoke(rpcCall.get("object"), new OperationContext(session, (BasicUser) principal, request));
                }
            }
            
            throw new SecurityViolationException("Access Forbidden");
            
        } catch (RuntimeException ex) {
            
            if(true) {
                throw ex;
            }
            
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, ex.getMessage());
            
            HashMap<String, Object> response = new HashMap<String, Object>();
            response.put("success", false);

            if (ex instanceof MalformedRequestException) {
                response.put("errors", new String[]{ex.getMessage()});
            } else if(ex instanceof SecurityViolationException) {
                response.put("errors", new String[]{"Access Denied"});
            } else {
                response.put("errors", new String[]{"Unknown Error"});
            }
            
            return response;
        }
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

    /**
     * @return the securityConstraintClasses
     */
    public List<String> getSecurityConstraintClasses() {
        return securityConstraintClasses;
    }

    /**
     * @param securityConstraintClasses the securityConstraintClasses to set
     */
    public void setSecurityConstraintClasses(List<String> securityConstraintClasses) {
        this.securityConstraintClasses = securityConstraintClasses;
    }
}
