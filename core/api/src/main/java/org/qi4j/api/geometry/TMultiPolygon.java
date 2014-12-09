package org.qi4j.api.geometry;

import org.qi4j.api.geometry.internal.GeometryCollections;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;

import java.util.ArrayList;
import java.util.List;

@Mixins( TMultiPolygon.Mixin.class )
public interface TMultiPolygon extends GeometryCollections {



    // Interaction
    TMultiPolygon of(TPolygon... polygons);
    TMultiPolygon of(List<TPolygon> polygons);



    public abstract class Mixin extends GeometryCollections.Mixin implements TMultiPolygon
    {
        @Structure
        Module module;

        @This
        TMultiPolygon self;


        public TMultiPolygon of(List<TPolygon> polygons)
        {
            of(polygons.toArray(new TPolygon[polygons.size()]));
            return self;
        }



        public TMultiPolygon of(TPolygon... polygons)
        {
            self.geometryType().set(TGEOMETRY.MULTIPOLYGON);
            init();
            List<TGeometry> l = new ArrayList<>();

            for (TPolygon p : polygons)
            {
                l.add(p);
            }

            if (self.isEmpty())
                self.geometries().set(l); // points().set(l);
            else
                self.geometries().get().addAll(l);

            return self;
        }

    }
}
