package org.qi4j.index.elasticsearch.extensions.spatial.mappings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by jj on 06.11.14.
 */
public class ElasticSearchMappingsCache {

    private static Map<String,Map<String, Object>> ES_MAPPINGS_CACHE_V2 = new HashMap<>();

    private long ttl;

    public static ElasticSearchMappingsCache MappingsCache() {
        return new ElasticSearchMappingsCache();
    }


    public boolean exists(String key) {
        return ES_MAPPINGS_CACHE_V2.containsKey(key);
    }

    public Map<String, Object> get(String key)
    {
        return ES_MAPPINGS_CACHE_V2.get(key);
    }


    public void put(String key, Map<String, Object> values) {

        ES_MAPPINGS_CACHE_V2.put(key, values);
    }

    public boolean putIfNotExist(String key, Map<String, Object> values) {
        if (!exists(key)) {
            put(key, values);
            return false;
        } else
            return true;
    }

    public String toString() {
        return ES_MAPPINGS_CACHE_V2.toString();
    }




}


