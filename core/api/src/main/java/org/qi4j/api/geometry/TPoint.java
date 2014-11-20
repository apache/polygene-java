package org.qi4j.api.geometry;

import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;

import java.util.ArrayList;
import java.util.List;


@Mixins( TPoint.Mixin.class )
public interface TPoint extends TGeometry {


     public static final int _2D = 2;
     public static final int _3D = 3;


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

    double[] source();
    int compareTo(Object o);



    public abstract class Mixin implements TPoint
    {

        private void init()
        {

            if (self.coordinates().get() == null) {

                List<Coordinate> c = new ArrayList<Coordinate>();
                c.add(module.newValueBuilder(Coordinate.class).prototype().x(0).y(0).z(0));
                self.coordinates().set(c);
                self.type().set(TGEOMETRY.POINT);
            }
        }

        @Structure
        Module module;

        @This
        TPoint self;

        private boolean isEmpty()
        {
            return (self.coordinates() == null) || (self.coordinates().get() == null) || (self.coordinates().get().isEmpty()) ? true : false;

        }


        public TPoint of()
        {
            if (isEmpty() )
                return self.of(0.0d, 0.0d, 0.0d);
            else
                return self;
        }

        public TPoint of(double x, double y, double z)
        {
            init();
            self.x(x); self.y(y); self.z(z);
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
            self.type().set(TGEOMETRY.POINT);

            return self;
        }

        public TPoint x(double x) {
            init();

           // self.coordinates().get().get(0).module.newValueBuilder(Coordinate.class).prototype().X(x));

            self.coordinates().get().get(0).x(x);

            return self;
        }

        public double x() {

            return self.coordinates().get().get(0).getOrdinate(Coordinate.X);
        }

        public double y() {
            return self.coordinates().get().get(0).getOrdinate(Coordinate.Y);
        }

        public double z() {
            return self.coordinates().get().get(0).getOrdinate(Coordinate.Z);
        }

        public TPoint y(double y) {
            init();
            self.coordinates().get().get(0).y(y);

            return self;
        }

        public TPoint z(double z) {
            init();
            self.coordinates().get().get(0).z(z);

            return self;
        }

        public TPoint of(List<Double> coordinates)
        {

            List<Coordinate> c = new ArrayList<Coordinate>();

            for (Double xyzn : coordinates) {
                c.add(module.newValueBuilder(Coordinate.class).prototype().of(xyzn));
            }
            return null;
        }

        public double[] source()
        {
            // List<Double> c = new ArrayList<Double>();

            // for (int i = 0; i < self.coordinates().get().size(); i++) {
                return self.coordinates().get().get(0).source();
            // }

            // double [] values = new double[3];
            // return null;
        }

        public Coordinate[] getCoordinates()
        {
            List<Coordinate> coordinates = new ArrayList<>(); //.toArray()[1] = getCoordinate();
            coordinates.add(getCoordinate());
            return (Coordinate[])coordinates.toArray();
        }

        public Coordinate getCoordinate() {
            return self.coordinates().get().size() != 0 ? self.coordinates().get().get(0) : null;
        }

        public int getNumPoints() {
            return isEmpty() ? 0 : 1;
        }

        public int compareTo(Object other)
        {
            TPoint point = (TPoint)other;
            return getCoordinate().compareTo(point.getCoordinate());
        }

    }

}
