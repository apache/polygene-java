package org.qi4j.api.geometry.internal.builders;

import org.qi4j.api.geometry.TFeature;
import org.qi4j.api.geometry.TFeatureCollection;
import org.qi4j.api.geometry.TMultiPolygon;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.structure.Module;

import java.util.List;

/**
 * Created by jj on 26.11.14.
 */
public class TFeatureCollectionBuilder {

    private Module module;
    private TFeatureCollection geometry;


    public TFeatureCollectionBuilder(Module module)
    {
        this.module = module;
        geometry = module.newValueBuilder(TFeatureCollection.class).prototype();
    }

    public TFeatureCollectionBuilder of(List<TFeature> features)
    {
        geometry.of(features);
        return this;
    }

    public TFeatureCollectionBuilder of(TFeature... features)
    {
        geometry.of(features);
        return this;
    }

    public TFeatureCollection geometry()
    {
        return geometry;
    }

    public TFeatureCollection geometry(int srid)
    {
        return geometry();
    }
}
