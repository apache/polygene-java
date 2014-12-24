package org.qi4j.index.elasticsearch.extensions.spatial.mappings.cache;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;

/**
 * Created by jj on 19.12.14.
 */
public class MappingsCachesTable {

    private static final Table<String, String, MappingsCache> CACHES_TABLE = HashBasedTable.create();

    public static  MappingsCache getMappingCache(String index, String type)
    {
        return CACHES_TABLE.get(index, type);
    }

    public static MappingsCache getMappingCache(ElasticSearchSupport support)
    {
        if (!CACHES_TABLE.contains(support.index(), support.entitiesType())) {
            CACHES_TABLE.put(support.index(), support.entitiesType(), new MappingsCache(support));
        }
        return CACHES_TABLE.get(support.index(), support.entitiesType());
    }

    public static void clear()
    {
        CACHES_TABLE.clear();
    }


}
