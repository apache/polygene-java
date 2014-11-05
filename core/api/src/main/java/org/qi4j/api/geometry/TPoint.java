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



    Property<List<Coordinate>> coordinates();
    TPoint of(Coordinate... coordinates);
   //  TPoint of(double... coordinates);



    public abstract class Mixin implements TPoint
    {

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

        public TPoint of(List<Double> coordinates)
        {

            List<Coordinate> c = new ArrayList<Coordinate>();

            for (Double xyzn : coordinates) {
                c.add(module.newValueBuilder(Coordinate.class).prototype().of(xyzn));
            }
            return null;
        }


    }

}
