/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.vhd.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
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
    @Autowired
    private DisplayAttributeHelper displayAttributeHelper;

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
    @RPCAction(name = "user.list")
    public HashMap<String, Object> listUsers(OperationContext opContext) throws NamingException {

        String domain = (String) opContext.getParams().get("domain");
        Assert.notNull(domain, "Domain can't be null in this request");

        List<String> groups = (List<String>) opContext.getParams().get("groups");

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

        HashMap<String, HashMap<String, Object>> users = new HashMap<String, HashMap<String, Object>>();

        if (groups == null || groups.isEmpty()) {
            NamingEnumeration<SearchResult> searchResults = dirContext.search(nameHelper.getUsersBaseDn(domainDn), "(uid=*)", controls);

            while (searchResults.hasMore()) {
                SearchResult sr = searchResults.next();
                if (nameHelper.isUserDn(sr.getNameInNamespace().toLowerCase())) {
                    HashMap<String, Object> user = new HashMap<String, Object>();
                    String userId = nameHelper.newUserDn(sr.getNameInNamespace()).getAsUserId();
                    user.put("userId", userId);
                    user.put("fullName", sr.getAttributes().get("cn").get());
                    users.put(userId, user);
                }
            }
        } else {
            for (String groupId : groups) {
                GroupDn groupDn = nameHelper.newGroupDnFromId(groupId);
                DirContextAdapter ldapGroup = (DirContextAdapter) dirContext.lookup(groupDn);

                SortedSet<String> userDnList = ldapGroup.getAttributeSortedStringSet("uniqueMember");

                for (String userDnStr : userDnList) {
                    UserDn userDn = nameHelper.newUserDn(userDnStr);
                    if (!users.containsKey(userDn.getAsUserId())) {
                        HashMap<String, Object> user = new HashMap<String, Object>();
                        DirContextAdapter ldapUser = (DirContextAdapter) dirContext.lookup(userDn);

                        user.put("userId", userDn.getAsUserId());
                        user.put("fullName", ldapUser.getStringAttribute("cn"));

                        users.put(userDn.getAsUserId(), user);
                    }
                }
            }
        }

        HashMap<String, Object> response = new HashMap<String, Object>();

        response.put("users", users.values());

        return response;
    }

    /*
     * getUser
     * required fields: userId
     */
    @RPCAction(name = "user.get")
    public HashMap<String, Object> getUser(OperationContext opContext) throws NamingException {

        String userId = (String) opContext.getParams().get("userId");
        Assert.notNull(userId, "userId can't be null in this request");

        BasicUser basicUser = getBasicUser();
        DirContext dirContext = basicUser.getDirContext();

        UserDn userDn = nameHelper.newUserDnFromId(userId);

        DirContextAdapter dca = (DirContextAdapter) dirContext.lookup(userDn);

        HashMap<String, Object> user = displayAttributeHelper.getUserAttributeMapForUser(dca);

        return user;
    }

    @RPCAction(name = "group.list")
    public HashMap<String, Object> listGroups(OperationContext opContext) throws NamingException {

        String domain = (String) opContext.getParams().get("domain");
        Assert.notNull(domain, "userId can't be null in this request");

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

        NamingEnumeration<SearchResult> searchResults = dirContext.search(nameHelper.getGroupsBaseDn(domainDn), "(cn=*)", controls);

        List<HashMap<String, Object>> groups = new LinkedList<HashMap<String, Object>>();
        while (searchResults.hasMore()) {
            SearchResult sr = searchResults.next();
            if (nameHelper.isGroupDn(sr.getNameInNamespace().toLowerCase())) {
                HashMap<String, Object> group = new HashMap<String, Object>();
                group.put("groupId", nameHelper.newGroupDn(sr.getNameInNamespace().toLowerCase()).getAsGroupId());
                groups.add(group);
            }
        }

        HashMap<String, Object> response = new HashMap<String, Object>();
        response.put("groups", groups);

        return response;
    }

    @RPCAction(name = "group.get")
    public HashMap<String, Object> getGroup(OperationContext opContext) throws NamingException {

        String groupId = (String) opContext.getParams().get("groupId");
        Assert.notNull(groupId, "userId can't be null in this request");

        BasicUser basicUser = getBasicUser();
        DirContext dirContext = basicUser.getDirContext();

        GroupDn groupDn = nameHelper.newGroupDnFromId(groupId);

        DirContextAdapter dca = (DirContextAdapter) dirContext.lookup(groupDn);

        HashMap<String, Object> group = new HashMap<String, Object>();

        group.put("Name", dca.getStringAttribute("cn"));
        group.put("Surname", dca.getStringAttribute("sn"));

        return group;
    }

    @RPCAction(name = "attr.defs.get")
    public HashMap<String, Object> getAttributeDefinition(OperationContext opContext) throws NamingException {
        return displayAttributeHelper.getDisplayAttrDefinitionListForUser();
    }

    @RPCAction(name = "user.mod")
    public HashMap<String, Object> modifyUser(OperationContext opContext) throws NamingException {

        String userId = (String) opContext.getParams().get("userId");
        Assert.notNull(userId, "userId can't be null in this request");

        UserDn userDn = nameHelper.newUserDnFromId(userId);
        BasicUser basicUser = getBasicUser();
        AccessControl accessControl = new AccessControl(nameHelper);

        /*
         * Access Control Start
         */

        accessControl.validateAccess(basicUser, userDn.getDomain());

        /*
         * Access Control End
         */

        DirContext dirContext = basicUser.getDirContext();
        DirContextAdapter dca = (DirContextAdapter) dirContext.lookup(userDn);
        
        HashMap<String, Object> attrMod = displayAttributeHelper.apiToLdapAttrNames( (HashMap<String, Object>) opContext.getParams().get("mod"));
        
        for(Entry<String, Object> entry : attrMod.entrySet()) {
            
            if(entry.getValue() instanceof List) {
                dca.setAttributeValues(entry.getKey(), ((List<String>) entry.getValue()).toArray());
            } else if(entry.getValue() instanceof Object) {
                dca.setAttributeValue(entry.getKey(), entry.getValue());
            }
        }
        
        dirContext.modifyAttributes(userDn, dca.getModificationItems());
        
        HashMap<String, Object> response = new HashMap<String, Object>();
        response.put("success", true);
        
        return response;
    }
}
