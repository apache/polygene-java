package org.qi4j.index.elasticsearch.internal;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jj on 06.11.14.
 */
public class ElasticSearchMappingsCache {


    private static Set<String> ES_MAPPINGS_CACHE = new HashSet<String>();
    private long ttl;

    public static ElasticSearchMappingsCache MappingsCache() {
        return new ElasticSearchMappingsCache();
    }


    public boolean exists(String key) {
        return ES_MAPPINGS_CACHE.contains(key);
    }

    public void put(String key) {
        ES_MAPPINGS_CACHE.add(key);
    }

    public boolean putIfNotExist(String key) {
        if (!exists(key)) {
            put(key);
            return false;
        } else
            return true;
    }

    public String toString() {
        return ES_MAPPINGS_CACHE.toString();
    }




}


