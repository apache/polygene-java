package org.qi4j.index.elasticsearch.extensions.spatial.functions.predicates;

import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.index.query.*;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_DisjointSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.SpatialPredicatesSpecification;
import org.qi4j.index.elasticsearch.extensions.spatial.internal.AbstractElasticSearchSpatialFunction;
import org.qi4j.spi.query.EntityFinderException;

import java.util.Map;

import static org.elasticsearch.index.query.FilterBuilders.termFilter;
import static org.elasticsearch.index.query.FilterBuilders.notFilter;
import static org.qi4j.index.elasticsearch.extensions.spatial.mappings.old.ElasticSearchMappingsHelper.Mappings;

/**
 * Created by jj on 19.11.14.
 */
public class ST_Disjoint extends AbstractElasticSearchSpatialFunction implements PredicateFinderSupport.PredicateSpecification {


    public void processSpecification( FilterBuilder filterBuilder,
                                      SpatialPredicatesSpecification<?> spec,
                                      Map<String, Object> variables )
                                                        throws EntityFinderException
    {
        if ((spec.value() == null && spec.operator() == null))
            throw new EntityFinderException("Insufficient data provided. ST_Disjoint specification requires " +
                    "valid filter geometry of type " + TGeometry.class.getSimpleName());

        System.out.println("ST_Disjoint");

        TGeometry filterGeometry = resolveGeometry(filterBuilder,spec, module);

        /**
         * When the geometry used in the ST_Disjoint expression is of type TPoint and a distance is specified, e.g.
         *
         * TPoint filterGeometry = TPoint(module).x(..).y(..);
         * ST_Disjoint (templateFor(x.class).propertyOfTypeTPoint(), filterGeometry, 1, TUnit.METER)
         *
         * then a ES GeoDistanceFilter together with a negotiation (notFilter) is used.
         *
         */


           if (Mappings(support).onIndex(support.index()).andType(support.entitiesType()).isGeoPoint(spec.property().toString()))
                    throw new EntityFinderException("ST_Disjoint can not be used on values with geo_point mappings. Pls use geo_shape mappings.");


        /**
         * When the template property is of type TPoint then the filter property has to have an area.
         * Currently only filter geometries of type TPolygon are supported. E.g.
         *
         * TPolygon filterGeometry = TPolygon(module).shell(..)
         * ST_Within (templafeFor(x.class).propertyOfTypeTPoint(), filterGeometry);
         *
         *
         */
        else if (isPropertyOfTypeTPoint(spec.property()))
        {

            if (filterGeometry instanceof TPolygon)
            {
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
                if (((ST_DisjointSpecification)spec).getDistance() > 0)
                    throw new EntityFinderException("Invalid ST_Disjoint expression. A " + TPolygon.class.getSimpleName() + " can " +
                            "not be combined with distance.");

                TPolygon polygonFilter = (TPolygon)verifyProjection(filterGeometry);

                GeoPolygonFilterBuilder geoPolygonFilterBuilder = FilterBuilders.geoPolygonFilter(spec.property().toString());

                for (int i = 0; i < polygonFilter.shell().get().getNumPoints(); i++) {
                    TPoint point = polygonFilter.shell().get().getPointN(i);
                    geoPolygonFilterBuilder.addPoint(point.x(), point.y());
                }
                addFilter(notFilter(geoPolygonFilterBuilder), filterBuilder); // TODO NOT
            }
            else
                throw new EntityFinderException("Invalid ST_Disjoint expression. Unsupported type " +  filterGeometry.getClass().getSimpleName() +
                        " On properties of type " +  TPoint.class.getSimpleName() +
                        " only filters of type distance or polygon are supported.");
        }
        else
        {
            /**
             * In all other cases we are using a shape filter.
             */
            addFilter(createShapeFilter(spec.property().toString(), filterGeometry, ShapeRelation.DISJOINT), filterBuilder);
        }


    }














    public void _processSpecification( FilterBuilder filterBuilder,
                                      SpatialPredicatesSpecification<?> spec,
                                      Map<String, Object> variables )
            throws EntityFinderException
    {
        if (spec.value() == null && spec.operator() == null)
            throw new UnsupportedOperationException("ST_Within specification ...todo :"
                    + spec.getClass() + ": " + spec);

        System.out.println("ST_DisjointFunction Spec");

        try {
            TGeometry geometry = resolveGeometry(filterBuilder,spec, module);
            addFilter(createShapeFilter(spec.property().toString(), geometry, ShapeRelation.DISJOINT), filterBuilder);
        } catch(Exception _ex) {
            _ex.printStackTrace();
        }
    }

}
