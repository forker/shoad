/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.naming;

import javax.naming.InvalidNameException;

/**
 *
 * @author romansergey
 */
public interface NameHelper {
    public DomainDn newDomainDn(String dn) throws InvalidNameException;
    public UserDn newUserDn(String dn) throws InvalidNameException;
    public GroupDn newGroupDn(String dn) throws InvalidNameException;
    public DomainDn newDomainDnFromDomain(String id) throws InvalidNameException;
    public UserDn newUserDnFromId(String id) throws InvalidNameException;
    public GroupDn newGroupDnFromId(String id) throws InvalidNameException;
    public String getUsersBaseDn(DomainDn domainDn);
    public String getGroupsBaseDn(DomainDn domainDn);
    public String getUsersBaseDn(String domainDn) throws InvalidNameException;
    public String getGroupsBaseDn(String domainDn) throws InvalidNameException;
    public boolean isUserDn(String userDn);
    public boolean isGroupDn(String userDn);
}
