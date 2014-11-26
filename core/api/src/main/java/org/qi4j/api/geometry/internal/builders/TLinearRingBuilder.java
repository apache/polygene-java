package org.qi4j.api.geometry.internal.builders;

import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.structure.Module;

/**
 * Created by jj on 26.11.14.
 */
public class TLinearRingBuilder {

    private Module module;
    private TLinearRing geometry;


    public TLinearRingBuilder(Module module)
    {
        this.module = module;
        geometry = module.newValueBuilder(TLinearRing.class).prototype();
    }


    public TLinearRingBuilder ring(double[][] ring)
    {
        for(double xy[] : ring)
        {
            if (xy.length < 2) return null;
            geometry.xy(xy[0],xy[1]);
        }
        return this;
    }


    public TLinearRing geometry()
    {
        System.out.println(geometry + " " + geometry.isValid() );

        if (!geometry.isValid()) return null;
            else
        return geometry;
    }

    public TLinearRing geometry(int srid)
    {
        return geometry();
    }
}
