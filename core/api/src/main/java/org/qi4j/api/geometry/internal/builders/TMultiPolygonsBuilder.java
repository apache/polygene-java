package org.qi4j.api.geometry.internal.builders;

import org.qi4j.api.geometry.TMultiPoint;
import org.qi4j.api.geometry.TMultiPolygon;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.structure.Module;

import java.util.List;

/**
 * Created by jj on 26.11.14.
 */
public class TMultiPolygonsBuilder {

    private Module module;
    private TMultiPolygon geometry;


    public TMultiPolygonsBuilder(Module module)
    {
        this.module = module;
        geometry = module.newValueBuilder(TMultiPolygon.class).prototype();
    }


    public TMultiPolygonsBuilder points(double[][][] points)
    {
        for(double xy[][] : points)
        {
            if (xy.length < 2) return null;
            // geometry.xy(xy[0], xy[1]);
        }
        return this;
    }

    public TMultiPolygonsBuilder of(List<TPolygon> polygons)
    {
        geometry.of(polygons);
        return this;
    }

    public TMultiPolygonsBuilder of(TPolygon...polygons)
    {
        geometry.of(polygons);
        return this;
    }




    public TMultiPolygon geometry()
    {
        return geometry;
    }

    public TMultiPolygon geometry(int srid)
    {
        return geometry();
    }
}
