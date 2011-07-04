/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.vhd.model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author forker
 */
public class SUADConfiguration {

    private String ldapUrl;
    private List<String> userObjectClassList = new LinkedList<String>();
    private List<String> groupObjectClassList = new LinkedList<String>();

    /**
     * @return the ldapUrl
     */
    public String getLdapUrl() {
        return ldapUrl;
    }

    /**
     * @param ldapUrl the ldapUrl to set
     */
    public void setLdapUrl(String ldapUrl) {
        this.ldapUrl = ldapUrl;
    }

    /**
     * @return the userObjectClassList
     */
    public List<String> getUserObjectClassList() {
        return userObjectClassList;
    }

    /**
     * @param userObjectClassList the userObjectClassList to set
     */
    public void setUserObjectClass(String userObjectClassStr) {
        this.userObjectClassList.addAll(Arrays.asList(userObjectClassStr.split(",")));
    }

    /**
     * @return the groupObjectClassList
     */
    public List<String> getGroupObjectClassList() {
        return groupObjectClassList;
    }

    /**
     * @param groupObjectClassList the groupObjectClassList to set
     */
    public void setGroupObjectClass(String groupObjectClassStr) {
        this.groupObjectClassList.addAll(Arrays.asList(groupObjectClassStr.split(",")));
    }
}
