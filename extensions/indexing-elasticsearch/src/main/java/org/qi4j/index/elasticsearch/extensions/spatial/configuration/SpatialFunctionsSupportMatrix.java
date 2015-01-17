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

package org.qi4j.index.elasticsearch.extensions.spatial.configuration;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_DisjointSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_IntersectsSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_WithinSpecification;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SpatialFunctionsSupportMatrix
{
    private static final Table<String, SpatialConfiguration.INDEXING_METHOD, ConfigurationEntry> SPATIAL_SUPPORT_MATRIX = HashBasedTable.create();
    private static Boolean OrderBy = true;
    private static Class<? extends TGeometry> AnyGeometry = TGeometry.class;

    static
    {
        // ST_Within
        supports(enable(ST_WithinSpecification.class), propertyOf(AnyGeometry), filterOf(TPoint.class, TPolygon.class), enable(OrderBy), SpatialConfiguration.INDEXING_METHOD.GEO_POINT);
        supports(enable(ST_WithinSpecification.class), propertyOf(AnyGeometry), filterOf(TPoint.class, TPolygon.class), disable(OrderBy), SpatialConfiguration.INDEXING_METHOD.GEO_SHAPE);

        // ST_Disjoint
        supports(disable(ST_DisjointSpecification.class), propertyOf(AnyGeometry), filterOf(TPoint.class, TPolygon.class), enable(OrderBy), SpatialConfiguration.INDEXING_METHOD.GEO_POINT);
        supports(enable(ST_DisjointSpecification.class), propertyOf(AnyGeometry), filterOf(TPoint.class, TPolygon.class), disable(OrderBy), SpatialConfiguration.INDEXING_METHOD.GEO_SHAPE);


        // ST_Intersects
        supports(disable(ST_IntersectsSpecification.class), propertyOf(AnyGeometry), filterOf(TPoint.class, TPolygon.class), enable(OrderBy), SpatialConfiguration.INDEXING_METHOD.GEO_POINT);
        supports(enable(ST_IntersectsSpecification.class), propertyOf(AnyGeometry), filterOf(TPoint.class, TPolygon.class), disable(OrderBy), SpatialConfiguration.INDEXING_METHOD.GEO_SHAPE);
    }


    public static boolean isSupported(Class expression, Class<? extends TGeometry> geometryOfProperty, Class<? extends TGeometry> geometryOfFilter, Boolean orderBy, SpatialConfiguration.INDEXING_METHOD method, Boolean verifyOrderBy)
    {

        if (SPATIAL_SUPPORT_MATRIX.contains(expression.getName(), method))
            return SPATIAL_SUPPORT_MATRIX.get(expression.getName(), method).isSupported(geometryOfProperty, geometryOfFilter, orderBy, verifyOrderBy);
        else
            return false;
    }

    public static boolean isSupported(Class expression, Class<? extends TGeometry> geometryOfProperty, Class<? extends TGeometry> geometryOfFilter, SpatialConfiguration.INDEXING_METHOD method)
    {
        return isSupported(expression, geometryOfProperty, geometryOfFilter, false, method, false);
    }

    private static void supports(Class expression, Class<? extends TGeometry> geometryOfProperty, Class<? extends TGeometry> geometryOfFilter, Boolean orderBy, SpatialConfiguration.INDEXING_METHOD method)
    {
        supports
                (
                        expression,
                        (Class<? extends TGeometry>[]) Array.newInstance(geometryOfProperty, 1),
                        (Class<? extends TGeometry>[]) Array.newInstance(geometryOfFilter, 1),
                        orderBy, method
                );
    }

    private static void supports(Class expression, Class<? extends TGeometry>[] geometriesOfProperty, Class<? extends TGeometry>[] geometriesOfFilter, Boolean orderBy, SpatialConfiguration.INDEXING_METHOD method)
    {
        SPATIAL_SUPPORT_MATRIX.put(expression.getName(), method, new ConfigurationEntry(geometriesOfProperty, geometriesOfFilter, orderBy, method));
    }

    private static Class disable(Class clazz)
    {
        return Object.class;
    }

    private static Class enable(Class clazz)
    {
        return clazz;
    }

    private static Boolean disable(Boolean bool)
    {
        return false;
    }

    private static Boolean enable(Boolean bool)
    {
        return true;
    }

    private static Class<? extends TGeometry>[] filterOf(Class<? extends TGeometry>... geometryOfFilters)
    {
        return geometryOfFilters;
    }

    private static Class<? extends TGeometry>[] propertyOf(Class<? extends TGeometry>... geometryOfProperty)
    {
        return geometryOfProperty;
    }

    private static class ConfigurationEntry
    {
        private SpatialConfiguration.INDEXING_METHOD method;
        private Boolean orderBy;
        private List<Class<? extends TGeometry>> supportedPropertyGeometries = new LinkedList<>();
        private List<Class<? extends TGeometry>> supportedFilterGeometries = new LinkedList<>();

        public ConfigurationEntry(Class<? extends TGeometry>[] geometriesOfProperty, Class<? extends TGeometry>[] geometriesOfFilter, Boolean orderBy, SpatialConfiguration.INDEXING_METHOD method)
        {
            this.supportedPropertyGeometries = Arrays.asList(geometriesOfProperty);
            this.supportedFilterGeometries = Arrays.asList(geometriesOfFilter);
            this.orderBy = orderBy;
            this.method = method;
        }

        public boolean isSupported(Class<? extends TGeometry> geometryOfProperty, Class<? extends TGeometry> geometryOfFilter, Boolean orderBy, Boolean verifyOrderBy)
        {

            if (supportsProperty(geometryOfProperty) && supportsFilter(geometryOfFilter))
            {
                if (verifyOrderBy)
                {
                    if (this.orderBy && orderBy) return true;
                    if (this.orderBy && !orderBy) return true;
                    if (!this.orderBy && !orderBy) return true;
                    if (!this.orderBy && orderBy) return false;
                } else return true;
            } else return false;
            return false;
        }

        private boolean supportsProperty(Class<? extends TGeometry> geometryOfProperty)
        {
            if (supportedPropertyGeometries.contains(TGeometry.class))
                return true;
            else if (supportedPropertyGeometries.contains(geometryOfProperty))
                return true;
            else
                return false;
        }

        private boolean supportsFilter(Class<? extends TGeometry> geometryOfFilter)
        {
            if (supportedFilterGeometries.contains(TGeometry.class))
                return true;
            else if (supportedFilterGeometries.contains(geometryOfFilter))
                return true;
            else
                return false;
        }
    }
}
