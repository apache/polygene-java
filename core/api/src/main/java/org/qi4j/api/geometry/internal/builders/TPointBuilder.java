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

import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.structure.Module;


public class TPointBuilder
{

    private Module module;
    private TPoint geometry;


    public TPointBuilder(Module module)
    {
        this.module = module;
        geometry = module.newValueBuilder(TPoint.class).prototype();
    }

    public TPointBuilder x(double x)
    {
        geometry.x(x);
        return this;
    }

    public TPointBuilder y(double y)
    {
        geometry.y(y);
        return this;
    }

    public TPointBuilder z(double u)
    {
        geometry.z(u);
        return this;
    }


    public TPointBuilder lat(double lat)
    {
        geometry.y(lat);
        return this;
    }

    public TPointBuilder lon(double lon)
    {
        geometry.x(lon);
        return this;
    }

    public TPointBuilder alt(double alt)
    {
        geometry.z(alt);
        return this;
    }


    public boolean isPoint(TGeometry tGeometry)
    {
        return tGeometry instanceof TPoint ? true : false;
    }

    public TPoint geometry()
    {
        return geometry;
    }

    public TPoint geometry(String CRS)
    {
        geometry().setCRS(CRS);
        return geometry();
    }
}
