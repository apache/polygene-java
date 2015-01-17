/*
 * Copyright (c) 2014, Jiri Jetmar. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
import org.qi4j.api.structure.Module;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.internal.AbstractElasticSearchSpatialFunction;
import org.qi4j.spi.query.EntityFinderException;

import java.util.Map;


public class ST_Within extends AbstractElasticSearchSpatialFunction implements PredicateFinderSupport.PredicateSpecification
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
                        ((ST_WithinSpecification) spec).getDistance() > 0
                )
        {
            addFilter(createGeoDistanceFilter
                            (
                                    spec.property().toString(),
                                    (TPoint) verifyProjection(geomOfFilterProperty),
                                    ((ST_WithinSpecification) spec).getDistance(),
                                    ((ST_WithinSpecification) spec).getUnit()
                            ),
                    filterBuilder
            );
        }
        /**
         * When the template property is of type TPoint then the filter property has to have an area.
         * Currently only filter geometries of type TPolygon are supported. E.g.
         *
         * TPolygon polygon = TPolygon(module).shell(..)
         * ST_Within (templafeFor(x.class).propertyOfTypeTPoint(), polygon);
         *
         */
        else if (isPropertyOfType(TPoint.class, spec.property()))
        {

            if (isMappedAsGeoPoint(spec.property()))
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
                    if (((ST_WithinSpecification) spec).getDistance() > 0)
                        throw new EntityFinderException("Invalid ST_Within expression. A " + TPolygon.class.getSimpleName() + " can " +
                                "not be combined with distance.");

                    TPolygon polygonFilter = (TPolygon) verifyProjection(geomOfFilterProperty);

                    GeoPolygonFilterBuilder geoPolygonFilterBuilder = FilterBuilders.geoPolygonFilter(spec.property().toString());

                    for (int i = 0; i < polygonFilter.shell().get().getNumPoints(); i++)
                    {
                        TPoint point = polygonFilter.shell().get().getPointN(i);
                        geoPolygonFilterBuilder.addPoint(point.y(), point.x());
                    }
                    addFilter(geoPolygonFilterBuilder, filterBuilder);
                } else
                    throw new EntityFinderException("Invalid ST_Within expression. Unsupported type " + geomOfFilterProperty.getClass().getSimpleName() +
                            " On properties of type " + TPoint.class.getSimpleName() +
                            " only filters of type distance or polygon are supported.");
            } else if (isMappedAsGeoShape(spec.property()))
            {
                if (geomOfFilterProperty instanceof TPolygon)
                {
                    addFilter(createShapeFilter(spec.property().toString(), geomOfFilterProperty, ShapeRelation.WITHIN), filterBuilder);
                } else if (((ST_WithinSpecification) spec).getDistance() > 0)
                {
                    double radiusInMeters = convertDistanceToMeters(((ST_WithinSpecification) spec).getDistance(), ((ST_WithinSpecification) spec).getUnit());
                    TPolygon polygonizedCircleFilter = polygonizeCircle((TPoint) verifyProjection(geomOfFilterProperty), radiusInMeters);
                    addFilter(createShapeFilter(spec.property().toString(), polygonizedCircleFilter, ShapeRelation.WITHIN), filterBuilder);
                }
            }
        } else
        {
            addFilter(createShapeFilter(spec.property().toString(), geomOfFilterProperty, ShapeRelation.WITHIN), filterBuilder);
        }
    }


    public PredicateFinderSupport.PredicateSpecification support(Module module, ElasticSearchSupport support)
    {
        this.module = module;
        this.support = support;

        return this;
    }


}
