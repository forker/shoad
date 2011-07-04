/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.vhd.model;

/**
 *
 * @author forker
 */
public class DisplayAttribute {

    private String ldapName;
    private String apiName;
    private String displayName;
    private String type;
    private String access;
    private String unique;
    private boolean multiValue = false;
    private boolean domainUnique = false;
    private boolean globallyUnique = false;
    private boolean mustHave = false;

    /**
     * @return the ldapName
     */
    public String getLdapName() {
        return ldapName;
    }

    /**
     * @param ldapName the ldapName to set
     */
    public void setLdapName(String ldapName) {
        this.ldapName = ldapName;
    }

    /**
     * @return the apiName
     */
    public String getApiName() {
        return apiName;
    }

    /**
     * @param apiName the apiName to set
     */
    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the access
     */
    public String getAccess() {
        return access;
    }

    /**
     * @param access the access to set
     */
    public void setAccess(String access) {
        this.access = access;
    }

    /**
     * @return the unique
     */
    public String getUnique() {
        return unique;
    }

    /**
     * @param unique the unique to set
     */
    public void setUnique(String unique) {
        
        if(unique != null) {
            if(unique.contains("domain")) {
                setDomainUnique(true);
            }
            if(unique.contains("globally")) {
                setGloballyUnique(true);
            }
        }
        
        this.unique = unique;
    }

    /**
     * @return the multiValue
     */
    public boolean getMultiValue() {
        return multiValue;
    }

    /**
     * @param multiValue the multiValue to set
     */
    public void setMultiValue(boolean multiValue) {
        this.multiValue = multiValue;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the domainUnique
     */
    public boolean isDomainUnique() {
        return domainUnique;
    }

    /**
     * @param domainUnique the domainUnique to set
     */
    public void setDomainUnique(boolean domainUnique) {
        this.domainUnique = domainUnique;
    }

    /**
     * @return the globallyUnique
     */
    public boolean isGloballyUnique() {
        return globallyUnique;
    }

    /**
     * @param globallyUnique the globallyUnique to set
     */
    public void setGloballyUnique(boolean globallyUnique) {
        this.globallyUnique = globallyUnique;
    }

    /**
     * @return the mustHave
     */
    public boolean isMustHave() {
        return mustHave;
    }

    /**
     * @param mustHave the mustHave to set
     */
    public void setMustHave(boolean mustHave) {
        this.mustHave = mustHave;
    }
    
    
    
}
