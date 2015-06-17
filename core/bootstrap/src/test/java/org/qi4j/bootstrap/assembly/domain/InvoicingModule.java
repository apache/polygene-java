package org.qi4j.bootstrap.assembly.domain;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.layered.ModuleAssembler;

public class InvoicingModule
    implements ModuleAssembler
{
    @Override
    public ModuleAssembly assemble( LayerAssembly layer, ModuleAssembly module )
        throws AssemblyException
    {
        return module;
    }
}
