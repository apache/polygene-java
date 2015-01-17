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

package org.qi4j.api.geometry;

import org.qi4j.api.geometry.internal.builders.*;
import org.qi4j.api.structure.Module;


public class TGeometryFactory
{


    public static TCRSBuilder TCrs(Module module)
    {
        return new TCRSBuilder(module);
    }
    public static TPointBuilder TPoint(Module module)
    {
        return new TPointBuilder(module);
    }

    public static TMultiPointBuilder TMultiPoint(Module module)
    {
        return new TMultiPointBuilder(module);
    }
    public static TLinearRingBuilder TLinearRing(Module module)
    {
        return new TLinearRingBuilder(module);
    }
    public static TLineStringBuilder TLineString(Module module)
    {
        return new TLineStringBuilder(module);
    }
    public static TMultiLineStringBuilder TMultiLineString(Module module)
    {
        return new TMultiLineStringBuilder(module);
    }

    public static TPolygonBuilder TPolygon(Module module)
    {
        return new TPolygonBuilder(module);
    }
    public static TMultiPolygonsBuilder TMultiPolygon(Module module)
    {
        return new TMultiPolygonsBuilder(module);
    }
    public static TFeatureBuilder TFeature(Module module)
    {
        return new TFeatureBuilder(module);
    }
    public static TFeatureCollectionBuilder TFeatureCollection(Module module)
    {
        return new TFeatureCollectionBuilder(module);
    }
}
