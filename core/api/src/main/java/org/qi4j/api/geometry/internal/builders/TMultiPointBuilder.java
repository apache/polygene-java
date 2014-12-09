package org.qi4j.api.geometry.internal.builders;

import org.qi4j.api.geometry.TLineString;
import org.qi4j.api.geometry.TMultiPoint;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.structure.Module;

/**
 * Created by jj on 26.11.14.
 */
public class TMultiPointBuilder {

    private Module module;
    private TMultiPoint geometry;


    public TMultiPointBuilder(Module module)
    {
        this.module = module;
        geometry = module.newValueBuilder(TMultiPoint.class).prototype();
    }


    public TMultiPointBuilder points(double[][] points)
    {
        for(double xy[] : points)
        {
            if (xy.length < 2) return null;
            geometry.xy(xy[0],xy[1]);
        }
        return this;
    }

    public TMultiPointBuilder of(TPoint...points)
    {
        geometry().of(points);
        return this;
    }

    public TMultiPoint geometry()
    {
        return geometry;
    }

    public TMultiPoint geometry(String CRS)
    {
        geometry().setCRS(CRS);
        return geometry();
    }
}
