/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.vhd.model;

import java.util.HashMap;
import java.util.List;
import org.springframework.ldap.core.DirContextAdapter;

/**
 *
 * @author forker
 */
public interface DisplayAttributeHelper {
    public HashMap<String, Object> getDisplayAttrDefinitionListForUser();
    public HashMap<String, Object> getUserAttributeMapForUser(DirContextAdapter dca);
    public HashMap<String, Object> apiToLdapAttrNames(HashMap<String, Object> attrMap);
}
