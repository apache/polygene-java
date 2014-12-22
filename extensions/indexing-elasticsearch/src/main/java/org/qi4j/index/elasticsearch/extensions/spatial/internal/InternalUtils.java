package org.qi4j.index.elasticsearch.extensions.spatial.internal;

import org.qi4j.api.geometry.*;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.query.grammar.PropertyFunction;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.SpatialPredicatesSpecification;
import org.qi4j.api.util.Classes;

import java.lang.reflect.Type;

/**
 * Ugly - not proud on this... How to make it better ?
 */
public class InternalUtils
{

    public static Class classOfPropertyType(PropertyFunction propertyFunction)
    {
        Type typeOfProperty = GenericPropertyInfo.toPropertyType(Classes.typeOf(propertyFunction.accessor()));
        Class classOfProperty = null;
        try
        {
            classOfProperty = Class.forName(typeOfProperty.getTypeName());
        } catch(Exception _ex)
        {
            // must not happen
            // TODO, logger
        }
        finally
        {
            return classOfProperty;
        }
    }

    public static Class<? extends TGeometry> classOfGeometry(TGeometry geometry)
    {
        if (geometry instanceof TPoint)                 return TPoint.class;
        if (geometry instanceof TMultiPoint)            return TMultiPoint.class;
        if (geometry instanceof TLineString)            return TLineString.class;
        if (geometry instanceof TPolygon)               return TPolygon.class;
        if (geometry instanceof TMultiPolygon)          return TMultiPolygon.class;
        if (geometry instanceof TFeature)               return TFeature.class;
        if (geometry instanceof TFeatureCollection)     return TFeatureCollection.class;
        else return null;
    }

}
