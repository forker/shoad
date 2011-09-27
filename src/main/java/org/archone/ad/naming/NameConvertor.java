/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.naming;

import javax.naming.InvalidNameException;
import org.archone.ad.schema.AdConfiguration;

/**
 *
 * @author forker
 */
public class NameConvertor {
    
    private AdConfiguration adConfiguration;

    public NameConvertor() {
    }


    public String toGroupDn(String groupId) {
        return "cn=" + groupId.split("@")[0] + "," + getAdConfiguration().getGroupsRdn() + "," + extractDomainDn(groupId).toLowerCase();
    }


    public String toUserDn(String userId) {
        return "uid=" + userId.split("@")[0] + "," + getAdConfiguration().getUsersRdn() + "," + extractDomainDn(userId).toLowerCase();
    }


    public String toGroupId(String groupDn) {
        return groupDn.replaceAll("cn=", "").replaceAll("," + getAdConfiguration().getGroupsRdn() , "@groups").replaceAll(",dc=", ".").toLowerCase();
    }


    public String toUserId(String userDn) {
        return userDn.replaceAll("uid=", "").replaceAll("," +  getAdConfiguration().getUsersRdn(), "@").replaceAll(",dc=", ".").toLowerCase();
    }


    public String extractDomain(String entityId) {
        return entityId.replaceAll("[a-z\\.\\-]{1,}@(groups)\\.", "").toLowerCase();
    }


    public String extractDomainDn(String entityId) {
        return toDomainDn( extractDomain(entityId) ).toLowerCase();
    }


    public String toDomainDn(String domain) {
        String domainMatch = "(\\.[a-z0-9\\-]{1,}){1,}";
        if (("." + domain).matches(domainMatch)) {
            return "dc=" + domain.replaceAll("\\.", ",dc=").toLowerCase();
        } else {
            throw new RuntimeException("Doesn't look like a domain");
        }
    }


    public String toDomain(String domainDn) throws InvalidNameException {
        if (!("," + domainDn).matches("(,dc=[a-z0-9\\-\\.]{1,}){1,}")) {
            throw new InvalidNameException();
        }
        return domainDn.replace(",dc=", ".").substring(3).toLowerCase();
    }
    

    public String getUsersBaseDn(String domain) {
        return getAdConfiguration().getUsersRdn() + "," + toDomainDn(domain);
    }

    /**
     * @return the adConfiguration
     */
    public AdConfiguration getAdConfiguration() {
        return adConfiguration;
    }

    /**
     * @param adConfiguration the adConfiguration to set
     */
    public void setAdConfiguration(AdConfiguration adConfiguration) {
        this.adConfiguration = adConfiguration;
    }
    
    
    
    
}
