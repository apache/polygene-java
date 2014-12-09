package org.qi4j.api.geometry.internal.builders;

import org.qi4j.api.geometry.TLineString;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.structure.Module;

/**
 * Created by jj on 26.11.14.
 */
public class TLineStringBuilder {

    private Module module;
    private TLineString geometry;


    public TLineStringBuilder(Module module)
    {
        this.module = module;
        geometry = module.newValueBuilder(TLineString.class).prototype();
    }


    public TLineStringBuilder points(double[][] points)
    {
        for(double xy[] : points)
        {
            if (xy.length < 2) return null;
            geometry.xy(xy[0],xy[1]);
        }
        return this;
    }

    public TLineStringBuilder of(TPoint...points)
    {
        geometry().of(points);
        return this;
    }

    public TLineStringBuilder of()
    {
        geometry().of();
        return this;
    }


    public TLineString geometry()
    {
        return geometry;
    }

    public TLineString geometry(int srid)
    {
        return geometry();
    }
}
