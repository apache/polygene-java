package org.qi4j.index.elasticsearch.extensions.spatial.functions.predicates;

import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.geo.builders.CircleBuilder;
import org.elasticsearch.common.geo.builders.PointBuilder;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.*;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.geometry.TGeometry;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.SpatialPredicatesSpecification;
import org.qi4j.api.structure.Module;
import org.qi4j.functional.Specification;
import org.qi4j.index.elasticsearch.extensions.spatial.internal.AbstractElasticSearchSpatialFunction;
import org.qi4j.spi.query.EntityFinderException;

import java.util.Map;

/**
 * Created by jj on 19.11.14.
 */
public class ST_DisjointFunction extends AbstractElasticSearchSpatialFunction implements ElasticSearchSpatialPredicateFinderSupport.PredicateSpecification {


    public void processSpecification( FilterBuilder filterBuilder,
                                      SpatialPredicatesSpecification<?> spec,
                                      Map<String, Object> variables )
            throws EntityFinderException
    {
        if (spec.value() == null && spec.operator() == null)
            throw new UnsupportedOperationException("ST_Within specification ...todo :"
                    + spec.getClass() + ": " + spec);

        System.out.println("ST_DisjointFunction Spec");

        try {
            TGeometry geometry = resolveGeometry(spec, module);
            addFilter(createShapeFilter(spec.property().toString(), geometry, ShapeRelation.DISJOINT), filterBuilder);
        } catch(Exception _ex) {
            _ex.printStackTrace();
        }
    }

}
