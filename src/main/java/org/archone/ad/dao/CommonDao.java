/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.dao;

import java.util.LinkedList;
import java.util.List;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;
import org.archone.ad.domain.entities.ShoadEntity;
import org.archone.ad.domain.entities.ShoadGroup;
import org.archone.ad.domain.entities.ShoadUser;
import org.archone.ad.naming.NameConvertor;
import org.springframework.ldap.core.DirContextAdapter;

/**
 *
 * @author forker
 */
public class CommonDao {

    private DirContext dirContext;
    private NameConvertor nameConvertor;
    private SearchControls defaultSearchControls;

    public CommonDao(DirContext dirContext, NameConvertor nameConvertor) {
        this.dirContext = dirContext;
        this.nameConvertor = nameConvertor;
        
        defaultSearchControls = new SearchControls();
        defaultSearchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    }

    public void remove(DirContextAdapter dca) throws NamingException {
        this.dirContext.unbind(dca.getNameInNamespace());
    }

    public void remove(LdapName name) throws NamingException {
        this.dirContext.unbind(name);
    }

    public void create(LdapName name, DirContextAdapter dca) throws NamingException {
        this.dirContext.bind(name, dca);
    }

    public ShoadGroup lookupGroup(String groupId) throws NamingException {
        return (ShoadGroup) this.dirContext.lookup(nameConvertor.toGroupDn(groupId));
    }

    public ShoadUser lookupUser(String userId) throws NamingException {
        return (ShoadUser) this.dirContext.lookup(nameConvertor.toUserDn(userId));
    }

    public void save(ShoadEntity shoadEntity) throws NamingException {
        this.dirContext.bind(shoadEntity.getEntityDn(), shoadEntity);
    }

    public List<ShoadUser> listMembers(ShoadGroup group) throws NamingException {
        List<String> dns = group.listMemberDns();
        List<ShoadUser> users = new LinkedList<ShoadUser>();

        for (String dn : dns) {
            ShoadUser shoadUser = (ShoadUser) this.dirContext.lookup(dn);
            if (shoadUser != null) {
                shoadUser.setEntityId(nameConvertor.toUserId(dn));
                users.add(shoadUser);
            }
        }
        return users;
    }

    public List<ShoadUser> listUsers(String domain) throws NamingException {
        List<ShoadUser> users = new LinkedList<ShoadUser>();
        NamingEnumeration<SearchResult> searchResults = dirContext.search(nameConvertor.getUsersBaseDn(domain), "(uid=*)", defaultSearchControls);

        while (searchResults.hasMore()) {
            SearchResult sr = searchResults.next();
            ShoadUser shoadUser = (ShoadUser) this.dirContext.lookup(sr.getNameInNamespace());
            users.add(shoadUser);
        }
        
        return users;
    }
}
