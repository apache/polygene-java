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

package org.qi4j.api.geometry.internal.builders;

import org.qi4j.api.geometry.TMultiPolygon;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.structure.Module;

import java.util.List;


public class TMultiPolygonsBuilder
{

    private Module module;
    private TMultiPolygon geometry;


    public TMultiPolygonsBuilder(Module module)
    {
        this.module = module;
        geometry = module.newValueBuilder(TMultiPolygon.class).prototype();
    }


    public TMultiPolygonsBuilder points(double[][][] points)
    {
        for (double xy[][] : points)
        {
            if (xy.length < 2) return null;
        }
        return this;
    }

    public TMultiPolygonsBuilder of(List<TPolygon> polygons)
    {
        geometry.of(polygons);
        return this;
    }

    public TMultiPolygonsBuilder of(TPolygon... polygons)
    {
        geometry.of(polygons);
        return this;
    }


    public TMultiPolygon geometry()
    {
        return geometry;
    }

    public TMultiPolygon geometry(int srid)
    {
        return geometry();
    }
}
