/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.ldap;

/**
 *
 * @author romansergey
 */
public class InvalidLdapConnectionName extends Exception {

    public InvalidLdapConnectionName() {
    }
    
    public InvalidLdapConnectionName(String message) {
        super(message);
    }
    
}
