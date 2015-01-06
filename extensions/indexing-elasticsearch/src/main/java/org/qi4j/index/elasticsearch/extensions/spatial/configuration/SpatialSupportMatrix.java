package org.qi4j.index.elasticsearch.extensions.spatial.configuration;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.query.grammar.ExpressionSpecification;
import org.qi4j.index.elasticsearch.extensions.spatial.functions.predicates.ST_Disjoint;
import org.qi4j.index.elasticsearch.extensions.spatial.functions.predicates.ST_Intersects;
import org.qi4j.index.elasticsearch.extensions.spatial.functions.predicates.ST_WithinV2;
import org.qi4j.index.elasticsearch.extensions.spatial.mappings.cache.MappingsCache;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jj on 22.12.14.
 */
public class SpatialSupportMatrix
{

    private Boolean OrderBy = true;
    private Class<? extends TGeometry> AnyGeometry = TGeometry.class;
    public static enum INDEX_MAPPING_POINT_METHOD {AS_GEO_POINT, AS_GEO_SHAPE}

    private static final Table<Class, INDEX_MAPPING_POINT_METHOD, ConfigurationEntry> SPATIAL_SUPPORT_MATRIX = HashBasedTable.create();



    {
        // ST_Within
        supports(ST_WithinV2.class, propertyOf(AnyGeometry), filterOf(TPoint.class, TPolygon.class), OrderBy, INDEX_MAPPING_POINT_METHOD.AS_GEO_POINT);
        supports(ST_WithinV2.class, propertyOf(AnyGeometry), filterOf(TPoint.class, TPolygon.class), not(OrderBy), INDEX_MAPPING_POINT_METHOD.AS_GEO_SHAPE);

        // ST_Disjoint
        supports(not(ST_Disjoint.class), AnyGeometry, AnyGeometry, OrderBy, INDEX_MAPPING_POINT_METHOD.AS_GEO_POINT);
        supports(ST_Disjoint.class, TGeometry.class, TGeometry.class, not(OrderBy), INDEX_MAPPING_POINT_METHOD.AS_GEO_SHAPE);

        // ST_Intersects
        supports(not(ST_Intersects.class), AnyGeometry, AnyGeometry, OrderBy, INDEX_MAPPING_POINT_METHOD.AS_GEO_POINT);
        supports(not(ST_Intersects.class), TGeometry.class, TGeometry.class, not(OrderBy), INDEX_MAPPING_POINT_METHOD.AS_GEO_SHAPE);
    }




    {
        supports(ST_WithinV2.class, TPoint.class, not(TPoint.class), OrderBy, INDEX_MAPPING_POINT_METHOD.AS_GEO_SHAPE);
    }

    private class ConfigurationEntry
    {
        private INDEX_MAPPING_POINT_METHOD method;
        private Boolean orderBy;
        private List<Class<? extends  TGeometry>> supportedPropertyGeometries = new LinkedList<>();
        private List<Class<? extends  TGeometry>> supportedFilterGeometries = new LinkedList<>();

        public ConfigurationEntry(Class<? extends  TGeometry>[] geometriesOfProperty, Class<? extends  TGeometry>[] geometriesOfFilter, Boolean orderBy, INDEX_MAPPING_POINT_METHOD method)
        {
            this.supportedPropertyGeometries = Arrays.asList(geometriesOfProperty);
            this.supportedFilterGeometries   = Arrays.asList(geometriesOfFilter);
            this.orderBy = orderBy;
            this.method  = method;
        }

        public boolean isSupported( Class<? extends  TGeometry> geometryOfProperty, Class<? extends  TGeometry> geometryOfFilter, Boolean orderBy)
        {
            return false;
        }
    }


    public boolean isSupported(Class expression, Class<? extends  TGeometry> geometryOfProperty,Class<? extends  TGeometry> geometryOfFilter, Boolean orderBy, INDEX_MAPPING_POINT_METHOD method )
    {
        if (SPATIAL_SUPPORT_MATRIX.contains(expression, method))
            return SPATIAL_SUPPORT_MATRIX.get(expression, method).isSupported(geometryOfProperty, geometryOfFilter, orderBy);
        else
            return false;
    }

    private void supports (Class expression, Class<? extends  TGeometry> geometryOfProperty,Class<? extends  TGeometry> geometryOfFilter, Boolean orderBy, INDEX_MAPPING_POINT_METHOD method)
    {
        supports
                (
                 expression,
                 (Class < ?extends TGeometry >[])Array.newInstance(geometryOfProperty, 1),
                 (Class<? extends TGeometry>[]) Array.newInstance(geometryOfFilter, 1),
                 orderBy, method
                );
    }

    private void supports (Class expression, Class<? extends  TGeometry>[] geometriesOfProperty,Class<? extends  TGeometry>[] geometriesOfFilter, Boolean orderBy, INDEX_MAPPING_POINT_METHOD method)
    {
        SPATIAL_SUPPORT_MATRIX.put(expression, method, new ConfigurationEntry(geometriesOfProperty, geometriesOfFilter,orderBy, method));
    }



    private Class not(Class clazz)
    {
        return Object.class;
    }

    private Boolean not(Boolean bool)
    {
        return false;
    }


    private Class<? extends  TGeometry>[] filterOf(Class<? extends  TGeometry>... geometryOfFilters)
    {
        return geometryOfFilters;
    }

    private Class<? extends  TGeometry>[] propertyOf(Class<? extends  TGeometry>... geometryOfProperty)
    {
        return geometryOfProperty;
    }
}
