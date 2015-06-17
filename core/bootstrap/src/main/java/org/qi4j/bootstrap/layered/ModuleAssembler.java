package org.qi4j.bootstrap.layered;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;

public interface ModuleAssembler
{
    ModuleAssembly assemble( LayerAssembly layer, ModuleAssembly module )
        throws AssemblyException;
}
