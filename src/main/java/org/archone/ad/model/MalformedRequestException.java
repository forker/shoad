/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.model;

/**
 *
 * @author romansergey
 */
public class MalformedRequestException extends RuntimeException {

    public MalformedRequestException() {
    }
    
    public MalformedRequestException(String message) {
        super(message);
    }
    
}
