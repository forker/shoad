/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.security;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.naming.InvalidNameException;
import org.archone.ad.authentication.BasicUser;
import org.archone.ad.naming.DomainDn;
import org.archone.ad.naming.GroupDn;
import org.archone.ad.naming.NameHelper;
import org.archone.ad.naming.UserDn;
import org.archone.ad.rpc.OperationContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author romansergey
 */
public class AdAccessPolicy {

    @Autowired
    private NameHelper nameHelper;

    public List<String> getAdminDomains(BasicUser basicUser) throws InvalidNameException {

        List<String> adminDomains = new LinkedList<String>();

        List<String> groups = basicUser.getGroups();

        for (String group : groups) {
            if (group != null && GroupDn.isAdminGroupId(group)) {
                adminDomains.add(nameHelper.newGroupDnFromId(group).getDomain());
            }
        }

        return adminDomains;
    }

    @SecurityConstraint(name = "administrator.by_domain")
    public void validateEntityAccess(OperationContext opContext) throws InvalidNameException {
        
        HashMap<String, Object> params = opContext.getParams();
        BasicUser basicUser = opContext.getBasicUser();

        String domain = null;

        if (params.containsKey("domain")) {
            DomainDn domainDn = nameHelper.newDomainDnFromDomain((String) params.get("domain"));
            domain = domainDn.getDomain();
        }

        if (params.containsKey("groupId")) {
            GroupDn groupDn = nameHelper.newGroupDnFromId((String) params.get("groupId"));
            domain = groupDn.getDomain();
        }

        if (params.containsKey("userId")) {
            UserDn userDn = nameHelper.newUserDnFromId((String) params.get("userId"));
            domain = userDn.getDomain();
        }

        if (domain != null && !getAdminDomains(basicUser).contains(domain)) {
            throw new SecurityViolationException();
        }

        if (params.containsKey("groups")) {
            List<String> groups = (List<String>) params.get("groups");
            for (String group : groups) {
                GroupDn groupDn = nameHelper.newGroupDnFromId(group);
                if(!getAdminDomains(basicUser).contains(domain)) {
                    throw new SecurityViolationException(domain + " is forbidden");
                }
            }
        }
    }
}
