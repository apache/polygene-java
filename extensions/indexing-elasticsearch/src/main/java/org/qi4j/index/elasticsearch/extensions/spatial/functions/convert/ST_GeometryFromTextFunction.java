package org.qi4j.index.elasticsearch.extensions.spatial.functions.convert;

import org.elasticsearch.index.query.FilterBuilder;
import org.qi4j.api.geometry.TGeometry;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.SpatialPredicatesSpecification;
import org.qi4j.api.structure.Module;
import org.qi4j.index.elasticsearch.extensions.spatial.internal.AbstractElasticSearchSpatialFunction;
import org.qi4j.spi.query.EntityFinderException;

import java.util.Map;

/**
 * Created by jj on 20.11.14.
 */
public class ST_GeometryFromTextFunction extends AbstractElasticSearchSpatialFunction implements ElasticSearchSpatialConvertFinderSupport.ConvertSpecification {


    public TGeometry processSpecification(FilterBuilder filterBuilder, SpatialConvertSpecification<?> spec, Map<String, Object> variables)  throws EntityFinderException
    {

        System.out.println("ST_GeometryFromTextFunction()");

try {
    return spec.convert(module);
} catch(Exception _ex) {
    _ex.printStackTrace();
}

        return null;
    }
}
