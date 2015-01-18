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

package org.qi4j.api.geometry.internal;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.Property;

import java.util.ArrayList;
import java.util.List;


public interface GeometryCollections extends TGeometry
{

    Property<List<TGeometry>> geometries();
    TGeometry getGeometryN(int n);


    int getNumGeometries();

    boolean isEmpty();

    public abstract class Mixin extends TGeometry.Mixin implements GeometryCollections
    {

        @This
        GeometryCollections self;


        protected void init()
        {
            if (self.geometries().get() == null)
            {
                List<TGeometry> geometries = new ArrayList<>();
                self.geometries().set(geometries);
            }
        }

        public boolean isEmpty()
        {
            for (int i = 0; i < self.geometries().get().size(); i++)
            {
                if (!self.geometries().get().get(i).isEmpty())
                {
                    return false;
                }
            }
            return true;
        }

        public int getNumGeometries()
        {
            return self.geometries().get().size();
        }

        public TGeometry getGeometryN(int n)
        {
            return self.geometries().get().get(n);
        }
        public Coordinate[] getCoordinates()
        {
            Coordinate[] coordinates = new Coordinate[self.getNumPoints()];
            int k = -1;
            for (int i = 0; i < self.getNumGeometries(); i++)
            {
                Coordinate[] childCoordinates = self.geometries().get().get(i).getCoordinates();
                for (int j = 0; j < childCoordinates.length; j++)
                {
                    k++;
                    coordinates[k] = childCoordinates[j];
                }
            }
            return coordinates;
        }

        public int getNumPoints()
        {
            int numPoints = 0;
            for (int i = 0; i < self.geometries().get().size(); i++)
            {
                numPoints += self.geometries().get().get(i).getNumPoints();
            }
            return numPoints;
        }

    }

}
