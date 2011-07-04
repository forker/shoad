/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.vhd.model;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

/**
 *
 * @author forker
 */
public class IntegrityCheckUtil {
    
    private DirContext dirContext;

    public IntegrityCheckUtil(DirContext dirContext) {
        this.dirContext = dirContext;
    }
    
    public boolean isUnique(DomainDn domainDn, String attrName, String attrValue) throws NamingException {
        
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        return !dirContext.search(domainDn, "(" + attrName + "=" + attrValue + ")", controls).hasMore();
    }
    
}
