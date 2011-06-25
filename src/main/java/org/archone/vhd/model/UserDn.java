/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.vhd.model;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

/**
 *
 * @author forker
 */
public class UserDn extends LdapName {
    
    private String usersRdn;
    private int domainRdnIndex;
    
    public UserDn(String dn, String usersRdn) throws InvalidNameException {
        
        super(dn);
        this.usersRdn = usersRdn;
        this.domainRdnIndex = new LdapName(usersRdn).size() + 1;
        
        if(!dn.matches("uid=[a-z\\.\\-]{1,}," + usersRdn + "(,dc=[a-z0-9\\-]{1,}){1,}")) {
            throw new InvalidNameException();
        }
    }
    
    public static UserDn fromUserId(String userId, String usersRdn) throws InvalidNameException {
        if(userId.matches("[a-z\\.\\-]{1,}@users(\\.[a-z\\-]{1,}){1,}")) {
            return new UserDn ( "uid=" + userId.split("@")[0] + "," + usersRdn + "," + DomainDn.parseDomainDn( userId.replaceAll("[a-z\\.\\-]{1,}@users\\.", "" ) ), usersRdn);
        } else {
            throw new InvalidNameException();
        }
    }
    

    public String getDomain() {
        StringBuilder domain = new StringBuilder();
        
        for(int i=(this.size() - this.domainRdnIndex - 1); i >= 0; i--) {
            domain.append( this.get(i).replaceAll("dc=", "."));
        }
        
        return domain.deleteCharAt(0).toString();
    }
    
    public String getAsUserId() {
        StringBuilder userId = new StringBuilder();
        userId.append( this.get(this.size() - 1).replaceAll("uid=", "") );
        userId.append( "@users" );
        userId.append(".");
        userId.append(getDomain());

        return userId.toString();
    }
    
    
    
    
}
