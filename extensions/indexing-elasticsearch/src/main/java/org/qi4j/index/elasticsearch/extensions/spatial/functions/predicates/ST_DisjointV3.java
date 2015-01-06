package org.qi4j.index.elasticsearch.extensions.spatial.functions.predicates;

import com.spatial4j.core.distance.DistanceUtils;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.GeoPolygonFilterBuilder;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.geometry.internal.TCircle;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_DisjointSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_WithinSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.SpatialPredicatesSpecification;
import org.qi4j.index.elasticsearch.extensions.spatial.internal.AbstractElasticSearchSpatialFunction;
import org.qi4j.spi.query.EntityFinderException;

import java.util.Map;

import static org.elasticsearch.index.query.FilterBuilders.andFilter;
import static org.elasticsearch.index.query.FilterBuilders.notFilter;
import static org.elasticsearch.index.query.FilterBuilders.termFilter;

/**
 * Created by jj on 23.12.14.
 */
public class ST_DisjointV3 extends AbstractElasticSearchSpatialFunction implements PredicateFinderSupport.PredicateSpecification
{
    public void processSpecification(FilterBuilder filterBuilder,
                                     SpatialPredicatesSpecification<?> spec,
                                     Map<String, Object> variables)
            throws EntityFinderException
    {
        TGeometry geomOfFilterProperty = resolveGeometry(filterBuilder, spec, module);

        if (!isValid(spec))
            throw new EntityFinderException(spec.getClass() + " expression invalid.");

        if (!isMapped(spec.property()))
            throw new EntityFinderException(spec.getClass() + " expression invalid. No spatial mapping available for property " + spec.property());

        if (!isSupported(spec, geomOfFilterProperty))
            throw new EntityFinderException(spec.getClass() + " expression unsupported by ElasticSearch. Pls specify a supported expression.");




        /**
         * When the geometry used in the ST_Within expression is of type TPoint and a distance is specified, e.g.
         *
         * TPoint point = TPoint(module).x(..).y(..);
         * ST_Within (templateFor(x.class).propertyOfTypeTPoint(), point, 1, TUnit.METER)
         *
         * then a ES GeoDistanceFilter is used.
         *
         */
        if (
                isTPoint(geomOfFilterProperty) &&
                        isMappedAsGeoPoint(spec.property()) &&
                        ((ST_DisjointSpecification) spec).getDistance() > 0
                )
        {
            addFilter(andFilter(notFilter(createGeoDistanceFilter
                            (
                                    spec.property().toString(),
                                    (TPoint) verifyProjection(geomOfFilterProperty),
                                    ((ST_DisjointSpecification) spec).getDistance(),
                                    ((ST_DisjointSpecification) spec).getUnit()
                            )), null),
                    // )termFilter( "point.type", "point" )),
                    filterBuilder
            );
        }




        else if (isPropertyOfType(TPoint.class, spec.property()) && isMappedAsGeoShape(spec.property()))
        {
            if (geomOfFilterProperty instanceof TPolygon)
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

                TPolygon polygonFilter = (TPolygon)verifyProjection(geomOfFilterProperty);

                GeoPolygonFilterBuilder geoPolygonFilterBuilder = FilterBuilders.geoPolygonFilter(spec.property().toString());

                for (int i = 0; i < polygonFilter.shell().get().getNumPoints(); i++) {
                    TPoint point = polygonFilter.shell().get().getPointN(i);
                    geoPolygonFilterBuilder.addPoint(point.x(), point.y());
                }
                addFilter(notFilter(geoPolygonFilterBuilder), filterBuilder); // TODO NOT
            }
            else if (geomOfFilterProperty instanceof TPoint && ((ST_DisjointSpecification)spec).getDistance() > 0 )
            {

                double distanceMeters = convertDistanceToMeters(((ST_DisjointSpecification)spec).getDistance() , ((ST_DisjointSpecification)spec).getUnit());
                System.out.println("Distance in Meters " + distanceMeters);
                double distanceDegrees = DistanceUtils.dist2Degrees(distanceMeters, DistanceUtils.EARTH_MEAN_RADIUS_KM * 1000);
                // This is a special case. We are using polygon substitution to support a circle. ATTENTION - this is just a approximation !!
                TPoint circlePoint = (TPoint)verifyProjection(geomOfFilterProperty);
                TCircle tCircle = module.newValueBuilder(TCircle.class).prototype().of(circlePoint, distanceDegrees);
                TPolygon polygonizedCircleFilter = tCircle.polygonize(360);
                addFilter(createShapeFilter(spec.property().toString(), polygonizedCircleFilter, ShapeRelation.DISJOINT), filterBuilder);
            }


        }
        else
        {
            /**
             * In all other cases we are using a shape filter.
            */
            addFilter(createShapeFilter(spec.property().toString(), geomOfFilterProperty, ShapeRelation.DISJOINT), filterBuilder);
        }


    }
}
