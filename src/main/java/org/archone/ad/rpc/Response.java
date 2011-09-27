/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.rpc;

/**
 *
 * @author forker
 */
public class Response {
    
    public static DataMap success() {
        DataMap response = new DataMap();
        response.put("result", "SUCCESS");
        return response;
    }
    
    public static DataMap failure() {
        DataMap response = new DataMap();
        response.put("result", "FAILURE");
        return response;
    }
    
    public static DataMap result(String result) {
        DataMap response = new DataMap();
        response.put("result", result);
        return response;
    } 
}
