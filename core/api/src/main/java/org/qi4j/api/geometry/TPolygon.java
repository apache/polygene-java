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

import org.qi4j.api.common.Optional;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.geometry.internal.TShape;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;

import java.util.ArrayList;
import java.util.List;

@Mixins(TPolygon.Mixin.class)
public interface TPolygon extends TShape, TGeometry
{

    Property<TLinearRing> shell();

    @Optional
    Property<List<TLinearRing>> holes();

    TPolygon of(TLinearRing shell);
    TPolygon of(TLinearRing shell, @Optional TLinearRing... holes);
    TPolygon withHole(TLinearRing hole);
    TPolygon withHoles(@Optional TLinearRing... holes);
    boolean isEmpty();

    public abstract class Mixin implements TPolygon
    {
        @Structure
        Module module;

        @This
        TPolygon self;

        private void init()
        {
            if (self.holes().get() == null)
            {
                List<TLinearRing> ring = new ArrayList<>();
                self.holes().set(ring);
                self.geometryType().set(TGEOMETRY_TYPE.POINT);
            }
        }

        public TPolygon of(TLinearRing shell)
        {
            return of(shell, null);
        }

        public TPolygon of(TLinearRing shell, TLinearRing... holes)
        {
            init();
            if (shell != null)
            {
                self.shell().set(shell);
            }
            withHoles(holes);
            self.geometryType().set(TGEOMETRY_TYPE.POLYGON);
            return self;
        }

        public TPolygon withHole(TLinearRing hole)
        {
            if (hole != null) self.holes().get().add(hole);
            return self;
        }
        public TPolygon withHoles(TLinearRing... holes)
        {
            if (holes != null && holes.length != 0)
            {
                for (TLinearRing hole : holes)
                    withHole(hole);
            }
            return self;
        }

        @Override
        public Coordinate[] getCoordinates()
        {
            if (isEmpty())
            {
                return new Coordinate[]{};
            }
            Coordinate[] coordinates = new Coordinate[getNumPoints()];
            int k = -1;
            Coordinate[] shellCoordinates = self.shell().get().getCoordinates();
            for (int x = 0; x < shellCoordinates.length; x++)
            {
                k++;
                coordinates[k] = shellCoordinates[x];
            }
            for (int i = 0; i < self.holes().get().size(); i++)
            {
                Coordinate[] childCoordinates = self.holes().get().get(i).getCoordinates();
                for (int j = 0; j < childCoordinates.length; j++)
                {
                    k++;
                    coordinates[k] = childCoordinates[j];
                }
            }
            return coordinates;
        }

        public boolean isEmpty()
        {
            return (self.shell() == null) || (self.shell().get() == null) || (self.shell().get().isEmpty()) ? true : false;
        }

        public int getNumPoints()
        {

            int numPoints = self.shell().get().getNumPoints();
            for (int i = 0; i < self.holes().get().size(); i++)
            {
                numPoints += self.holes().get().get(i).getNumPoints();
            }
            return numPoints;
        }
    }
}
