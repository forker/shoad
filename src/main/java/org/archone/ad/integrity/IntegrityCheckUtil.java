/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.integrity;

import org.archone.ad.naming.DomainDn;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

/**
 *
 * @author romansergey
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
