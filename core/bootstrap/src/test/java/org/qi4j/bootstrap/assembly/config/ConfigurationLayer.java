package org.qi4j.bootstrap.assembly.config;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.layered.LayerAssembler;
import org.qi4j.bootstrap.LayerAssembly;

public class ConfigurationLayer implements LayerAssembler
{
    @Override
    public LayerAssembly assemble( LayerAssembly layer )
        throws AssemblyException
    {
        return layer;
    }
}
