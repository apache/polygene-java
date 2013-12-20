package org.qi4j.bootstrap.builder;

import java.util.ArrayList;
import java.util.HashMap;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;

public class LayerDeclaration
{
    private final String layerName;
    private final ArrayList<String> using = new ArrayList<>();
    private ArrayList<ModuleDeclaration> modules = new ArrayList<>();
    private LayerAssembly layer;

    LayerDeclaration( String layerName )
    {
        this.layerName = layerName;
    }

    public LayerDeclaration using( String layerName )
    {
        this.using.add( layerName );
        return this;
    }

    public ModuleDeclaration withModule( String moduleName )
    {
        ModuleDeclaration module = new ModuleDeclaration( moduleName );
        modules.add( module );
        return module;
    }

    LayerAssembly createLayer( ApplicationAssembly application )
    {
        layer = application.layer( layerName );
        layer.setName( layerName );
        for( ModuleDeclaration module : modules )
        {
            ModuleAssembly assembly = module.createModule( layer );
        }
        return layer;
    }

    void initialize( HashMap<String, LayerAssembly> createdLayers )
        throws AssemblyException
    {
        for( String uses : using )
        {
            LayerAssembly usedLayer = createdLayers.get( uses );
            layer.uses( usedLayer );
        }
        for( ModuleDeclaration module : modules )
        {
            module.initialize();
        }
    }
}
