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
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_DisjointSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.SpatialPredicatesSpecification;
import org.qi4j.api.structure.Module;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.internal.AbstractElasticSearchSpatialFunction;
import org.qi4j.spi.query.EntityFinderException;

import java.util.Map;

import static org.elasticsearch.index.query.FilterBuilders.notFilter;


public class ST_Disjoint extends AbstractElasticSearchSpatialFunction implements PredicateFinderSupport.PredicateSpecification
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


        if (isPropertyOfType(TPoint.class, spec.property()) && isMappedAsGeoShape(spec.property()))
        {
            if (geomOfFilterProperty instanceof TPolygon)
            {

                if (((ST_DisjointSpecification) spec).getDistance() > 0)
                    throw new EntityFinderException("Invalid ST_Disjoint expression. A " + TPolygon.class.getSimpleName() + " can " +
                            "not be combined with distance.");

                TPolygon polygonFilter = (TPolygon) verifyProjection(geomOfFilterProperty);

                GeoPolygonFilterBuilder geoPolygonFilterBuilder = FilterBuilders.geoPolygonFilter(spec.property().toString());

                for (int i = 0; i < polygonFilter.shell().get().getNumPoints(); i++)
                {
                    TPoint point = polygonFilter.shell().get().getPointN(i);
                    geoPolygonFilterBuilder.addPoint(point.x(), point.y());
                }
                addFilter(notFilter(geoPolygonFilterBuilder), filterBuilder);
            } else if (geomOfFilterProperty instanceof TPoint && ((ST_DisjointSpecification) spec).getDistance() > 0)
            {
                double radiusInMeters = convertDistanceToMeters(((ST_DisjointSpecification) spec).getDistance(), ((ST_DisjointSpecification) spec).getUnit());
                TPolygon polygonizedCircleFilter = polygonizeCircle((TPoint) verifyProjection(geomOfFilterProperty), radiusInMeters);
                addFilter(createShapeFilter(spec.property().toString(), polygonizedCircleFilter, ShapeRelation.DISJOINT), filterBuilder);
            }
        } else
        {
            addFilter(createShapeFilter(spec.property().toString(), geomOfFilterProperty, ShapeRelation.DISJOINT), filterBuilder);
        }


    }

    public PredicateFinderSupport.PredicateSpecification support(Module module, ElasticSearchSupport support)
    {
        this.module = module;
        this.support = support;

        return this;
    }
}
