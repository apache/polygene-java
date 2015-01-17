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

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one single n-Dimensional coordinate in the n-Dimensional "space"
 */

@Mixins(Coordinate.Mixin.class)
public interface Coordinate extends Comparable, ValueComposite
{

    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;

    // single coordinate store
    @Optional
    Property<List<Double>> coordinate();

    Coordinate of();
    Coordinate of(double x, double y, double z);
    Coordinate of(double... coordinates);

    Coordinate x(double x);
    Coordinate y(double y);
    Coordinate z(double z);

    double x();
    double y();
    double z();

    double getOrdinate(int ordinateIndex);
    int compareTo(Object o);
    double[] source();

    public abstract class Mixin implements Coordinate
    {

        List<Double> EMPTY = new ArrayList<>(X + Y + Z);
        @This
        Coordinate self;

        private void init()
        {
            if (isEmpty())
            {
                EMPTY.add(new Double(0.0));
                EMPTY.add(new Double(0.0));
                EMPTY.add(new Double(0.0));

                self.coordinate().set(EMPTY);
            }
        }

        private boolean isEmpty()
        {
            return (self.coordinate() == null) || (self.coordinate().get() == null) || (self.coordinate().get().isEmpty()) ? true : false;
        }

        public Coordinate of()
        {
            return self.of(0.0d, 0.0d, 0.0d);
        }

        public Coordinate of(double x, double y, double z)
        {
            init();
            self.x(x);
            self.y(y);
            self.z(z);
            return self;
        }


        public double x()
        {
            return getOrdinate(X);
        }
        public double y()
        {
            return getOrdinate(Y);
        }
        public double z()
        {
            return getOrdinate(Z);
        }

        public Coordinate x(double x)
        {
            init();
            if (!Double.isNaN(x) && !Double.isInfinite(x))
            {
                self.coordinate().get().set(X, x);
            }
            return self;
        }

        public Coordinate y(double y)
        {
            init();
            if (!Double.isNaN(y) && !Double.isInfinite(y))
            {
                self.coordinate().get().set(Y, y);
            }
            return self;
        }

        public Coordinate z(double z)
        {
            init();
            if (!Double.isNaN(z) && !Double.isInfinite(z))
            {
                self.coordinate().get().set(Z, z);
            }
            return self;
        }

        public int compareTo(Object o)
        {
            Coordinate other = (Coordinate) o;
            if (self.coordinate().get().get(X) < other.coordinate().get().get(X)) return -1;
            if (self.coordinate().get().get(X) > other.coordinate().get().get(X)) return 1;
            if (self.coordinate().get().get(Y) < other.coordinate().get().get(Y)) return -1;
            if (self.coordinate().get().get(Y) > other.coordinate().get().get(Y)) return 1;
            return 0;
        }

        public double getOrdinate(int ordinateIndex)
        {
            switch (ordinateIndex)
            {
                case X:
                    return self.coordinate().get().get(X);
                case Y:
                    return self.coordinate().get().get(Y);
                case Z:
                    return self.coordinate().get().get(Z);
            }
            throw new IllegalArgumentException("Invalid ordinate index: " + ordinateIndex);
        }

        public double[] source()
        {
            double[] source = new double[X + Y + Z];
            source[X] = getOrdinate(X);
            source[Y] = getOrdinate(Y);
            source[Z] = getOrdinate(Z);
            return source;
        }


        public Coordinate of(double... coordinates)
        {
            List<Double> l = new ArrayList<Double>(coordinates.length);
            for (double xyzn : coordinates)
            {
                // only values that makes "sense"
                if (!Double.isNaN(xyzn) && !Double.isInfinite(xyzn))
                    l.add(new Double(xyzn));
            }
            self.coordinate().set(l);
            return self;
        }


    }
}
