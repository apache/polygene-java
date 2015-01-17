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

import org.qi4j.api.geometry.TLineString;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.structure.Module;


public class TLineStringBuilder
{

    private Module module;
    private TLineString geometry;


    public TLineStringBuilder(Module module)
    {
        this.module = module;
        geometry = module.newValueBuilder(TLineString.class).prototype();
    }


    public TLineStringBuilder points(double[][] points)
    {
        for (double yx[] : points)
        {
            if (yx.length < 2) return null;
            geometry.yx(yx[0], yx[1]);
        }
        return this;
    }

    public TLineStringBuilder of(TPoint... points)
    {
        geometry().of(points);
        return this;
    }

    public TLineStringBuilder of()
    {
        geometry().of();
        return this;
    }


    public TLineString geometry()
    {
        return geometry;
    }

    public TLineString geometry(int srid)
    {
        return geometry();
    }
}
