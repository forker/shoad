/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.model;

import org.archone.ad.naming.NameHelper;
import org.archone.ad.naming.GroupDn;
import java.util.LinkedList;
import java.util.List;
import javax.naming.InvalidNameException;
import org.archone.ad.authentication.BasicUser;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author romansergey
 */
public class UserHelperImpl implements UserHelper {
    
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
}
