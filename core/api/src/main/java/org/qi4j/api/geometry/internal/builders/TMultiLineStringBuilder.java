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
import org.qi4j.api.geometry.TMultiLineString;
import org.qi4j.api.structure.Module;

import java.util.List;

public class TMultiLineStringBuilder
{

    private Module module;
    private TMultiLineString geometry;


    public TMultiLineStringBuilder(Module module)
    {
        this.module = module;
        geometry = module.newValueBuilder(TMultiLineString.class).prototype();
    }


    public TMultiLineStringBuilder points(double[][][] points)
    {
        for (double xy[][] : points)
        {
            if (xy.length < 2) return null;
        }
        return this;
    }

    public TMultiLineStringBuilder of(List<TLineString> lines)
    {
        geometry.of(lines);
        return this;
    }

    public TMultiLineStringBuilder of(TLineString... lines)
    {
        geometry.of(lines);
        return this;
    }


    public TMultiLineString geometry()
    {
        return geometry;
    }

    public TMultiLineString geometry(int srid)
    {
        return geometry();
    }
}
