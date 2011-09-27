/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.domain;

import javax.naming.NamingException;
import org.archone.ad.naming.NameHelper;
import org.archone.ad.naming.GroupDn;
import java.util.LinkedList;
import java.util.List;
import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.apache.shiro.SecurityUtils;
import org.archone.ad.schema.AdConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;

/**
 *
 * @author romansergey
 */
public class UserHelper{

    @Autowired
    private NameHelper nameHelper;
    @Autowired
    private AdConfiguration adConfiguration;
    
    public List<String> getAdminDomains() throws InvalidNameException, NamingException {

        List<String> adminDomains = new LinkedList<String>();
        
        List<String> groups = lookupMembershipGroups();

        for (String group : groups) {
            if (group != null && GroupDn.isAdminGroupId(group)) {
                adminDomains.add(nameHelper.newGroupDnFromId(group).getDomain());
            }
        }
        return adminDomains;
    }

    public boolean isAdminDomain(String domain) throws InvalidNameException {
        return SecurityUtils.getSubject().hasRole("administrator@groups." + domain);
    }
    
    public List<String> lookupMembershipGroups() throws javax.naming.NamingException {
        DirContextAdapter userDirContext = (DirContextAdapter) SecurityUtils.getSubject();
        return lookupMembershipGroups(userDirContext, userDirContext.getNameInNamespace());
    }

    public List<String> lookupMembershipGroups(DirContext dirContext, String userDn) throws javax.naming.NamingException {

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> searchResults = dirContext.search("", adConfiguration.getMembershipSearchFilter(), new String[]{userDn}, controls);

        List<String> roles = new LinkedList<String>();
        while (searchResults.hasMore()) {
            GroupDn groupDn = new GroupDn(searchResults.next().getNameInNamespace(), adConfiguration.getGroupsRdn());
            roles.add(groupDn.getAsGroupId());
        }

        return roles;
    }
}
