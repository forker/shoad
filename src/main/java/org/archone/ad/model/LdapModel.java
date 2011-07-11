/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.model;

import org.archone.ad.schema.AdConfiguration;
import org.archone.ad.rpc.RPCAction;
import org.archone.ad.schema.DisplayAttributeHelper;
import org.archone.ad.schema.DisplayAttribute;
import org.archone.ad.integrity.IntegrityCheckUtil;
import org.archone.ad.naming.DomainDn;
import org.archone.ad.naming.NameHelper;
import org.archone.ad.naming.UserDn;
import org.archone.ad.naming.GroupDn;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.archone.ad.authentication.BasicUser;
import org.archone.ad.rpc.OperationContext;
import org.archone.ad.security.SecuredMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;

/**
 *
 * @author romansergey
 */
public class LdapModel {

    @Autowired
    private NameHelper nameHelper;
    @Autowired
    private UserHelper userHelper;
    @Autowired
    private DisplayAttributeHelper displayAttributeHelper;
    @Autowired
    private AdConfiguration ldapConfiguration;
     
    @RPCAction(name = "domain.list", role="ROLE_ADMINISTRATOR")
    public HashMap<String, Object> listDomains(OperationContext opContext) throws NamingException {
       
        BasicUser basicUser = opContext.getBasicUser();
        
        List<String> domains = userHelper.getAdminDomains(basicUser);
        
        HashMap<String, Object> response = new HashMap<String, Object>();
        response.put("domains", domains);
        response.put("success", true);

        return response;
    }
    

    /*
     * listUsers
     * required fields: domain
     */
    @RPCAction(name = "user.list", role="ROLE_ADMINISTRATOR", required={"domain"})
    @SecuredMethod(constraints="administrator.by_domain")
    public HashMap<String, Object> listUsers(OperationContext opContext) throws NamingException {

        String domain = (String) opContext.getParams().get("domain");

        List<String> groups = (List<String>) opContext.getParams().get("groups");

        BasicUser basicUser = opContext.getBasicUser();
        DirContext dirContext = basicUser.getDirContext();
        
        DomainDn domainDn = nameHelper.newDomainDnFromDomain(domain);

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

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

                if (userDnList != null) {
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
        }

        HashMap<String, Object> response = new HashMap<String, Object>();

        response.put("users", users.values());

        return response;
    }

    /*
     * getUser
     * required fields: userId
     */
    @RPCAction(name = "user.get", role="ROLE_ADMINISTRATOR", required={"userId"})
    @SecuredMethod(constraints="administrator.by_domain")
    public HashMap<String, Object> getUser(OperationContext opContext) throws NamingException {

        String userId = (String) opContext.getParams().get("userId");

        BasicUser basicUser = opContext.getBasicUser();
        DirContext dirContext = basicUser.getDirContext();

        UserDn userDn = nameHelper.newUserDnFromId(userId);

        DirContextAdapter dca = (DirContextAdapter) dirContext.lookup(userDn);

        HashMap<String, Object> user = displayAttributeHelper.getUserAttributeMapForUser(dca);

        return user;
    }

    @RPCAction(name = "group.list", role="ROLE_ADMINISTRATOR", required={"domain"})
    @SecuredMethod(constraints="administrator.by_domain")
    public HashMap<String, Object> listGroups(OperationContext opContext) throws NamingException {

        String domain = (String) opContext.getParams().get("domain");
        
        BasicUser basicUser = opContext.getBasicUser();

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

    @RPCAction(name = "group.get", role="ROLE_ADMINISTRATOR", required={"groupId"})
    @SecuredMethod(constraints="administrator.by_domain")
    public HashMap<String, Object> getGroup(OperationContext opContext) throws NamingException {

        String groupId = (String) opContext.getParams().get("groupId");

        BasicUser basicUser = opContext.getBasicUser();
        DirContext dirContext = basicUser.getDirContext();

        GroupDn groupDn = nameHelper.newGroupDnFromId(groupId);

        DirContextAdapter dca = (DirContextAdapter) dirContext.lookup(groupDn);

        HashMap<String, Object> group = new HashMap<String, Object>();

        group.put("groupId", dca.getStringAttribute("cn"));
        group.put("description", dca.getStringAttribute("description"));

        return group;
    }

    @RPCAction(name = "attr.defs.get", role="ROLE_ADMINISTRATOR")
    public HashMap<String, DisplayAttribute> getAttributeDefinition(OperationContext opContext) throws NamingException {
        return displayAttributeHelper.getDisplayAttrDefinitionListForUser();
    }

    @RPCAction(name = "user.mod", role="ROLE_ADMINISTRATOR", required={"userId"})
    @SecuredMethod(constraints="administrator.by_domain")
    public HashMap<String, Object> modifyUser(OperationContext opContext) throws NamingException {

        String userId = (String) opContext.getParams().get("userId");

        UserDn userDn = nameHelper.newUserDnFromId(userId);
        DomainDn domainDn = nameHelper.newDomainDnFromDomain(userDn.getDomain());
        BasicUser basicUser = opContext.getBasicUser();

        HashMap<String, Object> response = new HashMap<String, Object>();

        DirContext dirContext = basicUser.getDirContext();
        DirContextAdapter dca = (DirContextAdapter) dirContext.lookup(userDn);

        HashMap<String, Object> modValues = displayAttributeHelper.apiToLdapAttrNames((HashMap<String, Object>) opContext.getParams().get("mod"));
        HashMap<String, Object> addValues = displayAttributeHelper.apiToLdapAttrNames((HashMap<String, Object>) opContext.getParams().get("add"));
        HashMap<String, Object> removeValues = displayAttributeHelper.apiToLdapAttrNames((HashMap<String, Object>) opContext.getParams().get("remove"));
        List<String> removeAttrs = (List<String>) opContext.getParams().get("removeAttr");

        IntegrityCheckUtil integrityCheckUtil = new IntegrityCheckUtil(dirContext);
        HashMap<String, String> busyValues = new HashMap<String, String>();

        LinkedList<ModificationItem> removeAttrList = new LinkedList<ModificationItem>();
        if (removeAttrs != null) {
            for (String apiName : removeAttrs) {
                BasicAttribute attr = new BasicAttribute(displayAttributeHelper.getLdapName(apiName));
                ModificationItem mi = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attr);
                removeAttrList.add(mi);
            }
        }

        for (Entry<String, Object> entry : modValues.entrySet()) {

            if (displayAttributeHelper.byLdapName(entry.getKey()).isDomainUnique()) {
                if (!integrityCheckUtil.isUnique(domainDn, entry.getKey(), entry.getValue().toString())) {
                    busyValues.put(entry.getKey(), entry.getValue().toString());
                }
            }

            dca.setAttributeValue(entry.getKey(), entry.getValue().toString());
        }

        for (Entry<String, Object> entry : removeValues.entrySet()) {
            if (entry.getValue() instanceof List) {
                for (Object value : (List) entry.getValue()) {
                    dca.removeAttributeValue(entry.getKey(), value);
                }
            } else {
                dca.removeAttributeValue(entry.getKey(), entry.getValue());
            }
        }

        for (Entry<String, Object> entry : addValues.entrySet()) {
            if (entry.getValue() instanceof List) {

                for (Object value : (List) entry.getValue()) {

                    if (displayAttributeHelper.byLdapName(entry.getKey()).isDomainUnique()) {
                        if (!integrityCheckUtil.isUnique(domainDn, entry.getKey(), (String) value)) {
                            busyValues.put(entry.getKey(), (String) value);
                        }
                    }

                    dca.addAttributeValue(entry.getKey(), value);
                }
            } else {

                if (displayAttributeHelper.byLdapName(entry.getKey()).isDomainUnique()) {
                    if (!integrityCheckUtil.isUnique(domainDn, entry.getKey(), (String) entry.getValue())) {
                        busyValues.put(entry.getKey(), (String) entry.getValue());
                    }
                }

                dca.addAttributeValue(entry.getKey(), entry.getValue());
            }
        }

        if (busyValues.size() > 0) {
            response.put("success", false);
            response.put("busyValues", busyValues);
        } else {
            dirContext.modifyAttributes(userDn, removeAttrList.toArray(new ModificationItem[0]));
            dirContext.modifyAttributes(userDn, dca.getModificationItems());
            response.put("success", true);
        }

        return response;
    }

    @RPCAction(name = "user.membership.get", role="ROLE_ADMINISTRATOR", required={"userId"})
    @SecuredMethod(constraints="administrator.by_domain")
    public HashMap<String, Object> listMermbershipGroups(OperationContext opContext) throws NamingException {

        String userId = (String) opContext.getParams().get("userId");

        UserDn userDn = nameHelper.newUserDnFromId(userId);

        BasicUser basicUser = opContext.getBasicUser();

        DomainDn domainDn = nameHelper.newDomainDnFromDomain(userDn.getDomain());

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        DirContext dirContext = basicUser.getDirContext();

        NamingEnumeration<SearchResult> searchResults = dirContext.search(nameHelper.getGroupsBaseDn(nameHelper.newDomainDnFromDomain(userDn.getDomain())), "(uniqueMember=" + userDn.toString() + ")", controls);

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

    @RPCAction(name = "user.membership.mod", role="ROLE_ADMINISTRATOR", required={"userId"})
    @SecuredMethod(constraints="administrator.by_domain")
    public HashMap<String, Object> modifyMermbershipGroups(OperationContext opContext) throws NamingException {

        String userId = (String) opContext.getParams().get("userId");

        UserDn userDn = nameHelper.newUserDnFromId(userId);

        BasicUser basicUser = opContext.getBasicUser();

        DomainDn domainDn = nameHelper.newDomainDnFromDomain(userDn.getDomain());

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        DirContext dirContext = basicUser.getDirContext();

        List<String> addGroups = (List<String>) opContext.getParams().get("add");
        for (String group : addGroups) {
            GroupDn groupDn = nameHelper.newGroupDnFromId(group);
            DirContextAdapter groupDca = (DirContextAdapter) dirContext.lookup(groupDn);
            groupDca.addAttributeValue("uniqueMember", userDn.toString());
            dirContext.modifyAttributes(groupDn, groupDca.getModificationItems());
        }


        List<String> removeGroups = (List<String>) opContext.getParams().get("remove");
        for (String group : removeGroups) {
            GroupDn groupDn = nameHelper.newGroupDnFromId(group);
            DirContextAdapter groupDca = (DirContextAdapter) dirContext.lookup(groupDn);
            groupDca.removeAttributeValue("uniqueMember", userDn.toString());
            dirContext.modifyAttributes(groupDn, groupDca.getModificationItems());
        }

        HashMap<String, Object> response = new HashMap<String, Object>();
        response.put("success", true);


        return response;
    }

    @RPCAction(name = "user.add", role="ROLE_ADMINISTRATOR", required={"userId"})
    @SecuredMethod(constraints="administrator.by_domain")
    public HashMap<String, Object> addUser(OperationContext opContext) throws NamingException {
        String userId = (String) opContext.getParams().get("userId");

        UserDn userDn = nameHelper.newUserDnFromId(userId);

        BasicUser basicUser = opContext.getBasicUser();

        DomainDn domainDn = nameHelper.newDomainDnFromDomain(userDn.getDomain());
        DirContext dirContext = basicUser.getDirContext();

        Attributes attrs = new BasicAttributes();
        BasicAttribute ocattr = new BasicAttribute("objectclass");
        for (String objectClassName : ldapConfiguration.getUserObjectClassList()) {
            ocattr.add(objectClassName);
        }
        attrs.put(ocattr);

        for (DisplayAttribute displayAttribute : displayAttributeHelper.getApiNameIndexedAttrDef().values()) {
            Object attrValue = opContext.getParams().get(displayAttribute.getApiName());

            if (attrValue != null) {
                BasicAttribute attr = new BasicAttribute(displayAttribute.getLdapName());

                if (attrValue instanceof List) {
                    for (Object attrOneValue : (List) attrValue) {
                        attr.add(attrOneValue);
                    }
                } else {
                    attr.add(attrValue);
                }

                attrs.put(attr);

            } else if (displayAttribute.isMustHave()) {
                throw new RuntimeException(displayAttribute.getApiName() + " is required!");
            }

        }

        dirContext.bind(userDn, null, attrs);

        HashMap<String, Object> response = new HashMap<String, Object>();

        response.put("success", true);

        return response;
    }

    @RPCAction(name = "group.add", role="ROLE_ADMINISTRATOR", required={"groupId"})
    @SecuredMethod(constraints="administrator.by_domain")
    public HashMap<String, Object> addGroup(OperationContext opContext) throws NamingException {
        String groupId = (String) opContext.getParams().get("groupId");

        GroupDn groupDn = nameHelper.newGroupDnFromId(groupId);

        BasicUser basicUser = opContext.getBasicUser();

        DomainDn domainDn = nameHelper.newDomainDnFromDomain(groupDn.getDomain());
        DirContext dirContext = basicUser.getDirContext();

        Attributes attrs = new BasicAttributes();
        BasicAttribute ocattr = new BasicAttribute("objectclass");
        for (String objectClassName : ldapConfiguration.getGroupObjectClassList()) {
            ocattr.add(objectClassName);
        }
        attrs.put(ocattr);

        String description = (String) opContext.getParams().get("description");
        if (description != null && !description.isEmpty()) {
            BasicAttribute descattr = new BasicAttribute("description");
            descattr.add(description);
            attrs.put(descattr);
        }


        dirContext.bind(groupDn, null, attrs);

        HashMap<String, Object> response = new HashMap<String, Object>();

        response.put("success", true);

        return response;
    }

    @RPCAction(name = "group.mod", role="ROLE_ADMINISTRATOR", required={"groupId", "description"})
    @SecuredMethod(constraints="administrator.by_domain")
    public HashMap<String, Object> modifyGroup(OperationContext opContext) throws NamingException {
        String groupId = (String) opContext.getParams().get("groupId");
        String description = (String) opContext.getParams().get("description");

        GroupDn groupDn = nameHelper.newGroupDnFromId(groupId);

        BasicUser basicUser = opContext.getBasicUser();

        DomainDn domainDn = nameHelper.newDomainDnFromDomain(groupDn.getDomain());
        DirContext dirContext = basicUser.getDirContext();

        HashMap<String, Object> response = new HashMap<String, Object>();

        DirContextAdapter dca = (DirContextAdapter) dirContext.lookup(groupDn);

        dca.setAttributeValue("description", description);

        dirContext.modifyAttributes(groupDn, dca.getModificationItems());

        response.put("success", true);

        return response;
    }

    @RPCAction(name = "group.remove", role="ROLE_ADMINISTRATOR", required={"groupId"})
    @SecuredMethod(constraints="administrator.by_domain")
    public HashMap<String, Object> removeGroup(OperationContext opContext) throws NamingException {
        String groupId = (String) opContext.getParams().get("groupId");

        GroupDn groupDn = nameHelper.newGroupDnFromId(groupId);

        BasicUser basicUser = opContext.getBasicUser();

        DomainDn domainDn = nameHelper.newDomainDnFromDomain(groupDn.getDomain());
        DirContext dirContext = basicUser.getDirContext();

        dirContext.unbind(groupDn);

        HashMap<String, Object> response = new HashMap<String, Object>();
        response.put("success", true);

        return response;
    }

    @RPCAction(name = "user.remove", role="ROLE_ADMINISTRATOR", required={"userId"})
    @SecuredMethod(constraints="administrator.by_domain")
    public HashMap<String, Object> removeUser(OperationContext opContext) throws NamingException {
        
        String userId = (String) opContext.getParams().get("userId");

        BasicUser basicUser = opContext.getBasicUser();
        DirContext dirContext = basicUser.getDirContext();

        UserDn userDn = nameHelper.newUserDnFromId(userId);
        
        DomainDn domainDn = nameHelper.newDomainDnFromDomain(userDn.getDomain());

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> searchResults = dirContext.search(nameHelper.getGroupsBaseDn(nameHelper.newDomainDnFromDomain(userDn.getDomain())), "(uniqueMember=" + userDn.toString() + ")", controls);

        while (searchResults.hasMore()) {
            SearchResult sr = searchResults.next();
            DirContextAdapter dca = (DirContextAdapter) dirContext.lookup(sr.getNameInNamespace());
            dca.removeAttributeValue("uniqueMember", userDn.toString());
            dirContext.modifyAttributes(sr.getNameInNamespace(), dca.getModificationItems());
        }

        dirContext.unbind(userDn);
        
        HashMap<String, Object> response = new HashMap<String, Object>();
        response.put("success", true);

        return response;
    }
}