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

import org.qi4j.api.geometry.*;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.query.grammar.PropertyFunction;
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
        } catch (Exception _ex)
        {
            // must not happen
            // TODO, logger
        } finally
        {
            return classOfProperty;
        }
    }

    public static Class<? extends TGeometry> classOfGeometry(TGeometry geometry)
    {
        if (geometry instanceof TPoint) return TPoint.class;
        if (geometry instanceof TMultiPoint) return TMultiPoint.class;
        if (geometry instanceof TLineString) return TLineString.class;
        if (geometry instanceof TPolygon) return TPolygon.class;
        if (geometry instanceof TMultiPolygon) return TMultiPolygon.class;
        if (geometry instanceof TFeature) return TFeature.class;
        if (geometry instanceof TFeatureCollection) return TFeatureCollection.class;
        else return null;
    }

}
