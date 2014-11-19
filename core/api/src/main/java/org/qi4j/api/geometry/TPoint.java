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
   //  TPoint of(double... coordinates);

    TPoint X(double x);
    TPoint Y(double y);
    TPoint Z(double z);

    double X();
    double Y();
    double Z();

    double[] source();



    public abstract class Mixin implements TPoint
    {

        private void init()
        {

            if (self.coordinates().get() == null) {

                List<Coordinate> c = new ArrayList<Coordinate>();
                c.add(module.newValueBuilder(Coordinate.class).prototype().X(0).Y(0).Z(0));
                self.coordinates().set(c);
                self.type().set("Point");
            }
        }

        @Structure
        Module module;

        @This
        TPoint self;

        public TPoint of(Coordinate... coordinates)
        {

            List<Coordinate> c = new ArrayList<Coordinate>();

            for (Coordinate xyzn : coordinates)
            {
                c.add(xyzn);
            }

            self.coordinates().set(c);
            self.type().set("Point");

            return self;
        }

        public TPoint X(double x) {
            init();

           // self.coordinates().get().get(0).module.newValueBuilder(Coordinate.class).prototype().X(x));

            self.coordinates().get().get(0).X(x);

            return self;
        }

        public double X() {

            return self.coordinates().get().get(0).getOrdinate(Coordinate.X);
        }

        public double Y() {
            return self.coordinates().get().get(0).getOrdinate(Coordinate.Y);
        }

        public double Z() {
            return self.coordinates().get().get(0).getOrdinate(Coordinate.Z);
        }

        public TPoint Y(double y) {
            init();
            self.coordinates().get().get(0).Y(y);

            return self;
        }

        public TPoint Z(double z) {
            init();
            self.coordinates().get().get(0).Z(z);

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

    }

}
