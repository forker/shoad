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
public class DomainDn extends LdapName {

    public DomainDn(String dn) throws InvalidNameException {
        super(dn);

        if (!("," + dn).matches("(,dc=[a-z0-9\\-\\.]{1,}){1,}")) {
            throw new InvalidNameException();
        }
    }
    
    public static DomainDn fromDomain(String domain) throws InvalidNameException {
        return new DomainDn( parseDomainDn(domain) );
    }

    public String getDomain() {
        StringBuilder domain = new StringBuilder();

        for (int i = (this.size() - 1); i >= 0; i--) {
            domain.append(this.get(i).replaceAll("dc=", "."));
        }

        return domain.deleteCharAt(0).toString();
    }

    public static String parseDomainDn(String domain) {
        String domainMatch = "(\\.[a-z0-9\\-]{1,}){1,}";
        if (("." + domain).matches(domainMatch)) {
            return "dc=" + domain.replaceAll("\\.", ",dc=");
        } else {
            throw new RuntimeException("Doesn't look like a domain");
        }

    }
}
