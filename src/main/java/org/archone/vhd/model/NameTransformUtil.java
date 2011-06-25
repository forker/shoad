/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.vhd.model;

import org.springframework.util.Assert;

/**
 *
 * @author forker
 */
public class NameTransformUtil {
    private String userSubnode;
    private String groupSubnode;
    private String domainDn;

    public NameTransformUtil(String userSubnode, String groupSubnode, String domainDn) {
        this.userSubnode = userSubnode;
        this.groupSubnode = groupSubnode;
        this.domainDn = domainDn;
    }
    
    public NameTransformUtil(String userSubnode, String groupSubnode) {
        this.userSubnode = userSubnode;
        this.groupSubnode = groupSubnode;
    }
    
    public String parseUserDn(String userId) {
        Assert.notNull(this.domainDn, "Should be null, reignite the object with domainDn");
        String userPrefixMatch = "[a-z\\.\\-]{1,}@users";
        String domainMatch = "(\\.[a-z\\-]{1,}){1,}";
        
        if(userId.matches(userPrefixMatch + domainMatch)) {
            return "uid=" + userId.split("@")[0] + ",ou=users," + parseDomainDn( userId.replaceAll( userPrefixMatch + "\\.", "" ) );
        } else {
            throw new RuntimeException("UserId doesn't match the regex");
        }
    }
    
    public String parseUserId(String dn) {
        Assert.notNull(this.domainDn, "Should be null, reignite the object with domainDn");
        
        if(dn.matches("uid=[a-z\\.\\-]{1,}," + userSubnode + "," + this.domainDn)) {
            return dn.substring(4).replaceAll(",ou=", "@").replaceAll(",dc=", ".");
        } else {
            throw new RuntimeException("UserId doesn't match the regex");
        }
    }
    
    public String extractDomainDn(String groupId) {
        String adminGroupPrefix = "[a-zA-Z\\.\\-]{1,}" + groupSubnode.replaceAll("ou=", "@");
        String domainMatch = "(\\.[a-z\\-]{1,}){1,}";
        if(groupId.matches(adminGroupPrefix + domainMatch)) {
            return groupId.replaceAll(adminGroupPrefix, "").replaceAll("\\.", ",dc=").substring(1);
        } else {
            throw new RuntimeException("Doesn't look like a groupId");
        }
    }
    
    public String extractDomain(String groupId) {
        String adminGroupPrefix = "[a-zA-Z\\.\\-]{1,}" + groupSubnode.replaceAll("ou=", "@");
        String domainMatch = "(\\.[a-z\\-]{1,}){1,}";
        if(groupId.matches(adminGroupPrefix + domainMatch)) {
            return groupId.replaceAll(adminGroupPrefix + "\\.", "");
        } else {
            throw new RuntimeException("Doesn't look like a groupId");
        }
    }
    
    public boolean isAdminGroupId(String groupId) {
        String adminGroupPrefix = "administrator" + groupSubnode.replaceAll("ou=", "@");
        String domainMatch = "(\\.[a-z\\-]{1,}){1,}";
        if(groupId.matches(adminGroupPrefix + domainMatch)) {
            return true;
        } else {
            return false;
        }
    }
    
    public static String parseDomainDn(String domain) {
        String domainMatch = "(\\.[a-z\\-]{1,}){1,}";
        if(("." + domain).matches(domainMatch)) {
            return "dc=" + domain.replaceAll("\\.", ",dc=");
        } else {
            throw new RuntimeException("Doesn't look like a domain");
        }
        
    }
    
    public String getUserDnMatchPattern() {
        return "uid=[a-z\\.\\-]{1,}," + getUserBase();
    }
    public String getUserBase() {
        return userSubnode + "," + domainDn;
    }
    

    /**
     * @param domainDn the domainDn to set
     */
    public void setDomainDn(String domainDn) {
        this.domainDn = domainDn;
    }
    
}
