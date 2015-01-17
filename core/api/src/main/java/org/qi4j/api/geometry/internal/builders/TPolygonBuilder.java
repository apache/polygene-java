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

import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.structure.Module;


public class TPolygonBuilder
{

    private Module module;
    private TPolygon geometry;


    public TPolygonBuilder(Module module)
    {
        this.module = module;
        geometry = module.newValueBuilder(TPolygon.class).prototype();
    }

    public TPolygonBuilder shell(TLinearRing shell)
    {
        geometry.of(shell);
        return this;
    }

    public TPolygonBuilder shell(double[][] shell)
    {
        geometry.of(new TLinearRingBuilder(module).ring(shell).geometry());
        return this;
    }

    public TPolygonBuilder withHoles(TLinearRing... holes)
    {
        geometry.withHoles(holes);
        return this;
    }


    public TPolygon geometry()
    {
        return geometry;
    }

    public TPolygon geometry(String CRS)
    {
        geometry().setCRS(CRS);
        return geometry();
    }
}
