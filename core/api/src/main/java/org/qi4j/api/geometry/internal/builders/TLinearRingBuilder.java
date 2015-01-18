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
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.structure.Module;


public class TLinearRingBuilder
{

    private Module module;
    private TLinearRing geometry;


    public TLinearRingBuilder(Module module)
    {
        this.module = module;
        geometry = module.newValueBuilder(TLinearRing.class).prototype();
    }


    public TLinearRingBuilder ring(double[][] ring)
    {
        for (double xy[] : ring)
        {
            if (xy.length < 2) return null;
            geometry.yx(xy[0], xy[1]);
        }
        return this;
    }

    public TLinearRingBuilder of(TPoint... points)
    {
        geometry().of(points);
        return this;
    }

    public TLinearRing geometry()
    {

        return geometry;
    }

    public TLinearRing geometry(int srid)
    {
        return geometry();
    }
}
