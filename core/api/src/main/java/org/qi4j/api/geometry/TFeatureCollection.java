package org.qi4j.api.geometry;

import org.qi4j.api.geometry.internal.GeometryCollections;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;

import java.util.ArrayList;
import java.util.List;

@Mixins( TFeatureCollection.Mixin.class )
public interface TFeatureCollection extends GeometryCollections {

    TFeatureCollection of(TFeature... features);
    TFeatureCollection of(List<TFeature> features);

    public abstract class Mixin extends GeometryCollections.Mixin implements TFeatureCollection
    {
        @Structure
        Module module;

        @This
        TFeatureCollection self;

        public TFeatureCollection of(List<TFeature> features)
        {
            of(features.toArray(new TFeature[features.size()]));
            return self;
        }

        public TFeatureCollection of(TFeature... features)
        {
            self.geometryType().set(TGEOMETRY.FEATURECOLLECTION);
            init();
            List<TGeometry> l = new ArrayList<>();

            for (TFeature f : features)
            {
                l.add(f);
            }

            if (self.isEmpty())
                self.geometries().set(l);
            else
                self.geometries().get().addAll(l);

            return self;
        }



    }

}
