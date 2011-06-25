/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.vhd.model;

import javax.naming.InvalidNameException;

/**
 *
 * @author forker
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
