/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.vhd.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.archone.vhd.authentication.BasicUser;
import org.archone.vhd.rpc.OperationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

/**
 *
 * @author forker
 */
public class LdapModel {

    @Autowired
    private NameHelper nameHelper;

    
    public BasicUser getBasicUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof BasicUser)) {
            throw new RuntimeException("Unexpected princiapl object");
        }
        
        return ((BasicUser) principal);
    }
    
    
    /*
     * listUsers
     * required fields: domain
     */
    @RPCAction(name = "listUsers")
    public HashMap<String, Object> listUsers(OperationContext opContext) throws NamingException {
        
        String domain = (String) opContext.getParams().get("domain");
        Assert.notNull(domain, "Domain can't be null in this request");

        BasicUser basicUser = getBasicUser();
        AccessControl accessControl = new AccessControl(nameHelper);

        /*
         * Access Control Start
         */
        
        accessControl.validateAccess(basicUser, domain);
        
        /*
         * Access Control End
         */

        DomainDn domainDn = nameHelper.newDomainDnFromDomain(domain);
        
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        DirContext dirContext = basicUser.getDirContext();

        NamingEnumeration<SearchResult> searchResults = dirContext.search(nameHelper.getUsersBaseDn(domainDn), "(uid=*)", controls);

        List<HashMap<String,Object>> users = new LinkedList<HashMap<String,Object>>();
        while (searchResults.hasMore()) {
            SearchResult sr = searchResults.next();
            if ( nameHelper.isUserDn( sr.getNameInNamespace().toLowerCase() )) {
                HashMap<String, Object> user = new HashMap<String, Object>();
                user.put("userId", nameHelper.newUserDn(sr.getNameInNamespace()).getAsUserId());
                user.put("fullName", sr.getAttributes().get("cn").get());
                users.add(user);
            }
        }

        HashMap<String, Object> response = new HashMap<String, Object>();
        
        response.put("users", users);

        return response;
    }

    /*
     * getUser
     * required fields: userId
     */
    @RPCAction(name="getUser")
    public HashMap<String, Object> getUser(OperationContext opContext) throws NamingException {
        
        String userId = (String) opContext.getParams().get("userId");
        Assert.notNull(userId, "userId can't be null in this request");

        BasicUser basicUser = getBasicUser();
        DirContext dirContext = basicUser.getDirContext();
                
        UserDn userDn = nameHelper.newUserDnFromId(userId);
        
        DirContextAdapter dca = (DirContextAdapter) dirContext.lookup(userDn);
                     
        HashMap<String, Object> user = new HashMap<String, Object>();
        
        user.put("Full Name", dca.getStringAttribute("cn"));
        user.put("Surname", dca.getStringAttribute("sn"));
               
        return user;
    }
    
}
