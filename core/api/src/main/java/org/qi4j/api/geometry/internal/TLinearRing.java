package org.qi4j.api.geometry.internal;

import org.qi4j.api.geometry.TLineString;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

@Mixins( TLinearRing.Mixin.class )
public interface TLinearRing extends TLineString {

    TLinearRing of(TPoint... points);
    boolean isValid();

    public abstract class Mixin extends TLineString.Mixin implements TLinearRing
    {

        @This
        TLinearRing self;

        public TLinearRing of(TPoint... points)
        {
            super.of(points);

            if(!isValid()) {
                System.out.println("Not valid..");
            }

            return self;
        }

        public boolean isValid()
        {
          // JJ TODO
          return true;
        }
    }

}
