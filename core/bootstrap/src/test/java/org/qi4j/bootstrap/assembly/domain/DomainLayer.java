package org.qi4j.bootstrap.assembly.domain;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.layered.LayeredLayerAssembler;

public class DomainLayer extends LayeredLayerAssembler
{
    @Override
    public LayerAssembly assemble( LayerAssembly layer )
        throws AssemblyException
    {
        createModule( layer, InvoicingModule.class );
        createModule( layer, OrderModule.class );
        return layer;
    }
}
