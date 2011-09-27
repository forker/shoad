/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.domain.entities;

import java.util.LinkedList;
import java.util.List;


/**
 *
 * @author forker
 */
public class ShoadGroup extends ShoadEntity {
    
    public void removeMember(ShoadUser user) {
        if(this.getAttributeSortedStringSet("uniqueMember").contains(user.getEntityDn())) {
            this.removeAttributeValue("uniqueMember", user.getEntityDn());
        }
    }

    public void addMember(ShoadUser user) {
        this.addAttributeValue("uniqueMember", user.getEntityDn());
    }

    public List<String> listMemberDns() {
        return new LinkedList<String>(this.getAttributeSortedStringSet("uniqueMember"));
    }
    
    
    
}
