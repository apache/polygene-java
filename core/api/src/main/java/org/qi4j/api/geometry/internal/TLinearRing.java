package org.qi4j.api.geometry.internal;

import org.qi4j.api.geometry.TLineString;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

import java.util.List;

@Mixins( TLinearRing.Mixin.class )
public interface TLinearRing extends TLineString {

    // TLinearRing of(TPoint... points);
    // TLinearRing of(List<TPoint> points);
    // TLinearRing xy(double x, double y);
    boolean isValid();

    public abstract class Mixin extends TLineString.Mixin implements TLinearRing //, TLineString
    {

        @This
        TLinearRing self;
/**
        @Override
        public TLinearRing of(TPoint... points)
        {
             super.of(points);

            // self.of(points);

            return self;
        }

        @Override
        public TLinearRing of(List<TPoint> points)
        {
            of(points.toArray(new TPoint[points.size()]));
            return self;
        }

        @Override
        public TLinearRing xy(double x, double y) {
            super.xy(x,y);
            // self.xy(x,y);
            return self;
        }
*/

        // public Coordinate[] getCoordinates()
      //  {
        //    return null;
        //}

        @Override
        public boolean isValid()
        {
             if (self.getStartPoint() == null || self.getEndPoint() == null) return  false;
             return self.getStartPoint().compareTo(self.getEndPoint()) == 0 ? true : false;
        }
    }

}
