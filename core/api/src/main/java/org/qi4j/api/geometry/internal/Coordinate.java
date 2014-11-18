package org.qi4j.api.geometry.internal;

import org.qi4j.api.common.Optional;
import org.qi4j.api.geometry.TGeomRoot;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one single n-Dimensional coordinate in the n-Dimensional "space"
 */

@Mixins( Coordinate.Mixin.class )
public interface Coordinate extends Comparable, ValueComposite, TGeomRoot {

    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;

    // single coordinate store
    @Optional
    Property<List<Double>> coordinate();

    Coordinate of(double... coordinates);
    double getOrdinate(int ordinateIndex);
    int compareTo(Object o);
    double[] source();

    Coordinate X(double x);
    Coordinate Y(double y);
    Coordinate Z(double z);


    public abstract class Mixin implements Coordinate
    {

        List<Double> EMPTY = new ArrayList<Double>(X + Y + Z);



        @This
        Coordinate self;

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


        public Coordinate X(double x)
        {

            EMPTY.add(new Double(0.0));
            EMPTY.add(new Double(0.0));
            EMPTY.add(new Double(0.0));

            if (self.coordinate().get() == null) self.coordinate().set(EMPTY);

            if (!Double.isNaN(x) && !Double.isInfinite(x))
            {
                System.out.println(coordinate().get());
                self.coordinate().get().set(X, x);
            }
            return self;
        }

        public Coordinate Y(double y)
        {
            EMPTY.add(new Double(0.0));
            EMPTY.add(new Double(0.0));
            EMPTY.add(new Double(0.0));

            if (self.coordinate().get() == null) self.coordinate().set(EMPTY);

            if (!Double.isNaN(y) && !Double.isInfinite(y))
            {
                self.coordinate().get().set(Y,y);
            }
            return self;
        }

        public Coordinate Z(double z)
        {

            EMPTY.add(new Double(0.0));
            EMPTY.add(new Double(0.0));
            EMPTY.add(new Double(0.0));

            if (self.coordinate().get() == null) self.coordinate().set(EMPTY);

            if (!Double.isNaN(z) && !Double.isInfinite(z))
            {
                self.coordinate().get().set(Z,z);
            }
            return self;
        }

        public int compareTo(Object o)
        {
            Coordinate other = (Coordinate)o;

            if (self.coordinate().get().get(X) < other.coordinate().get().get(X)) return -1;
            if (self.coordinate().get().get(X) > other.coordinate().get().get(X)) return 1;
            if (self.coordinate().get().get(Y) < other.coordinate().get().get(Y)) return -1;
            if (self.coordinate().get().get(Y) > other.coordinate().get().get(Y)) return 1;
            return 0;
        }

        public double getOrdinate(int ordinateIndex)
        {
            switch (ordinateIndex) {
                case X: return self.coordinate().get().get(X);
                case Y: return self.coordinate().get().get(Y);
                case Z: return self.coordinate().get().get(Z);
            }
            throw new IllegalArgumentException("Invalid ordinate index: " + ordinateIndex);
        }

        public double[] source()
        {
            double [] values = new double[3];
            values[X] = getOrdinate(X);
            values[Y] = getOrdinate(Y);
            values[Z] = getOrdinate(Z);

            return values;
        }
    }
}
