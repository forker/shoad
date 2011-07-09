/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.model;

import java.util.List;
import javax.naming.InvalidNameException;
import org.archone.ad.authentication.BasicUser;

/**
 *
 * @author forker
 */
public interface UserHelper {
    public List<String> getAdminDomains(BasicUser basicUser) throws InvalidNameException;
    
}
