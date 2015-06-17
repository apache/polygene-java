package org.qi4j.bootstrap.layered;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;

public abstract class LayeredLayerAssembler
    implements LayerAssembler
{
    private HashMap<Class<? extends ModuleAssembler>, ModuleAssembler> assemblers = new HashMap<>();

    protected ModuleAssembly createModule( LayerAssembly layer, Class<? extends ModuleAssembler> modulerAssemblerClass )
    {
        try
        {
            ModuleAssembler moduleAssembler = instantiateAssembler( layer, modulerAssemblerClass );
            String classname = modulerAssemblerClass.getSimpleName();
            if( classname.endsWith( "Module" ) )
            {
                classname = classname.substring( 0, classname.length() - 6 ) + " Module";
            }
            LayeredApplicationAssembler.setNameIfPresent( modulerAssemblerClass, classname );
            ModuleAssembly module = layer.module( classname );
            assemblers.put( modulerAssemblerClass, moduleAssembler );
            ModuleAssembly assembly = moduleAssembler.assemble( layer, module );
            if( assembly == null )
            {
                return module;
            }
            return assembly;
        }
        catch( Exception e )
        {
            throw new IllegalArgumentException( "Unable to instantiate module with " + modulerAssemblerClass.getSimpleName(), e );
        }
    }

    private ModuleAssembler instantiateAssembler( LayerAssembly layer,
                                                  Class<? extends ModuleAssembler> modulerAssemblerClass
    )
        throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException
    {
        ModuleAssembler moduleAssembler;
        try
        {
            Constructor<? extends ModuleAssembler> assemblyConstructor = modulerAssemblerClass.getConstructor( ModuleAssembly.class );
            moduleAssembler = assemblyConstructor.newInstance( layer );
        }
        catch( NoSuchMethodException e )
        {
            moduleAssembler = modulerAssemblerClass.newInstance();
        }
        return moduleAssembler;
    }

    @SuppressWarnings( "unchecked" )
    protected <T extends ModuleAssembler> T assemblerOf( Class<T> moduleAssemblerType )
    {
        return (T) assemblers.get( moduleAssemblerType );
    }
}
