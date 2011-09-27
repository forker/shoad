/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.rpc;

/**
 *
 * @author romansergey
 */
public class SecurityViolationException extends RuntimeException {

    public SecurityViolationException() {
    }
    
    public SecurityViolationException(String message) {
        super(message);
    }
    
}
