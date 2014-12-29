package org.qi4j.index.elasticsearch.extensions.spatial.configuration;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.query.grammar.extensions.spatial.convert.ST_GeomFromTextSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_DisjointSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_WithinSpecification;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jj on 22.12.14.
 */
public class SpatialFunctionsSupportMatrix
{

    private static Boolean OrderBy = true;
    private static Class<? extends TGeometry> AnyGeometry = TGeometry.class;
    public static Class WKT = ST_GeomFromTextSpecification.class;

    public static enum INDEX_MAPPING_TPOINT_METHOD {TPOINT_AS_GEOPOINT, TPOINT_AS_GEOSHAPE}


    private static final Table<String, SpatialConfiguration.INDEXING_METHOD, ConfigurationEntry> SPATIAL_SUPPORT_MATRIX = HashBasedTable.create();



    static
    {
        // ST_Within
        // supports(enable(ST_WithinSpecification.class), propertyOf(AnyGeometry), filterOf(TPoint.class, TPolygon.class), enable(OrderBy),  INDEX_MAPPING_TPOINT_METHOD.TPOINT_AS_GEOPOINT);
        // supports(enable(ST_WithinSpecification.class), propertyOf(AnyGeometry), filterOf(TPoint.class, TPolygon.class), disable(OrderBy), INDEX_MAPPING_TPOINT_METHOD.TPOINT_AS_GEOSHAPE);

        supports(enable(ST_WithinSpecification.class), propertyOf(AnyGeometry), filterOf(TPoint.class, TPolygon.class), enable(OrderBy), SpatialConfiguration.INDEXING_METHOD.USE_GEO_POINT);
        supports(enable(ST_WithinSpecification.class), propertyOf(AnyGeometry), filterOf(TPoint.class, TPolygon.class), disable(OrderBy), SpatialConfiguration.INDEXING_METHOD.USE_GEO_SHAPE);


        // supports(ST_WithinV2.class, propertyOf(AnyGeometry), filterOf(TPoint.class, TPolygon.class), OrderBy, INDEXING_METHOD.TPOINT_AS_GEOPOINT);
        //supports(ST_WithinV2.class, propertyOf(AnyGeometry), filterOf(TPoint.class, TPolygon.class), disable(OrderBy), INDEXING_METHOD.TPOINT_AS_GEOSHAPE);

        // ST_Disjoint
        supports(disable(ST_DisjointSpecification.class), propertyOf(AnyGeometry), filterOf(AnyGeometry), disable(OrderBy), SpatialConfiguration.INDEXING_METHOD.USE_GEO_POINT);
        supports(enable(ST_DisjointSpecification.class),  propertyOf(AnyGeometry), filterOf(TPoint.class, TPolygon.class), disable(OrderBy), SpatialConfiguration.INDEXING_METHOD.USE_GEO_SHAPE);


        // ST_Disjoint
        // supports(not(ST_Disjoint.class), AnyGeometry, AnyGeometry, OrderBy, INDEXING_METHOD.TPOINT_AS_GEOPOINT);
        // supports(ST_Disjoint.class, TGeometry.class, TGeometry.class, not(OrderBy), INDEXING_METHOD.TPOINT_AS_GEOSHAPE);

        // ST_Intersects
        // supports(not(ST_Intersects.class), AnyGeometry, AnyGeometry, OrderBy, INDEXING_METHOD.TPOINT_AS_GEOPOINT);
        // supports(not(ST_Intersects.class), TGeometry.class, TGeometry.class, not(OrderBy), INDEXING_METHOD.TPOINT_AS_GEOSHAPE);
    }




    private static class ConfigurationEntry
    {
        private SpatialConfiguration.INDEXING_METHOD method;
        private Boolean orderBy;
        private List<Class<? extends  TGeometry>> supportedPropertyGeometries = new LinkedList<>();
        private List<Class<? extends  TGeometry>> supportedFilterGeometries = new LinkedList<>();

        public ConfigurationEntry(Class<? extends  TGeometry>[] geometriesOfProperty, Class<? extends  TGeometry>[] geometriesOfFilter, Boolean orderBy, SpatialConfiguration.INDEXING_METHOD method)
        {
            this.supportedPropertyGeometries = Arrays.asList(geometriesOfProperty);
            this.supportedFilterGeometries   = Arrays.asList(geometriesOfFilter);
            this.orderBy = orderBy;
            this.method  = method;
        }

        public boolean isSupported( Class<? extends  TGeometry> geometryOfProperty, Class<? extends  TGeometry> geometryOfFilter, Boolean orderBy, Boolean verifyOrderBy)
        {
            System.out.println("geometryOfProperty " + geometryOfProperty);
            System.out.println("geometryOfFilter " + geometryOfFilter);
            System.out.println("OrderBy " + orderBy);

            if (supportsProperty(geometryOfProperty) && supportsFilter(geometryOfFilter))
            {
                if (verifyOrderBy) {
                    if (this.orderBy && orderBy) return true;
                    if (this.orderBy && !orderBy) return true;
                    if (!this.orderBy && !orderBy) return true;
                    if (!this.orderBy && orderBy) return false;
                } else return true;
            }
            else return false;

            return false;


/**
            if (supportsProperty(geometryOfProperty) && supportsFilter(geometryOfFilter))
                if (orderBy && (this.orderBy != orderBy)) // <- when we validate against orderBy, the it has to match. Otherwise ignore.
                    return false;
                else
                if (!orderBy && (this.orderBy != orderBy) )
                    return true;
                else
                    return false;
            else
               return false;
 */
        }

        private boolean supportsProperty(Class<? extends  TGeometry> geometryOfProperty)
        {
            if (supportedPropertyGeometries.contains(TGeometry.class) )
                return true;
            else if (supportedPropertyGeometries.contains(geometryOfProperty))
                return true;
            else
                return false;
        }

        private boolean supportsFilter(Class<? extends  TGeometry> geometryOfFilter)
        {
            if (supportedFilterGeometries.contains(TGeometry.class) )
                return true;
            else if (supportedFilterGeometries.contains(geometryOfFilter))
                return true;
            else
                return false;
        }
    }


    public static boolean isSupported(Class expression, Class<? extends  TGeometry> geometryOfProperty,Class<? extends  TGeometry> geometryOfFilter, Boolean orderBy, SpatialConfiguration.INDEXING_METHOD method, Boolean verifyOrderBy )
    {
        System.out.println(SPATIAL_SUPPORT_MATRIX.toString());

        System.out.println("isSupported " +expression + " " +  geometryOfProperty + " " +  geometryOfFilter + " Method " + method);
        System.out.println("Contains " + SPATIAL_SUPPORT_MATRIX.contains(expression.getName(), method) );
        if (SPATIAL_SUPPORT_MATRIX.contains(expression.getName(), method))
            return SPATIAL_SUPPORT_MATRIX.get(expression.getName(), method).isSupported(geometryOfProperty, geometryOfFilter, orderBy, verifyOrderBy);
        else
            return false;
    }


    public static boolean isSupported(Class expression, Class<? extends  TGeometry> geometryOfProperty,Class<? extends  TGeometry> geometryOfFilter, SpatialConfiguration.INDEXING_METHOD method )
    {
        return isSupported(expression, geometryOfProperty, geometryOfFilter, false, method, false);
    }

    private static void supports (Class expression, Class<? extends  TGeometry> geometryOfProperty,Class<? extends  TGeometry> geometryOfFilter, Boolean orderBy, SpatialConfiguration.INDEXING_METHOD method)
    {
        supports
                (
                 expression,
                 (Class < ?extends TGeometry >[])Array.newInstance(geometryOfProperty, 1),
                 (Class<? extends TGeometry>[]) Array.newInstance(geometryOfFilter, 1),
                 orderBy, method
                );
    }

    private static void supports (Class expression, Class<? extends  TGeometry>[] geometriesOfProperty,Class<? extends  TGeometry>[] geometriesOfFilter, Boolean orderBy, SpatialConfiguration.INDEXING_METHOD method)
    {
        SPATIAL_SUPPORT_MATRIX.put(expression.getName(), method, new ConfigurationEntry(geometriesOfProperty, geometriesOfFilter,orderBy, method));
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



    private static Class<? extends  TGeometry>[] filterOf(Class<? extends  TGeometry>... geometryOfFilters)
    {
        return geometryOfFilters;
    }

    private static Class<? extends  TGeometry>[] propertyOf(Class<? extends  TGeometry>... geometryOfProperty)
    {
        return geometryOfProperty;
    }
}
