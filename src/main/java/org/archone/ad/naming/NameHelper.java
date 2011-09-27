/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.naming;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

/**
 *
 * @author romansergey
 */
public class NameHelper {

    private String usersRdn;
    private String groupsRdn;

    public NameHelper(String usersRdn, String groupsRdn) {
        this.usersRdn = usersRdn;
        this.groupsRdn = groupsRdn;
    }


    public DomainDn newDomainDn(String dn) throws InvalidNameException {
        return new DomainDn(dn);
    }


    public UserDn newUserDn(String dn) throws InvalidNameException {
        return new UserDn(dn, this.usersRdn);
    }


    public GroupDn newGroupDn(String dn) throws InvalidNameException {
        return new GroupDn(dn, this.groupsRdn);
    }


    public DomainDn newDomainDnFromDomain(String id) throws InvalidNameException {
        return DomainDn.fromDomain(id);
    }


    public UserDn newUserDnFromId(String id) throws InvalidNameException {
        return UserDn.fromUserId(id, usersRdn);
    }


    public GroupDn newGroupDnFromId(String id) throws InvalidNameException {
        return GroupDn.fromGroupId(id, groupsRdn);
    }


    public String getUsersBaseDn(DomainDn domainDn) {
        return usersRdn + "," + domainDn.toString();
    }


    public String getGroupsBaseDn(DomainDn domainDn) {
        return groupsRdn + "," + domainDn.toString();
    }


    public String getUsersBaseDn(String domain) throws InvalidNameException {
        return usersRdn + "," + DomainDn.fromDomain(domain).toString();
    }


    public String getGroupsBaseDn(String domain) throws InvalidNameException {
        return groupsRdn + "," + DomainDn.fromDomain(domain).toString();
    }


    public boolean isUserDn(String userDn) {
        try {
            new UserDn(userDn, usersRdn);
            return true;
        } catch (InvalidNameException ex) {
            return false;
        }
    }


    public boolean isGroupDn(String groupDn) {
        try {
            new GroupDn(groupDn, groupsRdn);
            return true;
        } catch (InvalidNameException ex) {
            return false;
        }
    }


    public UserDn newUserDn(String username, DomainDn domainDn) throws InvalidNameException {
        LdapName name = domainDn.getUserNodeDn();
        name.add("uid=" + username);
        return new UserDn(name.toString(), this.usersRdn);
    }


    public GroupDn newGroupDn(String groupname, DomainDn domainDn) throws InvalidNameException {
        LdapName name = domainDn.getGroupNodeDn();
        name.add("cn=" + groupname);
        return new GroupDn(name.toString(), this.groupsRdn);
    }
}
