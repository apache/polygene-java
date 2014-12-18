package org.qi4j.library.spatial.v2.transformations;

import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.structure.Module;
import org.qi4j.library.spatial.v2.conversions.from.TGeometryFromConverter;

/**
 * Created by jj on 04.12.14.
 */
public class FromHelper {

    private Module module;

    public FromHelper(Module module)
    {
        this.module = module;
    }

    private FromHelper() {}

    public ToHelper from(TGeometry tGeometry)
    {
        return new ToHelper(module, new TGeometryFromConverter(module).convert(tGeometry));
    }


}
