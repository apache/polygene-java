package org.qi4j.bootstrap.assembly.infrastructure;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.layered.LayerAssembler;
import org.qi4j.bootstrap.layered.LayeredLayerAssembler;

public class InfrastructureLayer extends LayeredLayerAssembler
    implements LayerAssembler
{
    public static final String NAME = "Infrastructure Layer";
    private final ModuleAssembly configModule;

    public InfrastructureLayer( ModuleAssembly configModule )
    {
        this.configModule = configModule;
    }

    @Override
    public LayerAssembly assemble( LayerAssembly layer )
        throws AssemblyException
    {
        new StorageModule( configModule ).assemble( layer, layer.module( StorageModule.NAME ) );
        new IndexingModule( configModule ).assemble( layer, layer.module( IndexingModule.NAME ) );
        createModule( layer, SerializationModule.class );
        return layer;
    }
}
