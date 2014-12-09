package org.qi4j.library.spatial.v2.conversions;

import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.structure.Module;
import org.qi4j.library.spatial.v2.conversions.from.FromHelper;

/**
 * Created by jj on 04.12.14.
 */
public class TConversions {

    public static FromHelper Convert(Module module)
    {
        return new FromHelper(module);
    }
}
