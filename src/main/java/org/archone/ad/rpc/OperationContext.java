/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.ad.rpc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.archone.ad.authentication.BasicUser;

/**
 *
 * @author forker
 */
public class OperationContext {
    
    private HttpSession session;
    private HashMap<String, Object> params;
    private List<String> errors = new LinkedList<String>();
    private BasicUser basicUser;

    public OperationContext(HttpSession session, BasicUser basicUser, HashMap<String, Object> params) {
        this.basicUser = basicUser;
        this.session = session;
        this.params = params;
    }
     
    /**
     * @return the params
     */
    public HashMap<String, Object> getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(HashMap<String, Object> params) {
        this.params = params;
    }

    /**
     * @return the session
     */
    public HttpSession getSession() {
        return session;
    }

    /**
     * @param session the session to set
     */
    public void setSession(HttpSession session) {
        this.session = session;
    }
    
    public void pushError(String error) {
        this.errors.add(error);
    }
    
    public List<String> getErrors() {
        return this.errors;
    }
    
    public boolean hasErrors() {
        return this.errors.size() > 0;
    }

    /**
     * @return the basicUser
     */
    public BasicUser getBasicUser() {
        return basicUser;
    }

    /**
     * @param basicUser the basicUser to set
     */
    public void setBasicUser(BasicUser basicUser) {
        this.basicUser = basicUser;
    }
   
}