/*
 * Copyright (c) 2014, Jiri Jetmar. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.index.elasticsearch.extensions.spatial.mappings.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;

import java.util.concurrent.TimeUnit;

import static org.qi4j.index.elasticsearch.extensions.spatial.mappings.builders.SpatialMappingFactory.MappingQuery;


public class MappingsCache
{

    private static final int TTL_SECONDS = 1 * 60; // <- JJ TODO make it configurable
    private static final int CONCURENCY_LEVEL = 50;

    private LoadingCache<String, String> ES_MAPPINGS_CACHE;

    private ElasticSearchSupport support;

    {

        ES_MAPPINGS_CACHE =
                CacheBuilder.newBuilder()
                        .expireAfterAccess(TTL_SECONDS, TimeUnit.SECONDS)
                        .concurrencyLevel(CONCURENCY_LEVEL) // valid ?
                        .build(
                                new CacheLoader<String, String>()
                                {
                                    public String load(String key)
                                    {
                                        if (valid())
                                        {
                                            return reloadStrategy(key);
                                        } else
                                            return "";
                                    }
                                }
                        );
    }


    public MappingsCache(ElasticSearchSupport support)
    {
        this.support = support;
    }

    private String reloadStrategy(String key)
    {
        String result = MappingQuery(support).get(key);
        return (result == null || result.length() == 0) ? "" : result;
    }

    private boolean valid()
    {
        return (support != null) && (support.index() != null) && (support.entitiesType() != null) ? true : false;
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
        if (mappings != null)
            ES_MAPPINGS_CACHE.put(key, mappings);
    }

    public boolean putIfNotExist(String key, String mappings)
    {
        if (!exists(key))
        {
            put(key, mappings);
            return false;
        } else
            return true;
    }
}
