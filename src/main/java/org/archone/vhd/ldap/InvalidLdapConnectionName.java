/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.vhd.ldap;

/**
 *
 * @author forker
 */
public class InvalidLdapConnectionName extends Exception {

    public InvalidLdapConnectionName() {
    }
    
    public InvalidLdapConnectionName(String message) {
        super(message);
    }
    
}
