/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.ad.naming;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InvalidNameException;

/**
 *
 * @author forker
 */
public class NameHelperImpl implements NameHelper {

    private String usersRdn;
    private String groupsRdn;

    public NameHelperImpl(String usersRdn, String groupsRdn) {
        this.usersRdn = usersRdn;
        this.groupsRdn = groupsRdn;
    }

    @Override
    public DomainDn newDomainDn(String dn) throws InvalidNameException {
        return new DomainDn(dn);
    }

    @Override
    public UserDn newUserDn(String dn) throws InvalidNameException {
        return new UserDn(dn, this.usersRdn);
    }

    @Override
    public GroupDn newGroupDn(String dn) throws InvalidNameException {
        return new GroupDn(dn, this.groupsRdn);
    }

    @Override
    public DomainDn newDomainDnFromDomain(String id) throws InvalidNameException {
        return DomainDn.fromDomain(id);
    }

    @Override
    public UserDn newUserDnFromId(String id) throws InvalidNameException {
        return UserDn.fromUserId(id, usersRdn);
    }

    @Override
    public GroupDn newGroupDnFromId(String id) throws InvalidNameException {
        return GroupDn.fromGroupId(id, groupsRdn);
    }

    @Override
    public String getUsersBaseDn(DomainDn domainDn) {
        return usersRdn + "," + domainDn.toString();
    }

    @Override
    public String getGroupsBaseDn(DomainDn domainDn) {
        return groupsRdn + "," + domainDn.toString();
    }

    @Override
    public String getUsersBaseDn(String domain) throws InvalidNameException {
        return usersRdn + "," + DomainDn.fromDomain(domain).toString();
    }

    @Override
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
    
}
