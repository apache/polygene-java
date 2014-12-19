package org.qi4j.index.elasticsearch.extensions.spatial.mappings.builders;

import org.qi4j.index.elasticsearch.ElasticSearchSupport;

/**
 * Created by jj on 19.12.14.
 */
public class MappingQueryBuilder extends AbstractBuilder {

    public MappingQueryBuilder(ElasticSearchSupport support) {
        this.support = support;
    }

}
