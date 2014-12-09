package org.qi4j.library.spatial.v2.transformations;

import org.qi4j.api.structure.Module;

/**
 * Created by jj on 04.12.14.
 */
public class TTransformations {

    public static FromHelper Transform(Module module)
    {
        return new FromHelper(module);
    }
}
