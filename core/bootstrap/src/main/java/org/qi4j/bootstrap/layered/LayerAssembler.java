package org.qi4j.bootstrap.layered;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;

public interface LayerAssembler
{
    LayerAssembly assemble( LayerAssembly layer )
        throws AssemblyException;
}
