/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.domain;

import org.archone.ad.domain.entities.ShoadGroup;
import org.archone.ad.domain.entities.ShoadEntity;
import org.archone.ad.domain.entities.ShoadUser;
import org.archone.ad.dao.CommonDao;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.archone.ad.naming.NameConvertor;
import org.archone.ad.rpc.DataMap;
import org.archone.ad.rpc.OperationContext;
import org.archone.ad.rpc.Response;
import org.archone.ad.security.SecuredMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextAdapter;

/**
 *
 * @author romansergey
 */
public class LdapActions {

    @Autowired
    private NameHelper nameHelper;
    @Autowired
    private UserHelper userHelper;
    @Autowired
    private DisplayAttributeHelper displayAttributeHelper;
    @Autowired
    private AdConfiguration ldapConfiguration;
    @Autowired
    private ContextSource contextSource;

    @RPCAction(name = "user.login", required = {"username", "password"})
    @RequiresGuest
    public DataMap loginUser(OperationContext opContext) {

        String username = (String) opContext.getParams().get("username");
        String password = (String) opContext.getParams().get("password");

        UsernamePasswordToken upToken = new UsernamePasswordToken(username, password);
        Subject currentUser = SecurityUtils.getSubject();
        
        currentUser.login(upToken);

        return Response.success();
    }

    @RPCAction(name = "user.logout")
    @RequiresAuthentication
    public DataMap logoutUser(OperationContext opContext) {

        Subject currentUser = SecurityUtils.getSubject();
        currentUser.logout();

        return Response.success();
    }

    @RPCAction(name = "domain.list")
    public HashMap<String, Object> listDomains(OperationContext opContext) throws NamingException {

        DirContextAdapter userDirContext = (DirContextAdapter) SecurityUtils.getSubject().getPrincipal();

        List<String> domains =  new ArrayList();
        domains.add("example.com");

        HashMap<String, Object> response = new HashMap<String, Object>();
        response.put("domains", domains);
        response.put("success", true);

        return response;
    }


    /*
     * listUsers
     * required fields: domain
     */
    @RPCAction(name = "user.list", required = {"domain"})
    @SecuredMethod(constraints = "administrator.by_domain")
    public HashMap<String, Object> listUsers(OperationContext opContext) throws NamingException {

        String domain = (String) opContext.getParams().get("domain");

        List<String> groups = (List<String>) opContext.getParams().get("groups");
        List<String> retAttrs = (List<String>) opContext.getParams().get("attrs");
        
        DirContextAdapter userDirContext = (DirContextAdapter) SecurityUtils.getSubject().getPrincipal();
        CommonDao commonDao = new CommonDao(userDirContext, new NameConvertor());

        DomainDn domainDn = nameHelper.newDomainDnFromDomain(domain);

        HashMap<String, HashMap<String, Object>> users = new HashMap<String, HashMap<String, Object>>();

        List<ShoadUser> shoadUsers = null;

        if (groups == null || groups.isEmpty()) {
            shoadUsers = commonDao.listUsers(domain);
        } else {
            HashMap<String, ShoadUser> usersMap = new HashMap<String, ShoadUser>();
            for (String groupId : groups) {
                ShoadGroup shoadGroup = commonDao.lookupGroup(groupId);

                shoadUsers = commonDao.listMembers(shoadGroup);
            }
        }

        //TODO: review
        if (shoadUsers != null) {
            for (ShoadUser shoadUser : shoadUsers) {
                if (!users.containsKey(shoadUser.getEntityId())) {
                    HashMap<String, Object> user = new HashMap<String, Object>();
                    user.put("userId", shoadUser.getEntityId());

                    if (retAttrs != null) {
                        for (String attr : retAttrs) {
                            if (displayAttributeHelper.hasApiName(attr)) {
                                user.put(attr, shoadUser.getObjectAttribute(displayAttributeHelper.getLdapName(attr)));
                            }
                        }
                    }
                    users.put(shoadUser.getEntityId(), user);
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
    @RPCAction(name = "user.get", required = {"userId"})
    @SecuredMethod(constraints = "administrator.by_domain")
    public HashMap<String, Object> getUser(OperationContext opContext) throws NamingException {

        String userId = (String) opContext.getParams().get("userId");

        DirContextAdapter userDirContext = (DirContextAdapter) SecurityUtils.getSubject().getPrincipal();

        UserDn userDn = nameHelper.newUserDnFromId(userId);

        DirContextAdapter dca = (DirContextAdapter) userDirContext.lookup(userDn);

        HashMap<String, Object> user = displayAttributeHelper.getUserAttributeMapForUser(dca);

        return user;
    }

    @RPCAction(name = "group.list", required = {"domain"})
    @SecuredMethod(constraints = "administrator.by_domain")
    public HashMap<String, Object> listGroups(OperationContext opContext) throws NamingException {

        String domain = (String) opContext.getParams().get("domain");

        DirContextAdapter userDirContext = (DirContextAdapter) SecurityUtils.getSubject().getPrincipal();

        DomainDn domainDn = nameHelper.newDomainDnFromDomain(domain);

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> searchResults = userDirContext.search(nameHelper.getGroupsBaseDn(domainDn), "(cn=*)", controls);

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

    @RPCAction(name = "group.get", required = {"groupId"})
    @SecuredMethod(constraints = "administrator.by_domain")
    public HashMap<String, Object> getGroup(OperationContext opContext) throws NamingException {

        String groupId = (String) opContext.getParams().get("groupId");

        DirContextAdapter userDirContext = (DirContextAdapter) SecurityUtils.getSubject().getPrincipal();

        GroupDn groupDn = nameHelper.newGroupDnFromId(groupId);

        DirContextAdapter dca = (DirContextAdapter) userDirContext.lookup(groupDn);

        HashMap<String, Object> group = new HashMap<String, Object>();

        group.put("groupId", dca.getStringAttribute("cn"));
        group.put("description", dca.getStringAttribute("description"));

        return group;
    }

    @RPCAction(name = "attr.defs.get")
    @RequiresAuthentication
    public HashMap<String, DisplayAttribute> getAttributeDefinition(OperationContext opContext) throws NamingException {
        return displayAttributeHelper.getDisplayAttrDefinitionListForUser();
    }

    @RPCAction(name = "user.mod", required = {"userId"})
    @RequiresAuthentication
    @SecuredMethod(constraints = "administrator.by_domain")
    public HashMap<String, Object> modifyUser(OperationContext opContext) throws NamingException {

        String userId = (String) opContext.getParams().get("userId");

        UserDn userDn = nameHelper.newUserDnFromId(userId);
        DomainDn domainDn = nameHelper.newDomainDnFromDomain(userDn.getDomain());
        DirContextAdapter userDirContext = (DirContextAdapter) SecurityUtils.getSubject().getPrincipal();

        HashMap<String, Object> response = new HashMap<String, Object>();
        
        DirContextAdapter dca = (DirContextAdapter) userDirContext.lookup(userDn);

        HashMap<String, Object> modValues = displayAttributeHelper.apiToLdapAttrNames((HashMap<String, Object>) opContext.getParams().get("mod"));
        HashMap<String, Object> addValues = displayAttributeHelper.apiToLdapAttrNames((HashMap<String, Object>) opContext.getParams().get("add"));
        HashMap<String, Object> removeValues = displayAttributeHelper.apiToLdapAttrNames((HashMap<String, Object>) opContext.getParams().get("remove"));
        List<String> removeAttrs = (List<String>) opContext.getParams().get("removeAttr");

        IntegrityCheckUtil integrityCheckUtil = new IntegrityCheckUtil(userDirContext);
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
            userDirContext.modifyAttributes(userDn, removeAttrList.toArray(new ModificationItem[0]));
            userDirContext.modifyAttributes(userDn, dca.getModificationItems());
            response.put("success", true);
        }

        return response;
    }

    @RPCAction(name = "user.membership.get", required = {"userId"})
    @SecuredMethod(constraints = "administrator.by_domain")
    public HashMap<String, Object> listMermbershipGroups(OperationContext opContext) throws NamingException {

        String userId = (String) opContext.getParams().get("userId");

        UserDn userDn = nameHelper.newUserDnFromId(userId);
        DomainDn domainDn = nameHelper.newDomainDnFromDomain(userDn.getDomain());
        
        DirContextAdapter userDirContext = (DirContextAdapter) SecurityUtils.getSubject().getPrincipal();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> searchResults = userDirContext.search(nameHelper.getGroupsBaseDn(nameHelper.newDomainDnFromDomain(userDn.getDomain())), "(uniqueMember=" + userDn.toString() + ")", controls);

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

    @RPCAction(name = "user.membership.mod", required = {"userId"})
    @SecuredMethod(constraints = "administrator.by_domain")
    public HashMap<String, Object> modifyMermbershipGroups(OperationContext opContext) throws NamingException {

        String userId = (String) opContext.getParams().get("userId");

        DirContextAdapter userDirContext = (DirContextAdapter) SecurityUtils.getSubject().getPrincipal();
        CommonDao commonDao = new CommonDao(userDirContext, new NameConvertor());

        ShoadUser shoadUser = commonDao.lookupUser(userId);

        List<String> addGroups = (List<String>) opContext.getParams().get("add");
        for (String group : addGroups) {
            ShoadGroup shoadGroup = commonDao.lookupGroup(group);
            shoadGroup.addMember(shoadUser);
            commonDao.save((ShoadEntity) shoadGroup);
        }


        List<String> removeGroups = (List<String>) opContext.getParams().get("remove");
        for (String group : removeGroups) {
            ShoadGroup shoadGroup = commonDao.lookupGroup(group);
            shoadGroup.removeMember(shoadUser);
            commonDao.save((ShoadEntity) shoadGroup);
        }

        HashMap<String, Object> response = new HashMap<String, Object>();
        response.put("success", true);


        return response;
    }

    @RPCAction(name = "user.add", required = {"userId"})
    @SecuredMethod(constraints = "administrator.by_domain")
    public HashMap<String, Object> addUser(OperationContext opContext) throws NamingException {
        String userId = (String) opContext.getParams().get("userId");

        UserDn userDn = nameHelper.newUserDnFromId(userId);
        DomainDn domainDn = nameHelper.newDomainDnFromDomain(userDn.getDomain());

        DirContextAdapter userDirContext = (DirContextAdapter) SecurityUtils.getSubject().getPrincipal();
        
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

        userDirContext.bind(userDn, null, attrs);

        HashMap<String, Object> response = new HashMap<String, Object>();

        response.put("success", true);

        return response;
    }

    @RPCAction(name = "group.add", required = {"groupId"})
    @SecuredMethod(constraints = "administrator.by_domain")
    public HashMap<String, Object> addGroup(OperationContext opContext) throws NamingException {
        String groupId = (String) opContext.getParams().get("groupId");

        GroupDn groupDn = nameHelper.newGroupDnFromId(groupId);
        DomainDn domainDn = nameHelper.newDomainDnFromDomain(groupDn.getDomain());
        
        DirContextAdapter userDirContext = (DirContextAdapter) SecurityUtils.getSubject().getPrincipal();

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


        userDirContext.bind(groupDn, null, attrs);

        HashMap<String, Object> response = new HashMap<String, Object>();

        response.put("success", true);

        return response;
    }

    @RPCAction(name = "group.mod", required = {"groupId", "description"})
    @SecuredMethod(constraints = "administrator.by_domain")
    public HashMap<String, Object> modifyGroup(OperationContext opContext) throws NamingException {
        String groupId = (String) opContext.getParams().get("groupId");
        String description = (String) opContext.getParams().get("description");

        GroupDn groupDn = nameHelper.newGroupDnFromId(groupId);
        DomainDn domainDn = nameHelper.newDomainDnFromDomain(groupDn.getDomain());
        
        DirContextAdapter userDirContext = (DirContextAdapter) SecurityUtils.getSubject().getPrincipal();

        HashMap<String, Object> response = new HashMap<String, Object>();

        DirContextAdapter dca = (DirContextAdapter) userDirContext.lookup(groupDn);

        dca.setAttributeValue("description", description);

        userDirContext.modifyAttributes(groupDn, dca.getModificationItems());

        response.put("success", true);

        return response;
    }

    @RPCAction(name = "group.remove", required = {"groupId"})
    @SecuredMethod(constraints = "administrator.by_domain")
    public HashMap<String, Object> removeGroup(OperationContext opContext) throws NamingException {
        String groupId = (String) opContext.getParams().get("groupId");

        GroupDn groupDn = nameHelper.newGroupDnFromId(groupId);
        DomainDn domainDn = nameHelper.newDomainDnFromDomain(groupDn.getDomain());

        DirContextAdapter userDirContext = (DirContextAdapter) SecurityUtils.getSubject().getPrincipal();
        userDirContext.unbind(groupDn);

        HashMap<String, Object> response = new HashMap<String, Object>();
        response.put("success", true);

        return response;
    }

    @RPCAction(name = "user.remove", required = {"userId"})
    @SecuredMethod(constraints = "administrator.by_domain")
    public HashMap<String, Object> removeUser(OperationContext opContext) throws NamingException {

        String userId = (String) opContext.getParams().get("userId");

        DirContextAdapter userDirContext = (DirContextAdapter) SecurityUtils.getSubject().getPrincipal();

        UserDn userDn = nameHelper.newUserDnFromId(userId);

        DomainDn domainDn = nameHelper.newDomainDnFromDomain(userDn.getDomain());

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> searchResults = userDirContext.search(nameHelper.getGroupsBaseDn(nameHelper.newDomainDnFromDomain(userDn.getDomain())), "(uniqueMember=" + userDn.toString() + ")", controls);

        while (searchResults.hasMore()) {
            SearchResult sr = searchResults.next();
            DirContextAdapter dca = (DirContextAdapter) userDirContext.lookup(sr.getNameInNamespace());
            dca.removeAttributeValue("uniqueMember", userDn.toString());
            userDirContext.modifyAttributes(sr.getNameInNamespace(), dca.getModificationItems());
        }

        userDirContext.unbind(userDn);

        HashMap<String, Object> response = new HashMap<String, Object>();
        response.put("success", true);

        return response;
    }

    @RPCAction(name = "user.password.change", role = {"ROLE_ADMINISTRATOR", "ROLE_USER"}, required = {"userId", "newPassword", "oldPassword:ROLE_USER"})
    @SecuredMethod(constraints = "administrator.by_domain")
    public HashMap<String, Object> changeUserPassword(OperationContext opContext) throws NamingException, NoSuchAlgorithmException, UnsupportedEncodingException {

        String userId = (String) opContext.getParams().get("userId");
        String newPassword = (String) opContext.getParams().get("newPassword");
        
        DirContextAdapter userDirContext = (DirContextAdapter) SecurityUtils.getSubject().getPrincipal();

        UserDn userDn = nameHelper.newUserDnFromId(userId);

        DomainDn domainDn = nameHelper.newDomainDnFromDomain(userDn.getDomain());

        DirContextAdapter ldapUser = (DirContextAdapter) userDirContext.lookup(userDn);
        ldapUser.setAttributeValue("userPassword", newPassword);

        HashMap<String, Object> response = new HashMap<String, Object>();
        response.put("success", true);

        return response;
    }

    @RPCAction(name = "domain.create", required = {"domain", "userName", "password", "rootDn", "rootDnPassword"})
    @RequiresRoles("SUPERUSER")
    public HashMap<String, Object> createDomain(OperationContext opContext) throws NamingException, NoSuchAlgorithmException, UnsupportedEncodingException {

        String domain = (String) opContext.getParams().get("domain");
        String userName = (String) opContext.getParams().get("userName");
        String password = (String) opContext.getParams().get("password");
        String rootDn = (String) opContext.getParams().get("rootDn");
        String rootDnPassword = (String) opContext.getParams().get("rootDnPassword");

        DirContext dirContext = contextSource.getContext(rootDn, rootDnPassword);

        DirContextAdapter userRoot = (DirContextAdapter) dirContext.lookup("ds-cfg-backend-id=userRoot,cn=Backends,cn=config");

        DomainDn domainDn = nameHelper.newDomainDnFromDomain(domain);

        //Declaring backend
        userRoot.addAttributeValue("ds-cfg-base-dn", domainDn.toString());
        dirContext.modifyAttributes(userRoot.getNameInNamespace(), userRoot.getModificationItems());

        //Creating backend
        DirContextAdapter adapter = new DirContextAdapter();
        adapter.setAttributeValues("objectclass",
                new String[]{"top", "domain"});
        adapter.setAttributeValue("dc", domain.split("\\.")[0]);
        dirContext.bind(domainDn.toString(), adapter, null);

        //Creating group node
        DirContextAdapter groupNode = new DirContextAdapter();
        groupNode.setAttributeValue("objectclass", "organizationalUnit");
        dirContext.bind(domainDn.getGroupNodeDn(), groupNode);

        //creating user node
        DirContextAdapter userNode = new DirContextAdapter();
        userNode.setAttributeValue("objectclass", "organizationalUnit");
        dirContext.bind(domainDn.getUserNodeDn(), userNode);

        //creating user
        DirContextAdapter userAccount = new DirContextAdapter();
        userAccount.setAttributeValues("objectclass", ldapConfiguration.getUserObjectClassList().toArray());
        userAccount.setAttributeValue("sn", userName);
        userAccount.setAttributeValue("cn", userName);
        UserDn userDn = nameHelper.newUserDn(userName, domainDn);
        userAccount.setAttributeValue("userPassword", password);
        dirContext.bind(userDn, userAccount);

        //creating admin group
        DirContextAdapter adminGroup = new DirContextAdapter();
        adminGroup.setAttributeValues("objectclass", ldapConfiguration.getGroupObjectClassList().toArray());
        adminGroup.setAttributeValue("uniqueMember", userDn.toString());
        GroupDn groupDn = nameHelper.newGroupDn("administrator", domainDn);
        dirContext.bind(groupDn, adminGroup);

        HashMap<String, Object> response = new HashMap<String, Object>();
        response.put("success", true);

        return response;
    }
}