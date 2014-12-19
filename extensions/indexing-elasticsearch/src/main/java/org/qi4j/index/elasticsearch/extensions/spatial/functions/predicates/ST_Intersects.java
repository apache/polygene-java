package org.qi4j.index.elasticsearch.extensions.spatial.functions.predicates;

import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.index.query.FilterBuilder;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.SpatialPredicatesSpecification;
import org.qi4j.index.elasticsearch.extensions.spatial.internal.AbstractElasticSearchSpatialFunction;
import org.qi4j.spi.query.EntityFinderException;

import java.util.Map;

/**
 * Created by jj on 04.12.14.
 */
public class ST_Intersects extends AbstractElasticSearchSpatialFunction implements PredicateFinderSupport.PredicateSpecification
{

    public void processSpecification(FilterBuilder filterBuilder,
                                     SpatialPredicatesSpecification<?> spec,
                                     Map<String, Object> variables)
            throws EntityFinderException {
        if ((spec.value() == null && spec.operator() == null))
            throw new EntityFinderException("Insufficient data provided. ST_Intersects specification requires " +
                    "valid filter geometry of type " + TGeometry.class.getSimpleName());

        TGeometry filterGeometry = resolveGeometry(filterBuilder, spec, module);

        if (isPropertyOfTypeTPoint(spec.property()))
        {
            throw new EntityFinderException("Invalid ST_Intersects expression. Property on type " +
                    TPoint.class.getSimpleName() + " not supported.");
        }
        else
        {
            addFilter(createShapeFilter(spec.property().toString(), filterGeometry, ShapeRelation.INTERSECTS), filterBuilder);
        }

    }
}
