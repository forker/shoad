/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.dao;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import org.archone.ad.naming.GroupDn;
import org.archone.ad.naming.UserDn;
import org.springframework.ldap.core.DirContextAdapter;

/**
 *
 * @author forker
 */
public class GroupDao {
    
    private DirContext dirContext;
    private DirContextAdapter group;

    public GroupDao(DirContext dirContext, GroupDn groupDn) throws NamingException {
        this.dirContext = dirContext;       
        this.group = (DirContextAdapter) dirContext.lookup(groupDn);
    }
    
    public void removeMember(UserDn userDn) {
        if(group.getAttributeSortedStringSet("uniqueMember").contains(userDn.toString())) {
            group.removeAttributeValue("uniqueMember", userDn.toString());
        }
    }
    
    public void addMember(UserDn userDn) {
        group.addAttributeValue("uniqueMember", userDn.toString());
    }
    
    public void listMemberNames(UserDn userDn) {
        group.addAttributeValue("uniqueMember", userDn.toString());
    }
    
    
    
    public void save() throws NamingException {
        dirContext.modifyAttributes(group.getNameInNamespace(), group.getModificationItems());
        group.update();
    }
    
}
