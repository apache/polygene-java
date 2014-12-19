package org.qi4j.index.elasticsearch.extensions.spatial.functions.predicates;

import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.GeoPolygonFilterBuilder;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_WithinSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.SpatialPredicatesSpecification;
import org.qi4j.index.elasticsearch.extensions.spatial.internal.AbstractElasticSearchSpatialFunction;
import org.qi4j.spi.query.EntityFinderException;

import java.util.Map;

import static org.qi4j.api.geometry.TGeometryFactory.TPoint;
import static org.qi4j.index.elasticsearch.extensions.spatial.mappings.old.ElasticSearchMappingsHelper.Mappings;


/**
 * Created by jj on 19.11.14.
 */
public class ST_WithinV2 extends AbstractElasticSearchSpatialFunction implements PredicateFinderSupport.PredicateSpecification {

    public void processSpecification( FilterBuilder filterBuilder,
                                      SpatialPredicatesSpecification<?> spec,
                                      Map<String, Object> variables )
                                                           throws EntityFinderException
    {
        if ((spec.value() == null && spec.operator() == null)) //  || !isSpatial()
                    throw new EntityFinderException("Insufficient data provided. ST_Within specification requires " +
                            "valid filter geometry of type " + TGeometry.class.getSimpleName());

        TGeometry filterGeometry = resolveGeometry(filterBuilder,spec, module);


        if (!isMapped(spec.property()))
        {
            System.out.println("Not mapped - no data ?, and therefore we are not adding the corresponding filter");
        }
        else {

            /**
             * When the geometry used in the ST_Within expression is of type TPoint and a distance is specified, e.g.
             *
             * TPoint filterGeometry = TPoint(module).x(..).y(..);
             * ST_Within (templateFor(x.class).propertyOfTypeTPoint(), filterGeometry, 1, TUnit.METER)
             *
             * then a ES GeoDistanceFilter is used.
             *
             */
            if (
                    TPoint(module).isPoint(filterGeometry) &&
                            ((ST_WithinSpecification) spec).getDistance() > 0 &&
                            Mappings(support).onIndex(support.index()).andType(support.entitiesType()).isGeoPoint(spec.property().toString())
                    ) {
                addFilter(createGeoDistanceFilter(
                                spec.property().toString(),
                                (TPoint) verifyProjection(filterGeometry),
                                ((ST_WithinSpecification) spec).getDistance(),
                                ((ST_WithinSpecification) spec).getUnit()),
                        filterBuilder
                );
            }
            /**
             * When the template property is of type TPoint then the filter property has to have an area.
             * Currently only filter geometries of type TPolygon are supported. E.g.
             *
             * TPolygon filterGeometry = TPolygon(module).shell(..)
             * ST_Within (templafeFor(x.class).propertyOfTypeTPoint(), filterGeometry);
             *
             *
             */
            else if (isPropertyOfType(TPoint.class, spec.property()) && isMappedAsGeoPoint(spec.property())) {

                if (filterGeometry instanceof TPolygon) {
                    /**
                     * This must not happen, but in case the expression is defined using WTK like :
                     *
                     * ST_Within (templateFor(x.class).propertyOfTypeTPoint(),
                     *              POLYGON((0 0,10 0,10 10,0 10,0 0),(5 5,7 5,7 7,5 7, 5 5)),
                     *              1, TUnit.METER) // <- This is invalid !!
                     *
                     * we have to check it here.
                     *
                     */
                    if (((ST_WithinSpecification) spec).getDistance() > 0)
                        throw new EntityFinderException("Invalid ST_Within expression. A " + TPolygon.class.getSimpleName() + " can " +
                                "not be combined with distance.");

                    TPolygon polygonFilter = (TPolygon) verifyProjection(filterGeometry);

                    GeoPolygonFilterBuilder geoPolygonFilterBuilder = FilterBuilders.geoPolygonFilter(spec.property().toString());

                    for (int i = 0; i < polygonFilter.shell().get().getNumPoints(); i++) {
                        TPoint point = polygonFilter.shell().get().getPointN(i);
                        geoPolygonFilterBuilder.addPoint(point.x(), point.y());
                    }
                    addFilter(geoPolygonFilterBuilder, filterBuilder);
                } else
                    throw new EntityFinderException("Invalid ST_Within expression. Unsupported type " + filterGeometry.getClass().getSimpleName() +
                            " On properties of type " + TPoint.class.getSimpleName() +
                            " only filters of type distance or polygon are supported.");
            } else {
                /**
                 * In all other cases we are using a shape filter.
                 */
                addFilter(createShapeFilter(spec.property().toString(), filterGeometry, ShapeRelation.WITHIN), filterBuilder);
            }
        }

    }



        public void _processSpecification( FilterBuilder filterBuilder,
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
            else if (isPropertyOfTypeTPoint(spec.property())) // property is of type TPoint
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
