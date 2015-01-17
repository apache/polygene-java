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

import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * Lat = Y Lon = X
 *
 * For type "Point", each element in the coordinates array is a number representing the point coordinate in one
 * dimension. There must be at least two elements, and may be more. The order of elements must follow x, y, z order
 * (or longitude, latitude, altitude for coordinates in a geographic coordinate reference system). Any number of
 * additional dimensions are allowed, and interpretation and meaning of these coordinates is beyond the scope of
 * this specification.
 */
@Mixins(TPoint.Mixin.class)
public interface TPoint extends TGeometry
{

    Property<List<Coordinate>> coordinates();

    TPoint of(Coordinate... coordinates);

    TPoint of(double x, double y, double z);

    TPoint of();

    TPoint x(double x);

    TPoint y(double y);

    TPoint z(double z);

    double x();

    double y();

    double z();

    Coordinate getCoordinate();

    int compareTo(Object o);


    public abstract class Mixin implements TPoint
    {

        @Structure
        Module module;
        @This
        TPoint self;

        private void init()
        {
            if (self.coordinates().get() == null)
            {

                List<Coordinate> c = new ArrayList<Coordinate>();
                c.add(module.newValueBuilder(Coordinate.class).prototype().x(0).y(0).z(0));
                self.coordinates().set(c);
                self.geometryType().set(TGEOMETRY_TYPE.POINT);
            }
        }

        @Override
        public boolean isEmpty()
        {
            return (self.coordinates() == null) || (self.coordinates().get() == null) || (self.coordinates().get().isEmpty()) ? true : false;
        }


        public TPoint of()
        {
            if (isEmpty())
                return self.of(0.0d, 0.0d, 0.0d);
            else
                return self;
        }
        public TPoint of(double x, double y, double z)
        {
            init();
            self.x(x).y(y).z(z);
            self.geometryType().set(TGEOMETRY_TYPE.POINT);
            return self;
        }

        public TPoint of(Coordinate... coordinates)
        {
            List<Coordinate> c = new ArrayList<Coordinate>();

            for (Coordinate xyzn : coordinates)
            {
                c.add(xyzn);
            }
            self.coordinates().set(c);
            self.geometryType().set(TGEOMETRY_TYPE.POINT);
            return self;
        }

        public TPoint x(double x)
        {
            init();
            self.coordinates().get().get(0).x(x);
            return self;
        }

        public double x()
        {
            return self.coordinates().get().get(0).getOrdinate(Coordinate.X);
        }
        public TPoint y(double y)
        {
            init();
            self.coordinates().get().get(0).y(y);
            return self;
        }
        public double y()
        {
            return self.coordinates().get().get(0).getOrdinate(Coordinate.Y);
        }
        public double z()
        {
            return self.coordinates().get().get(0).getOrdinate(Coordinate.Z);
        }
        public TPoint z(double z)
        {
            init();
            self.coordinates().get().get(0).z(z);
            return self;
        }

        public TPoint of(List<Double> coordinates)
        {
            List<Coordinate> c = new ArrayList<Coordinate>();
            for (Double xyzn : coordinates)
            {
                c.add(module.newValueBuilder(Coordinate.class).prototype().of(xyzn));
            }
            return null;
        }

        @Override
        public Coordinate[] getCoordinates()
        {
            List<Coordinate> coordinates = new ArrayList<>();
            coordinates.add(getCoordinate());
            return coordinates.toArray(new Coordinate[coordinates.size()]);
        }

        public Coordinate getCoordinate()
        {
            return self.coordinates().get().size() != 0 ? self.coordinates().get().get(0) : null;
        }

        public int getNumPoints()
        {
            return isEmpty() ? 0 : 1;
        }

        public int compareTo(Object other)
        {
            TPoint point = (TPoint) other;
            return getCoordinate().compareTo(point.getCoordinate());
        }

    }

}
