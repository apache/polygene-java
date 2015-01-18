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

package org.qi4j.index.elasticsearch.extensions.spatial.internal;

import com.spatial4j.core.distance.DistanceUtils;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.geo.builders.CircleBuilder;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.*;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.geometry.TUnit;
import org.qi4j.api.geometry.internal.TCircle;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.query.grammar.PropertyFunction;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.SpatialPredicatesSpecification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Classes;
import org.qi4j.functional.Specification;
import org.qi4j.index.elasticsearch.ElasticSearchFinder;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.ElasticSearchSpatialFinder;
import org.qi4j.index.elasticsearch.extensions.spatial.configuration.SpatialConfiguration;
import org.qi4j.index.elasticsearch.extensions.spatial.configuration.SpatialFunctionsSupportMatrix;
import org.qi4j.library.spatial.projections.ProjectionsRegistry;
import org.qi4j.spi.query.EntityFinderException;

import java.lang.reflect.Type;
import java.util.Map;

import static org.qi4j.api.geometry.TGeometryFactory.TPoint;
import static org.qi4j.index.elasticsearch.extensions.spatial.mappings.SpatialIndexMapper.IndexMappingCache;
import static org.qi4j.library.spatial.projections.transformations.TTransformations.Transform;

public abstract class AbstractElasticSearchSpatialFunction
{

    private static final String EPSG_4326 = "EPSG:4326";
    private static final String DefaultSupportedProjection = EPSG_4326;
    private static final double DefaultProjectionConversionPrecisionInMeters = 2.00;

    protected Module module;
    protected ElasticSearchSupport support;

    protected boolean isSupported(SpatialPredicatesSpecification<?> spec, TGeometry geometryOfFilter) throws EntityFinderException
    {
        return SpatialFunctionsSupportMatrix.isSupported
                (
                        spec.getClass(),
                        isPropertyOfType(TPoint.class, spec.property()) ? TPoint.class : TGeometry.class,
                        InternalUtils.classOfGeometry(geometryOfFilter),
                        isMappedAsGeoPoint(spec.property()) ? SpatialConfiguration.INDEXING_METHOD.GEO_POINT : SpatialConfiguration.INDEXING_METHOD.GEO_SHAPE
                );
    }

    protected boolean isValid(SpatialPredicatesSpecification<?> spec) throws EntityFinderException
    {
        if ((spec.param() == null && spec.operator() == null))
            return false;
        else
            return true;
    }


    protected void addFilter(FilterBuilder filter, FilterBuilder into)
    {
        if (into instanceof AndFilterBuilder)
        {
            ((AndFilterBuilder) into).add(filter);
        } else if (into instanceof OrFilterBuilder)
        {
            ((OrFilterBuilder) into).add(filter);
        } else
        {
            throw new UnsupportedOperationException("FilterBuilder is nor an AndFB nor an OrFB, cannot continue.");
        }
    }

    protected TGeometry verifyProjection(TGeometry tGeometry)
    {
        if (new ProjectionsRegistry().getCRS(tGeometry.getCRS()) == null)
            throw new RuntimeException("Project with the CRS Identity " + tGeometry.getCRS() + " is unknown. Supported projections are JJ TODO");

        if (!tGeometry.getCRS().equalsIgnoreCase(DefaultSupportedProjection))
        {
            if (SpatialConfiguration.isFinderProjectionConversionEnabled(support.spatialConfiguration()))
            {
                Transform(module).from(tGeometry).to(DefaultSupportedProjection, DefaultProjectionConversionPrecisionInMeters);
            } else
                throw new RuntimeException("Filter Geometry uses a unsupported Projection and transformation is disabled.");
        }
        return tGeometry; // <- ATTENTION - transmation is done directly on the "reference" to avoid cloning of composites.
    }

    protected boolean isPropertyOfType(Class type, PropertyFunction propertyFunction)
    {
        Type returnType = Classes.typeOf(propertyFunction.accessor());
        Type propertyTypeAsType = GenericPropertyInfo.toPropertyType(returnType);

        Class clazz;

        try
        {
            clazz = Class.forName(propertyTypeAsType.getTypeName());
        } catch (Exception _ex)
        {
            throw new RuntimeException(_ex);
        }

        if (type.isAssignableFrom(clazz))
            return true;
        else
            return false;
    }


    protected boolean isMappedAsGeoPoint(PropertyFunction property)
    {
        return IndexMappingCache.isMappedAsGeoPoint(support.index(), support.entitiesType(), property.toString());
    }

    protected boolean isMappedAsGeoShape(PropertyFunction property)
    {
        return IndexMappingCache.isMappedAsGeoShape(support.index(), support.entitiesType(), property.toString());
    }
    protected boolean isTPoint(TGeometry filterGeometry)
    {
        return TPoint(module).isPoint(filterGeometry);
    }

    protected boolean isMapped(PropertyFunction property)
    {
        return IndexMappingCache.mappingExists(support.index(), support.entitiesType(), property.toString());
    }



    protected GeoShapeFilterBuilder createShapeFilter(String name, TGeometry geometry, ShapeRelation relation)
    {
        return createShapeFilter(name, geometry, relation, 0, null);
    }

    protected GeoDistanceFilterBuilder createGeoDistanceFilter(String name, TPoint tPoint, double distance, TUnit unit)
    {
        // Lat = Y Long = X
        return FilterBuilders.geoDistanceFilter(name)
                .lat(tPoint.y())
                .lon(tPoint.x())
                .distance(distance, convertDistanceUnit(unit));
    }

    protected TPolygon polygonizeCircle(TPoint centre, double radiusInMeters)
    {
        double radiusInDegrees = DistanceUtils.dist2Degrees(radiusInMeters, DistanceUtils.EARTH_MEAN_RADIUS_KM * 1000);
        TCircle tCircle = module.newValueBuilder(TCircle.class).prototype().of(centre, radiusInDegrees);
        return tCircle.polygonize(360);
    }


    private DistanceUnit convertDistanceUnit(TUnit tUnit)
    {
        switch (tUnit)
        {
            case MILLIMETER:
                return DistanceUnit.MILLIMETERS;
            case CENTIMETER:
                return DistanceUnit.CENTIMETERS;
            case METER:
                return DistanceUnit.METERS;
            case KILOMETER:
                return DistanceUnit.KILOMETERS;
            default:
                throw new RuntimeException("Can not convert Units");
        }
    }

    protected double convertDistanceToMeters(double source, TUnit sourceUnit)
    {
        switch (sourceUnit)
        {
            case MILLIMETER:
                return source / 1000;
            case CENTIMETER:
                return source / 100;
            case METER:
                return source;
            case KILOMETER:
                return source * 1000;
            default:
                throw new RuntimeException("Can not convert Units");
        }
    }


    private GeoShapeFilterBuilder createShapeFilter(String name, TGeometry geometry, ShapeRelation relation, double distance, DistanceUnit unit)
    {
        if (geometry instanceof TPoint)
        {
            CircleBuilder circleBuilder = ShapeBuilder.newCircleBuilder();
            circleBuilder.center(((TPoint) geometry).x(), ((TPoint) geometry).y()).radius(distance, unit);
            return FilterBuilders.geoShapeFilter(name, circleBuilder, relation);
        } else if (geometry instanceof TPolygon)
        {
            PolygonBuilder polygonBuilder = ShapeBuilder.newPolygon();
            for (int i = 0; i < ((TPolygon) geometry).shell().get().points().get().size(); i++)
            {
                TPoint point = ((TPolygon) geometry).shell().get().getPointN(i);
                polygonBuilder.point(
                        point.x(), point.y()
                );
            }

            return FilterBuilders.geoShapeFilter(name, polygonBuilder, relation);
        } else
        {
            // TODO
        }

        return null;
    }


    protected TGeometry resolveGeometry(FilterBuilder filterBuilder, Specification<Composite> spec, Module module) throws EntityFinderException
    {

        if (spec instanceof SpatialPredicatesSpecification)
        {
            if (((SpatialPredicatesSpecification) spec).param() != null)
            {
                return ((SpatialPredicatesSpecification) spec).param();
            } else if (((SpatialPredicatesSpecification) spec).operator() != null)
            {

                if (((SpatialPredicatesSpecification) spec).operator() instanceof SpatialConvertSpecification)
                {
                    executeSpecification(filterBuilder, (SpatialPredicatesSpecification) spec, null);
                    return ((SpatialConvertSpecification) ((SpatialPredicatesSpecification) spec).operator()).getGeometry();
                }

                return null;
            }
        }

        return null;
    }

    private void executeSpecification(FilterBuilder filterBuilder,
                                      SpatialPredicatesSpecification<?> spec,
                                      Map<String, Object> variables) throws EntityFinderException
    {


        if (((SpatialPredicatesSpecification) spec).operator() instanceof SpatialConvertSpecification)
        {


            if (ElasticSearchFinder.Mixin.EXTENDED_SPEC_SUPPORTS.get(spec.operator().getClass().getSuperclass()) != null)
            {

                ElasticSearchSpatialFinder.SpatialQuerySpecSupport spatialQuerySpecSupport = ElasticSearchFinder.Mixin.EXTENDED_SPEC_SUPPORTS
                        .get(spec.operator().getClass().getSuperclass()).support(module, support);
                spatialQuerySpecSupport.processSpecification(filterBuilder, spec.operator(), variables);

            } else
            {
                throw new UnsupportedOperationException("Query specification unsupported by Elastic Search "
                        + "(New Query API support missing?): "
                        + spec.getClass() + ": " + spec);
            }
        }
    }


}
