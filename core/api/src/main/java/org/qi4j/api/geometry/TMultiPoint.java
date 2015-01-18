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


@Mixins(TMultiPoint.Mixin.class)
public interface TMultiPoint extends GeometryCollections
{
    TMultiPoint of(TPoint... points);
    TMultiPoint of(List<TPoint> points);
    TMultiPoint yx(double y, double x);

    public abstract class Mixin extends GeometryCollections.Mixin implements TMultiPoint
    {
        @This
        TMultiPoint self;
        @Structure
        Module module;

        public TMultiPoint of(List<TPoint> points)
        {
            of(points.toArray(new TPoint[points.size()]));
            return self;
        }
        public TMultiPoint yx(double y, double x)
        {
            of(module.newValueBuilder(TPoint.class).prototype().x(x).y(y));
            return self;
        }
        public TMultiPoint of(TPoint... points)
        {
            self.geometryType().set(TGEOMETRY_TYPE.MULTIPOINT);
            init();
            List<TGeometry> l = new ArrayList<>();
            for (TPoint p : points)
            {
                l.add(p);
            }
            if (self.isEmpty())
                self.geometries().set(l); // points().set(l);
            else
                self.geometries().get().addAll(l);

            return self;
        }
    }
}
