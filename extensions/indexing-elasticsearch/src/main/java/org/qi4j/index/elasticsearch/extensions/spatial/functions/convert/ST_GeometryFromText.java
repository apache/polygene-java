package org.qi4j.index.elasticsearch.extensions.spatial.functions.convert;

import org.elasticsearch.index.query.FilterBuilder;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.index.elasticsearch.extensions.spatial.internal.AbstractElasticSearchSpatialFunction;
import org.qi4j.spi.query.EntityFinderException;

import java.util.Map;

import static org.qi4j.library.spatial.v2.conversions.TConversions.Convert;

/**
 * Created by jj on 20.11.14.
 */
public class ST_GeometryFromText extends AbstractElasticSearchSpatialFunction implements ConvertFinderSupport.ConvertSpecification {


    public void processSpecification(FilterBuilder filterBuilder, SpatialConvertSpecification<?> spec, Map<String, Object> variables)  throws EntityFinderException
    {

        System.out.println("ST_GeometryFromTextFunction()");

try {
    spec.setGeometry(Convert(module).from(spec.property()).toTGeometry());

    // return Convert(module).from(spec.property()).toTGeometry();

    // return spec.convert(module);
} catch(Exception _ex) {
    _ex.printStackTrace();
}

       //  return null;
    }
}
