package org.qi4j.api.geometry.internal.builders;

import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

/**
 * Created by jj on 26.11.14.
 */
public class TPointBuilder {

    private Module module;
    private TPoint geometry;


    public TPointBuilder(Module module)
    {
        this.module = module;
        geometry = module.newValueBuilder(TPoint.class).prototype();
    }

    public TPointBuilder x(double x)
    {
        geometry.x(x);
        return this;
    }

    public TPointBuilder y(double y)
    {
        geometry.y(y);
        return this;
    }

    public TPointBuilder z(double u)
    {
        geometry.z(u);
        return this;
    }

    public TPoint geometry()
    {
        return geometry;
    }

    public TPoint geometry(String CRS)
    {
        geometry().setCRS(CRS);
        return geometry();
    }
}
