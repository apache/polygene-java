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

import org.qi4j.api.geometry.internal.GeometryCollections;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;

import java.util.ArrayList;
import java.util.List;

@Mixins(TFeatureCollection.Mixin.class)
public interface TFeatureCollection extends GeometryCollections
{

    TFeatureCollection of(TFeature... features);
    TFeatureCollection of(List<TFeature> features);

    public abstract class Mixin extends GeometryCollections.Mixin implements TFeatureCollection
    {
        @Structure
        Module module;

        @This
        TFeatureCollection self;

        public TFeatureCollection of(List<TFeature> features)
        {
            of(features.toArray(new TFeature[features.size()]));
            return self;
        }

        public TFeatureCollection of(TFeature... features)
        {
            self.geometryType().set(TGEOMETRY_TYPE.FEATURECOLLECTION);
            init();
            List<TGeometry> l = new ArrayList<>();

            for (TFeature f : features)
            {
                l.add(f);
            }

            if (self.isEmpty())
                self.geometries().set(l);
            else
                self.geometries().get().addAll(l);

            return self;
        }
    }
}
