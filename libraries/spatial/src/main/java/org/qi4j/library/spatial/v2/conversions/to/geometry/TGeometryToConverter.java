package org.qi4j.library.spatial.v2.conversions.to.geometry;

import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.structure.Module;

/**
 * Created by jj on 04.12.14.
 */
public class TGeometryToConverter<T extends TGeometry> {


    private Module module;

    public TGeometryToConverter(Module module)
    {
        this.module = module;
    }

    public TGeometry convert (TGeometry tGeometry, String CRS)
    {
        return tGeometry;
    }

    public TGeometry convert(TGeometry tGeometry)
    {
        return convert(tGeometry, tGeometry.getCRS());
    }

}
