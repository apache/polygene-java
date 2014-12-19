package org.qi4j.index.elasticsearch.extensions.spatial.mappings.old;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;

import java.util.concurrent.TimeUnit;

/**
 * Created by jj on 06.11.14.
 */
public class ElasticSearchMappingsCache {


         private static LoadingCache<String, String> ES_MAPPINGS_CACHE =
                CacheBuilder.newBuilder()
                        .expireAfterAccess(1, TimeUnit.SECONDS)
                        .concurrencyLevel(50) // valid ?
                        .build(
                                new CacheLoader<String, String>() {
                                    public String load(String key) {
                                        return loadMapping(key);
                                    }
                                }
                        );

    private static ElasticSearchSupport support = null;

    private static String loadMapping(String key)
    {
        System.out.println("Loading key " + key );
        try
        {

            if (support != null) // <- should never be null as this method is called by the cache loader after the support already initialised.
            if (ElasticSearchMappingsHelper.Mappings(support).onIndex(support.index()).andType(support.entitiesType()).existsFieldMapping(key))
                return ElasticSearchMappingsHelper.Mappings(support).onIndex(support.index()).andType(support.entitiesType()).getFieldMappings(key).sourceAsMap().toString();
            else
                return "";
        } catch(Exception _ex)
        {
           //  _ex.printStackTrace();
            return "";
        }
        return "";
    }

    public static ElasticSearchMappingsCache MappingsCache(ElasticSearchSupport support) {
        if (ElasticSearchMappingsCache.support == null) ElasticSearchMappingsCache.support = support;
        return new ElasticSearchMappingsCache();
    }


    public boolean exists(String key)
    {
            return (ES_MAPPINGS_CACHE.getUnchecked(key) == null) || ES_MAPPINGS_CACHE.getUnchecked(key).length() == 0 ? false : true;
    }

    public String get(String key)
    {
        return ES_MAPPINGS_CACHE.getUnchecked(key);
    }



    public void put(String key, String mappings)
    {
        System.out.println("key " + key + " mappings " + mappings);
        ES_MAPPINGS_CACHE.put(key, mappings);
    }

    public boolean putIfNotExist(String key, String mappings) {
        if (!exists(key)) {
            put(key, mappings);
            return false;
        } else
            return true;
    }

    public String toString() {
        return ES_MAPPINGS_CACHE.toString();
    }




}


