package org.qi4j.library.spatial.v2.conversions.from;

import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.structure.Module;

/**
 * Created by jj on 04.12.14.
 */
public class TGeometryFromConverter {

    private Module module;

    public TGeometryFromConverter(Module module)
    {
        this.module = module;
    }

    public TGeometry convert(TGeometry tGeometry)
    {
        return tGeometry;
    }
}
