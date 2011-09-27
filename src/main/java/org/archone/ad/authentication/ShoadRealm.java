/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.authentication;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.archone.ad.naming.GroupDn;
import org.archone.ad.schema.AdConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.util.Assert;

/**
 *
 * @author forker
 */
public class ShoadRealm extends AuthorizingRealm {

    private ContextSource contextSource;
    @Autowired
    private AdConfiguration adConfiguration;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        if (principals == null) {
            throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
        }

        String username = (String) getAvailablePrincipal(principals);


        Set<String> roleNames = null;
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        DirContextAdapter context = (DirContextAdapter) contextSource.getReadOnlyContext();
        try {
            String userDn = (String) getUserDn(username);

            DirContextAdapter superuserGroup = (DirContextAdapter) new LdapTemplate(contextSource).lookup("cn=administrator,cn=shoad");
            Set<String> superusers = superuserGroup.getAttributeSortedStringSet("uniqueMember");

            Logger.getLogger("AUTH").log(Level.INFO, "THE SIZE IS {0}", new Integer(superusers.size()).toString());

            if (superusers.contains(userDn)) {
                Logger.getLogger("AUTH").log(Level.INFO, "SUPERUSER LOGGED IN");
                roleNames.add("SUPERUSER");
            }

            NamingEnumeration<SearchResult> searchResults = context.search("", adConfiguration.getMembershipSearchFilter(), new String[]{userDn}, controls);
            while (searchResults.hasMore()) {
                GroupDn groupDn = new GroupDn(searchResults.next().getNameInNamespace(), adConfiguration.getGroupsRdn());
                roleNames.add(groupDn.getAsGroupId());
            }

        } catch (javax.naming.NamingException ex) {
            Logger.getLogger(ShoadRealm.class.getName()).log(Level.SEVERE, null, ex);
            throw new AuthorizationException(ex);
        }


        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roleNames);

        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();
        Assert.notNull(username, "Null usernames are not allowed by this realm.");
        String password = new String(upToken.getPassword());
        Assert.hasLength(password, "Empty passwords are not allowed by this realm.");

        DirContext ctx = null;
        try {
            String userDn = getUserDn(username);

            ctx = contextSource.getContext(userDn, password);

            Attributes attrs = ctx.getAttributes(userDn);
            DirContextAdapter result = new DirContextAdapter(attrs, new DistinguishedName(userDn));

            return new SimpleAuthenticationInfo(result, password.toCharArray(), getName());

        } catch (javax.naming.NamingException ex) {
            throw new AuthenticationException();
        } catch (NamingException ex) {
            throw new AuthenticationException();
        }
    }

    private String getUserDn(String username) throws javax.naming.NamingException {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        DirContext dirContext = contextSource.getReadOnlyContext();
        NamingEnumeration<SearchResult> searchResults = dirContext.search("", adConfiguration.getUserDnSearchFilter(), new String[]{username}, controls);

        SearchResult sr = searchResults.next();

        if (sr == null || searchResults.hasMore()) {
            throw new AuthenticationException();
        }

        return sr.getNameInNamespace();
    }

    /**
     * @return the contextSource
     */
    public ContextSource getContextSource() {
        return contextSource;
    }

    /**
     * @param contextSource the contextSource to set
     */
    public void setContextSource(ContextSource contextSource) {
        this.contextSource = contextSource;
    }
}
