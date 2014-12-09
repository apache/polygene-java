package org.qi4j.api.geometry.internal.builders;

import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.structure.Module;

/**
 * Created by jj on 26.11.14.
 */
public class TPolygonBuilder {

    private Module module;
    private TPolygon geometry;


    public TPolygonBuilder(Module module)
    {
        this.module = module;
        geometry = module.newValueBuilder(TPolygon.class).prototype();
    }

    public TPolygonBuilder shell(TLinearRing shell)
    {
        geometry.of(shell); return this;
    }

    public TPolygonBuilder shell(double[][] shell)
    {
        System.out.println("Shell " + new TLinearRingBuilder(module).ring(shell).geometry());

        geometry.of( new TLinearRingBuilder(module).ring(shell).geometry()); return this;
    }

    public TPolygonBuilder shell2(double[]... shell)
    {
        return null;
    }

    public TPolygonBuilder withHoles(TLinearRing... holes)
    {
        geometry.withHoles(holes); return this;
    }


    public TPolygon geometry()
    {
        return geometry;
    }

    public TPolygon geometry(String CRS)
    {
        geometry().setCRS(CRS);
        return geometry();
    }
}
