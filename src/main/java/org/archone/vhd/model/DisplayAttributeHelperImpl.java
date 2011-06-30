/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.vhd.model;

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
import org.springframework.util.Assert;

/**
 *
 * @author forker
 */
public class DisplayAttributeHelperImpl implements DisplayAttributeHelper {

    @Autowired
    private ApplicationContext applicationContext;
    private String configPath;
    private List<HashMap<String, Object>> displayAttributeList;
    private HashMap<String, HashMap> apiNameIndex = new HashMap<String, HashMap>();

    public DisplayAttributeHelperImpl(String configPath) {
        this.configPath = configPath;
    }

    public void init() throws FileNotFoundException, IOException {
        Resource r = applicationContext.getResource(this.configPath);

        FileReader attrDefs = new FileReader(r.getFile());
        JSONDeserializer<List> jd = new JSONDeserializer<List>();
        this.displayAttributeList = jd.deserialize(attrDefs);
        
        
        for (HashMap<String, Object> displayAttribute : displayAttributeList) {

            if (displayAttribute.get("access") != null && ((String) displayAttribute.get("access")).contains("user")) {
                
                HashMap<String, Object> attrDef = (HashMap<String, Object>) displayAttribute.clone();
                
                
                String apiName = (String) (attrDef.get("apiName") != null ? attrDef.get("apiName") : attrDef.get("ldapName"));
                
                this.apiNameIndex.put(apiName, attrDef);
            }
        }
        
        
    }

    @Override
    public HashMap<String, Object> getDisplayAttrDefinitionListForUser() {
        HashMap<String, Object> attrDefMap = new HashMap<String, Object>();

        for (HashMap<String, Object> displayAttribute : displayAttributeList) {

            if (displayAttribute.get("access") != null && ((String) displayAttribute.get("access")).contains("user")) {
                
                HashMap<String, Object> attrDef = (HashMap<String, Object>) displayAttribute.clone();
                
                
                String apiName = (String) (attrDef.get("apiName") != null ? attrDef.get("apiName") : attrDef.get("ldapName"));
                
                /*
                 * 
                 */
                //attrDef.remove("ldapName");
                //attrDef.remove("access");
                
                attrDefMap.put(apiName, attrDef);
            }
        }

        return attrDefMap;
    }

    @Override
    public HashMap<String, Object> getUserAttributeMapForUser(DirContextAdapter dca) {
        HashMap<String, Object> user = new HashMap<String, Object>();
        for (HashMap<String, Object> displayAttribute : displayAttributeList) {
            if (displayAttribute.get("access") != null && ((String) displayAttribute.get("access")).contains("user")) {
                String ldapName = (String) displayAttribute.get("ldapName");
                Assert.notNull(ldapName, "Malformed or incomplete entry definition");

                String apiName = displayAttribute.get("apiName") != null ? (String) displayAttribute.get("apiName") : ldapName;

                if (displayAttribute.get("multiValue") != null && (Boolean) displayAttribute.get("multiValue") == true) {
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
    
    public HashMap<String, Object> apiToLdapAttrNames(HashMap<String, Object> attrMap) {
        HashMap<String, Object> ldapAttrMap = new HashMap<String, Object>();
        for(Entry<String, Object> entry : attrMap.entrySet()) {
            if(this.apiNameIndex.get(entry.getKey()) != null) {
                ldapAttrMap.put( (String) this.apiNameIndex.get(entry.getKey()).get("ldapName") , entry.getValue());
            }
        }
        
        return ldapAttrMap;
    }
    
    public HashMap<String, HashMap> getApiNameIndexedAttrDef() {
        return this.apiNameIndex;
    }
    
}
