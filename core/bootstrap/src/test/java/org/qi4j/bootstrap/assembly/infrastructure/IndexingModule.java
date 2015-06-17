package org.qi4j.bootstrap.assembly.infrastructure;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.layered.ModuleAssembler;

public class IndexingModule
    implements ModuleAssembler
{
    public static final String NAME = "Indexing Module";
    private final ModuleAssembly configModule;

    public IndexingModule( ModuleAssembly configModule )
    {
        this.configModule = configModule;
    }

    @Override
    public ModuleAssembly assemble( LayerAssembly layer, ModuleAssembly module )
        throws AssemblyException
    {
        return module;
    }
}
