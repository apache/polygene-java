package org.qi4j.api.geometry;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import java.util.ArrayList;
import java.util.List;


@Mixins( TLineString.Mixin.class )
public interface TLineString extends TGeometry {

    // Data
    Property<List<TPoint>> points();

    // Interactions
    TLineString of(TPoint... points);

    boolean isEmpty();
    int getNumPoints();
    TPoint getPointN(int n);
    TPoint getStartPoint();
    TPoint getEndPoint();

    public abstract class Mixin implements TLineString
    {
        @This
        TLineString self;

        public TLineString of(TPoint... points)
        {

            List<TPoint> l = new ArrayList<TPoint>();

            for (TPoint p : points)
            {
                l.add(p);
            }

            self.points().set(l);
            self.type().set("LineString");
            return self;
        }

        public boolean isEmpty() {
            return self.points().get().size() == 0;
        }

        public int getNumPoints() {
            return self.points().get().size();
        }

        public TPoint getPointN(int n) {
            System.out.println("points " + self.points().get().size());
            return self.points().get().get(n);
        }

        public TPoint getStartPoint() {
            if (isEmpty()) {
                return null;
            }
            return getPointN(0);
        }

        public TPoint getEndPoint() {
            if (isEmpty()) {
                return null;
            }
            return getPointN(getNumPoints() - 1);
        }
    }


}
