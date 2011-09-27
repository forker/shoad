/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.security;

import org.archone.ad.rpc.SecurityViolationException;
import java.util.HashMap;
import java.util.List;
import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import org.archone.ad.domain.UserHelper;
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
    
    @Autowired
    private UserHelper userHelper;

    @SecurityConstraint(name = "administrator.by_domain")
    public void validateEntityAccess(OperationContext opContext) throws InvalidNameException, NamingException {
        
        HashMap<String, Object> params = opContext.getParams();
        
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

        if (domain != null && !userHelper.getAdminDomains().contains(domain)) {
            throw new SecurityViolationException();
        }

        if (params.containsKey("groups")) {
            List<String> groups = (List<String>) params.get("groups");
            for (String group : groups) {
                GroupDn groupDn = nameHelper.newGroupDnFromId(group);
                if(!userHelper.isAdminDomain(domain)) {
                    throw new SecurityViolationException(domain + " is forbidden");
                }
            }
        }
    }
}
