/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.ad.naming;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

/**
 *
 * @author forker
 */
public class GroupDn extends LdapName {

    private String groupsRdn;
    private int domainRdnIndex;

    public GroupDn(String dn, String groupsRdn) throws InvalidNameException {

        super(dn);

        this.groupsRdn = groupsRdn;
        this.domainRdnIndex = new LdapName(groupsRdn).size() + 1;

        if (!dn.matches("cn=[a-z\\.\\-]{1,}," + groupsRdn + "(,dc=[a-z0-9\\-]{1,}){1,}")) {
            throw new InvalidNameException();
        }
    }

    public static GroupDn fromGroupId(String groupId, String groupsRdn) throws InvalidNameException {
        if (groupId.matches("[a-z\\.\\-]{1,}@groups(\\.[a-z\\-]{1,}){1,}")) {
            return new GroupDn("cn=" + groupId.split("@")[0] + "," + groupsRdn + "," + DomainDn.parseDomainDn(groupId.replaceAll("[a-z\\.\\-]{1,}@groups\\.", "")), groupsRdn);
        } else {
            throw new InvalidNameException();
        }
    }

    public String getDomain() {
        StringBuilder domain = new StringBuilder();

        for (int i = (this.size() - this.domainRdnIndex - 1); i >= 0; i--) {
            domain.append(this.get(i).replaceAll("dc=", "."));
        }

        return domain.deleteCharAt(0).toString();
    }

    public String getAsGroupId() {
        StringBuilder userId = new StringBuilder();
        userId.append(this.get(this.size() - 1).replaceAll("cn=", ""));
        userId.append("@groups");
        userId.append(".");
        userId.append(getDomain());

        return userId.toString();

    }

    public static boolean isAdminGroupId(String groupId) {
        String adminGroupPrefix = "administrator@groups";
        String domainMatch = "(\\.[a-z\\-]{1,}){1,}";
        if (groupId.matches(adminGroupPrefix + domainMatch)) {
            return true;
        } else {
            return false;
        }
    }
}
