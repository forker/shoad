/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.schema;

import java.util.HashMap;
import java.util.List;
import org.springframework.ldap.core.DirContextAdapter;

/**
 *
 * @author romansergey
 */
public interface DisplayAttributeHelper {
    public HashMap<String, DisplayAttribute> getDisplayAttrDefinitionListForUser();
    public HashMap<String, Object> getUserAttributeMapForUser(DirContextAdapter dca);
    public HashMap<String, Object> apiToLdapAttrNames(HashMap<String, Object> attrMap);
    public String getApiName(String ldapName);
    public String getLdapName(String apiName);
    public DisplayAttribute byLdapName(String ldapName);
    public DisplayAttribute byApiName(String apiName);
    public HashMap<String, DisplayAttribute> getApiNameIndexedAttrDef();
}
