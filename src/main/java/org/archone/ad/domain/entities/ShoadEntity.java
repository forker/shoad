/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.domain.entities;

import org.springframework.ldap.core.DirContextAdapter;

/**
 *
 * @author forker
 */
public abstract class ShoadEntity extends DirContextAdapter {

    private String entityId;

    public String getEntityId() {
        return this.entityId;
    }

    public String getEntityDn() {
        return this.getNameInNamespace();
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
}
