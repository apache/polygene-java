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

import org.qi4j.api.geometry.TFeature;
import org.qi4j.api.geometry.TFeatureCollection;
import org.qi4j.api.structure.Module;

import java.util.List;

public class TFeatureCollectionBuilder
{

    private Module module;
    private TFeatureCollection geometry;


    public TFeatureCollectionBuilder(Module module)
    {
        this.module = module;
        geometry = module.newValueBuilder(TFeatureCollection.class).prototype();
    }

    public TFeatureCollectionBuilder of(List<TFeature> features)
    {
        geometry.of(features);
        return this;
    }

    public TFeatureCollectionBuilder of(TFeature... features)
    {
        geometry.of(features);
        return this;
    }

    public TFeatureCollection geometry()
    {
        return geometry;
    }

    public TFeatureCollection geometry(int srid)
    {
        return geometry();
    }
}
