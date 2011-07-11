/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.authentication;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.simple.ParameterizedContextMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.util.Assert;

/**
 *
 * @author romansergey
 */
public class UserAttrsContextMapping implements UserDetailsContextMapper {

    private SpringSecurityLdapTemplate ldapTemplate;
    private ContextSource contextSource;
    private String password;

    public UserAttrsContextMapping(ContextSource contextSource) {
        Assert.notNull(contextSource, "contextSource must not be null");
        ldapTemplate = new SpringSecurityLdapTemplate(contextSource);
        this.contextSource = contextSource;
    }

    @Override
    public UserDetails mapUserFromContext(DirContextOperations dco, String username, Collection<GrantedAuthority> clctn) {

        DirContext dirContext = contextSource.getContext(dco.getNameInNamespace(), this.password);
                
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        List<String> groups = ldapTemplate.search("", "(uniqueMember=" + dco.getNameInNamespace() + ")(cn=administrator)", controls, new ParameterizedContextMapper<String>() {
            @Override
            public String mapFromContext(Object ctx) {
                DirContextAdapter adapter = (DirContextAdapter) ctx;
                if(adapter.getNameInNamespace().toLowerCase().matches("cn=administrator,ou=groups(,dc=[a-z]{1,}){1,}")) {
                    return adapter.getNameInNamespace().replaceAll(",ou=groups", "@groups").replaceAll(",(ou|dc)=", ".").replaceAll("cn=", "").toLowerCase();
                }
                return null;
            }
        });
        
        BasicUser bu = new BasicUser();
        bu.setDirContext(dirContext);
        bu.setGroups(groups);
        bu.setUsername(username);
        bu.setAccountNonExpired(true);
        bu.setAccountNonLocked(true);
        bu.setEnabled(true);
        bu.setAuthorities(clctn);
        bu.setCredentialsNonExpired(true);

        return bu;
    }

    @Override
    public void mapUserToContext(UserDetails ud, DirContextAdapter dca) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
