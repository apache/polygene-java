package org.qi4j.api.geometry;

import org.qi4j.api.geometry.internal.builders.TLinearRingBuilder;
import org.qi4j.api.geometry.internal.builders.TPointBuilder;
import org.qi4j.api.geometry.internal.builders.TPolygonBuilder;
import org.qi4j.api.structure.Module;

/**
 * Created by jj on 26.11.14.
 */
public class TGEOM {

    public static TPointBuilder TPOINT(Module module)
    {
        return new TPointBuilder(module);
    }

    public static TLinearRingBuilder TLINEARRING(Module module)
    {
        return new TLinearRingBuilder(module);
    }

    public static TPolygonBuilder TPOLYGON(Module module)
    {
        return new TPolygonBuilder(module);
    }
}
