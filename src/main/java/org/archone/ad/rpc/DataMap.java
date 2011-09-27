/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.rpc;

import java.util.HashMap;

/**
 *
 * @author forker
 */
public class DataMap extends HashMap<String, Object> {

    public DataMap( HashMap<String, Object> map ) {
        super(map);
    }

    public DataMap() {
    }
    
    public String getString(String name) {
        return (String) this.get(name);
    }

    public Integer getInt(String name) {
        return (Integer) this.get(name);
    }

    public Long getLong(String name) {
        return (Long) this.get(name);
    }

    public DataMap getMap(String name) {
        return new DataMap((HashMap<String, Object>) this.get(name));
    }
    
}
