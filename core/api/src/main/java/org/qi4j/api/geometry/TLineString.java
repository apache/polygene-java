package org.qi4j.api.geometry;

import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;

import java.util.ArrayList;
import java.util.List;


@Mixins( TLineString.Mixin.class )
public interface TLineString extends TGeometry {

    // Data
    Property<List<TPoint>> points();

    // Interactions
    TLineString of(TPoint... points);
    TLineString xy(double x, double y);

    boolean isEmpty();
    int getNumPoints();
    TPoint getPointN(int n);
    TPoint getStartPoint();
    TPoint getEndPoint();


    public abstract class Mixin implements TLineString
    {
        @This
        TLineString self;

        @Structure
        Module module;


        public TLineString of(TPoint... points)
        {

            List<TPoint> l = new ArrayList<>();

            for (TPoint p : points)
            {
                l.add(p);
            }


            if (self.isEmpty())
                self.points().set(l);
            else
                self.points().get().addAll(l);

            self.type().set(TGEOMETRY.LINESTRING);

            return self;
        }

        public TLineString xy(double x, double y) {
            of(module.newValueBuilder(TPoint.class).prototype().x(x).y(y));
            return self;
        }

        public boolean isEmpty() {
            return (self.points() == null) || (self.points().get() == null) || (self.points().get().isEmpty()) ? true : false;
        }

        public int getNumPoints() {
            return isEmpty() ? 0 : self.points().get().size();
        }

        public TPoint getPointN(int n) {
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

        public Coordinate[] getCoordinates()
        {
            int k = -1;
            Coordinate[] coordinates = new Coordinate[getNumPoints()];
          for (int i = 0; i < getNumPoints(); i++) {
              Coordinate[] childCoordinates = getPointN(i).getCoordinates();
              for (int j = 0; j < childCoordinates.length; j++) {
                  k++;
                  coordinates[k] = childCoordinates[j];
              }
          }

            return coordinates;
        }
    }


}
