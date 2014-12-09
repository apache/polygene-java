package org.qi4j.api.geometry.internal.builders;

import org.qi4j.api.geometry.TFeature;
import org.qi4j.api.geometry.TMultiPolygon;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.structure.Module;

import java.util.List;

/**
 * Created by jj on 26.11.14.
 */
public class TFeatureBuilder {

    private Module module;
    private TFeature geometry;


    public TFeatureBuilder(Module module)
    {
        this.module = module;
        geometry = module.newValueBuilder(TFeature.class).prototype();
    }


    public TFeatureBuilder of(TGeometry feature)
    {
        geometry.of(feature);
        return this;
    }


    public TFeature geometry()
    {
        return geometry;
    }

    public TFeature geometry(int srid)
    {
        return geometry();
    }
}
