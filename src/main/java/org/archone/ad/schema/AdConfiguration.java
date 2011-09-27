/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.schema;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author romansergey
 */
public class AdConfiguration {

    private String ldapUrl;
    private List<String> userObjectClassList = new LinkedList<String>();
    private List<String> groupObjectClassList = new LinkedList<String>();
    private String groupsRdn = "ou=groups";
    private String usersRdn = "ou=users";
    private String groupSearchBase = "";
    private String groupSearchFilter = "(cn={0})";
    private String groupRoleAttribute = "uniqueMember";
    private String membershipSearchFilter = "(uniqueMember={0})";
    private String userDnSearchFilter = "(|(mail={0})(email={0}))";

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

    /**
     * @return the groupsRdn
     */
    public String getGroupsRdn() {
        return groupsRdn;
    }

    /**
     * @param groupsRdn the groupsRdn to set
     */
    public void setGroupsRdn(String groupsRdn) {
        this.groupsRdn = groupsRdn;
    }

    /**
     * @return the usersRdn
     */
    public String getUsersRdn() {
        return usersRdn;
    }

    /**
     * @param usersRdn the usersRdn to set
     */
    public void setUsersRdn(String usersRdn) {
        this.usersRdn = usersRdn;
    }

    /**
     * @return the groupSearchBase
     */
    public String getGroupSearchBase() {
        return groupSearchBase;
    }

    /**
     * @param groupSearchBase the groupSearchBase to set
     */
    public void setGroupSearchBase(String groupSearchBase) {
        this.groupSearchBase = groupSearchBase;
    }

    /**
     * @return the groupSearchFilter
     */
    public String getGroupSearchFilter() {
        return groupSearchFilter;
    }

    /**
     * @param groupSearchFilter the groupSearchFilter to set
     */
    public void setGroupSearchFilter(String groupSearchFilter) {
        this.groupSearchFilter = groupSearchFilter;
    }

    /**
     * @return the groupRoleAttribute
     */
    public String getGroupRoleAttribute() {
        return groupRoleAttribute;
    }

    /**
     * @param groupRoleAttribute the groupRoleAttribute to set
     */
    public void setGroupRoleAttribute(String groupRoleAttribute) {
        this.groupRoleAttribute = groupRoleAttribute;
    }

    /**
     * @return the membershipSearchFilter
     */
    public String getMembershipSearchFilter() {
        return membershipSearchFilter;
    }

    /**
     * @param membershipSearchFilter the membershipSearchFilter to set
     */
    public void setMembershipSearchFilter(String membershipSearchFilter) {
        this.membershipSearchFilter = membershipSearchFilter;
    }

    /**
     * @return the userDnSearchFilter
     */
    public String getUserDnSearchFilter() {
        return userDnSearchFilter;
    }

    /**
     * @param userDnSearchFilter the userDnSearchFilter to set
     */
    public void setUserDnSearchFilter(String userDnSearchFilter) {
        this.userDnSearchFilter = userDnSearchFilter;
    }
}
