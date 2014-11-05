package org.qi4j.api.geometry;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;

import java.util.ArrayList;
import java.util.List;

@Mixins( TMultiPolygon.Mixin.class )
public interface TMultiPolygon extends TGeometry {


    // Data
    Property<List<TPolygon>> polygons();

    // Interaction
    TMultiPolygon of(TPolygon... polygons);



    public abstract class Mixin implements TMultiPolygon
    {
        @Structure
        Module module;

        @This
        TMultiPolygon self;


        public TMultiPolygon of(TPolygon... polygons)
        {
            if (polygons != null) {

                List<TPolygon> l = new ArrayList<TPolygon>();

                for (TPolygon p : polygons)
                {
                    l.add(p);
                }

                self.polygons().set(l);
            }

            return self;
        }

    }
}
