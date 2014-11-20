package org.qi4j.api.geometry.internal;

import org.qi4j.api.geometry.TLineString;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

import java.util.List;

@Mixins( TLinearRing.Mixin.class )
public interface TLinearRing extends TLineString {

    TLinearRing of(TPoint... points);
    TLinearRing of(List<TPoint> points);
    boolean isValid();

    public abstract class Mixin extends TLineString.Mixin implements TLinearRing
    {

        @This
        TLinearRing self;

        public TLinearRing of(TPoint... points)
        {
             super.of(points);

            System.out.println("Number of Points " + super.getNumPoints());

            if(!isValid()) {
                System.out.println("Not valid..");
            }

            return self;
        }

        public TLinearRing of(List<TPoint> points)
        {
            of(points.toArray(new TPoint[points.size()]));
            return self;
        }


        public Coordinate[] getCoordinates()
        {
            return null;
        }


        public boolean isValid()
        {
             return self.getStartPoint().compareTo(self.getEndPoint()) == 0 ? true : false;
        }
    }

}
