package org.qi4j.index.elasticsearch.extensions.spatial.functions.predicates;

import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.*;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_WithinSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.SpatialPredicatesSpecification;
import org.qi4j.api.util.Classes;
import org.qi4j.index.elasticsearch.ElasticSearchFinder;
import org.qi4j.index.elasticsearch.extensions.spatial.ElasticSearchSpatialFinderSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.internal.AbstractElasticSearchSpatialFunction;
import org.qi4j.spi.query.EntityFinderException;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by jj on 19.11.14.
 */
public class ST_Within extends AbstractElasticSearchSpatialFunction implements ElasticSearchSpatialPredicateFinderSupport.PredicateSpecification {


    public void processSpecification( FilterBuilder filterBuilder,
                                      SpatialPredicatesSpecification<?> spec,
                                      Map<String, Object> variables )
            throws EntityFinderException
    {
        if (spec.value() == null && spec.operator() == null && !(spec instanceof ST_WithinSpecification))
            throw new UnsupportedOperationException("ST_Within specification ...todo :"
                    + spec.getClass() + ": " + spec);



        try {


            TGeometry filterGeometry = resolveGeometry(filterBuilder,spec, module);

            // TPoint are managed in a different way.. JJ TODO
            if ((filterGeometry instanceof TPoint) && ((ST_WithinSpecification)spec).getDistance() > 0)
            {

                addFilter(createGeoDistanceFilter(
                        spec.property().toString(),
                        (TPoint)verifyProjection(filterGeometry),
                        ((ST_WithinSpecification)spec).getDistance(),
                        ((ST_WithinSpecification)spec).getUnit()),
                        filterBuilder
                );

            }
            else if (isPropertyTypeTPoint(spec.property())) // property is of type TPoint
            {
                // TODO MULTIPOINT, LINE, etc ?
                if (filterGeometry instanceof TPolygon) {
                    TPolygon polygonFilter = (TPolygon)verifyProjection(filterGeometry);

                    GeoPolygonFilterBuilder geoPolygonFilterBuilder = FilterBuilders.geoPolygonFilter(spec.property().toString());

                    for (int i = 0; i < polygonFilter.shell().get().getNumPoints(); i++) {
                        TPoint point = polygonFilter.shell().get().getPointN(i);
                        geoPolygonFilterBuilder.addPoint(point.x(), point.y());
                    }
                    addFilter(geoPolygonFilterBuilder, filterBuilder);
                }
                else
                    throw new RuntimeException("JJ TODO - on TPoints we are just supporting TPolygon");
            }
            else
            {
                addFilter(createShapeFilter(spec.property().toString(), filterGeometry, ShapeRelation.WITHIN), filterBuilder);
            }

            /**
            TGeometry geometry = resolveGeometry(filterBuilder,spec, module);

            if (geometry instanceof TPoint)
            {
                addFilter(createShapeFilter(spec.property().toString(), (TPoint)geometry, ShapeRelation.WITHIN, ((ST_WithinSpecification)spec).getDistance(), ((ST_WithinSpecification)spec).getUnit()), filterBuilder);
            }
            else if (geometry instanceof TPolygon) {
                addFilter(createShapeFilter(spec.property().toString(), geometry, ShapeRelation.WITHIN), filterBuilder);
            }
            else
                throw new UnsupportedOperationException("ST_Within specification - TPoint or TPolygon");

             */
        } catch(Exception _ex) {
            _ex.printStackTrace();
        }
    }

}
