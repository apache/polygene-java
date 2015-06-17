package org.qi4j.bootstrap.assembly.service;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.layered.LayerAssembler;
import org.qi4j.bootstrap.LayerAssembly;

public class ServiceLayer implements LayerAssembler
{
    public static final String NAME = "Service";

    @Override
    public LayerAssembly assemble( LayerAssembly layer )
        throws AssemblyException
    {
        return null;
    }
}
