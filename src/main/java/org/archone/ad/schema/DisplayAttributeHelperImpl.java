/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.ad.schema;

import flexjson.JSONDeserializer;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.ldap.core.DirContextAdapter;

/**
 *
 * @author forker
 */
public class DisplayAttributeHelperImpl implements DisplayAttributeHelper {

    @Autowired
    private ApplicationContext applicationContext;
    private String configPath;
    private List<DisplayAttribute> displayAttributeList;
    private HashMap<String, DisplayAttribute> apiNameIndex = new HashMap<String, DisplayAttribute>();
    private HashMap<String, DisplayAttribute> ldapNameIndex = new HashMap<String, DisplayAttribute>();

    public DisplayAttributeHelperImpl(String configPath) {
        this.configPath = configPath;
    }

    public void init() throws FileNotFoundException, IOException {
        Resource r = applicationContext.getResource(this.configPath);

        FileReader attrDefs = new FileReader(r.getFile());
        JSONDeserializer jd = new JSONDeserializer().use(null, LinkedList.class ).use( "values", DisplayAttribute.class );
        this.displayAttributeList = (List<DisplayAttribute>) jd.deserialize(attrDefs);
        
        
        for (DisplayAttribute displayAttribute : displayAttributeList) {

            if (displayAttribute.getAccess() != null && displayAttribute.getAccess().contains("user")) {
                
                if(displayAttribute.getApiName() == null) {
                    displayAttribute.setApiName(displayAttribute.getLdapName());
                }
                
                this.apiNameIndex.put(displayAttribute.getApiName(), displayAttribute);
                this.ldapNameIndex.put(displayAttribute.getLdapName(), displayAttribute);
            }
        }
        
        
    }

    @Override
    public HashMap<String, DisplayAttribute> getDisplayAttrDefinitionListForUser() {
        HashMap<String, DisplayAttribute> attrDefMap = new HashMap<String, DisplayAttribute>();

        for (DisplayAttribute displayAttribute : displayAttributeList) {

            if (displayAttribute.getAccess() != null && displayAttribute.getAccess().contains("user")) {
                attrDefMap.put(displayAttribute.getApiName(), displayAttribute);
            }
        }

        return attrDefMap;
    }

    @Override
    public HashMap<String, Object> getUserAttributeMapForUser(DirContextAdapter dca) {
        HashMap<String, Object> user = new HashMap<String, Object>();
        for (DisplayAttribute displayAttribute : displayAttributeList) {
            if (displayAttribute.getAccess() != null && displayAttribute.getAccess().contains("user")) {
                String ldapName = displayAttribute.getLdapName();
                String apiName = displayAttribute.getApiName();

                if (displayAttribute.getMultiValue() == true) {
                    Object[] attrs = dca.getObjectAttributes(ldapName);
                    if (attrs != null) {
                        user.put(apiName, attrs);
                    }
                } else {
                    Object attr = dca.getObjectAttribute(ldapName);
                    if (attr != null) {
                        user.put(apiName, attr);
                    }
                }
            }
        }
        return user;
    }
    
    @Override
    public HashMap<String, Object> apiToLdapAttrNames(HashMap<String, Object> attrMap) {
        HashMap<String, Object> ldapAttrMap = new HashMap<String, Object>();
        for(Entry<String, Object> entry : attrMap.entrySet()) {
            if(this.apiNameIndex.get(entry.getKey()) != null) {
                ldapAttrMap.put( (String) this.apiNameIndex.get(entry.getKey()).getLdapName() , entry.getValue());
            }
        }
        
        return ldapAttrMap;
    }
    
    @Override
    public String getApiName(String ldapName) {
        return (String) this.ldapNameIndex.get(ldapName).getApiName();
    }
    
    @Override
    public String getLdapName(String apiName) {
        return (String) this.apiNameIndex.get(apiName).getLdapName();
    }
    
    @Override
    public DisplayAttribute byApiName(String apiName) {
        return this.apiNameIndex.get(apiName);
    }

    @Override
    public DisplayAttribute byLdapName(String ldapName) {
        return this.ldapNameIndex.get(ldapName);
    }    
    
    public HashMap<String, DisplayAttribute> getApiNameIndexedAttrDef() {
        return this.apiNameIndex;
    }
    
}
