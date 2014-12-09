package org.qi4j.api.geometry.internal.builders;

import org.qi4j.api.geometry.TCRS;
import org.qi4j.api.structure.Module;

/**
 * Created by jj on 26.11.14.
 */
public class TCRSBuilder {

    private Module module;
    private TCRS geometry;


    public TCRSBuilder(Module module)
    {
        this.module = module;
        geometry = module.newValueBuilder(TCRS.class).prototype();
    }

    public TCRS crs(String crs)
    {
        return geometry.of(crs);
    }


}
