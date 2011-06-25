/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.vhd.model;

import java.util.LinkedList;
import java.util.List;
import javax.naming.InvalidNameException;
import org.archone.vhd.authentication.BasicUser;

/**
 *
 * @author forker
 */
public class AccessControl {
    
    private NameHelper dnHelper;

    public AccessControl(NameHelper dnHelper) {
        this.dnHelper = dnHelper;
    }
   
    
    public List<String> getAdminDomains(BasicUser basicUser) throws InvalidNameException {
        
        List<String> adminDomains = new LinkedList<String>();
        
        List<String> groups = basicUser.getGroups();
        
        for (String group : groups) {
            if (GroupDn.isAdminGroupId(group)) {
                adminDomains.add(dnHelper.newGroupDnFromId(group).getDomain());
            }
        }
        
        return adminDomains;
    }
    
    public void validateAccess(BasicUser basicUser, String domain) throws InvalidNameException {
        
        if(!getAdminDomains(basicUser).contains(domain)) {
            throw new IllegalAccessException();
        }
        
    }
}
