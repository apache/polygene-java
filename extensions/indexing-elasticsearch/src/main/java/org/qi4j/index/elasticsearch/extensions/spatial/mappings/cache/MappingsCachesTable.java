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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;


public class MappingsCachesTable
{

    private static final Table<String, String, MappingsCache> CACHES_TABLE = HashBasedTable.create();

    public static MappingsCache getMappingCache(String index, String type)
    {
        return CACHES_TABLE.get(index, type);
    }

    public static MappingsCache getMappingCache(ElasticSearchSupport support)
    {
        if (!CACHES_TABLE.contains(support.index(), support.entitiesType()))
        {
            CACHES_TABLE.put(support.index(), support.entitiesType(), new MappingsCache(support));
        }
        return CACHES_TABLE.get(support.index(), support.entitiesType());
    }

    public static void clear()
    {
        CACHES_TABLE.clear();
    }


}
