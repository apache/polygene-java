package org.qi4j.api.geometry;

import org.qi4j.api.geometry.internal.builders.*;
import org.qi4j.api.structure.Module;

/**
 * Created by jj on 26.11.14.
 */
public class TGEOM {


    public static TCRSBuilder TCRS(Module module)
    {
        return new TCRSBuilder(module);
    }

    public static TPointBuilder TPOINT(Module module)
    {
        return new TPointBuilder(module);
    }

    public static TMultiPointBuilder TMULTIPOINT(Module module)
    {
        return new TMultiPointBuilder(module);
    }

    public static TLinearRingBuilder TLINEARRING(Module module)
    {
        return new TLinearRingBuilder(module);
    }

    public static TLineStringBuilder TLINESTRING(Module module)
    {
        return new TLineStringBuilder(module);
    }

    public static TPolygonBuilder TPOLYGON(Module module)
    {
        return new TPolygonBuilder(module);
    }

    public static TMultiPolygonsBuilder TMULTIPOLYGON(Module module)
    {
        return new TMultiPolygonsBuilder(module);
    }

    public static TFeatureBuilder TFEATURE(Module module)
    {
        return new TFeatureBuilder(module);
    }

    public static TFeatureCollectionBuilder TFEATURECOLLECTION(Module module)
    {
        return new TFeatureCollectionBuilder(module);
    }
}
