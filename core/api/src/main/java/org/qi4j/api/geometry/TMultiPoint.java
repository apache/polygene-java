package org.qi4j.api.geometry;

import org.qi4j.api.geometry.internal.GeometryCollections;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jj on 28.11.14.
 */

@Mixins( TMultiPoint.Mixin.class )
public interface TMultiPoint extends GeometryCollections {


    TMultiPoint of(TPoint... points);
    TMultiPoint of(List<TPoint> points);
    TMultiPoint xy(double x, double y);

    public abstract class Mixin extends GeometryCollections.Mixin implements TMultiPoint
    {

        @This
        TMultiPoint self;

        @Structure
        Module module;

        public TMultiPoint of(List<TPoint> points)
        {
            of(points.toArray(new TPoint[points.size()]));
            return self;
        }

        public TMultiPoint xy(double x, double y) {
            of(module.newValueBuilder(TPoint.class).prototype().x(x).y(y));
            return self;
        }


        public TMultiPoint of(TPoint... points)
        {
            self.geometryType().set(TGEOMETRY.MULTIPOINT);
            init();
            List<TGeometry> l = new ArrayList<>();

            for (TPoint p : points)
            {
                l.add(p);
            }

            if (self.isEmpty())
                self.geometries().set(l); // points().set(l);
            else
                self.geometries().get().addAll(l);

            // self.type().set(TGeomRoot.TGEOMETRY.LINESTRING);

            return self;
        }



    }
}
