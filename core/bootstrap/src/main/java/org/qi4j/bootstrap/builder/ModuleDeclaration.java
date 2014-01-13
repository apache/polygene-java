package org.qi4j.bootstrap.builder;

import java.util.ArrayList;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;

public class ModuleDeclaration
{
    private final String moduleName;
    private final ArrayList<Assembler> assemblers = new ArrayList<>();
    private ModuleAssembly module;

    public ModuleDeclaration( String moduleName )
    {
        this.moduleName = moduleName;
    }

    public ModuleDeclaration withAssembler( Assembler assembler )
    {
        assemblers.add( assembler );
        return this;
    }

    ModuleDeclaration withAssembler( Class<? extends Assembler> assemblerClass )
        throws AssemblyException
    {
        Assembler assembler = createAssemblerInstance( assemblerClass );
        assemblers.add( assembler );
        return this;
    }

    public ModuleDeclaration withAssembler( String classname )
        throws AssemblyException
    {
        Class<? extends Assembler> clazz = loadClass( classname );
        return withAssembler( clazz );
    }

    ModuleAssembly createModule( LayerAssembly layer )
    {
        module = layer.module( moduleName );
        return module;
    }

    void initialize()
        throws AssemblyException
    {
        for( Assembler assembler : assemblers )
        {
            assembler.assemble( module );
        }
    }

    @SuppressWarnings( "unchecked" )
    private Class<? extends Assembler> loadClass( String classname )
        throws AssemblyException
    {
        try
        {
            Class<?> clazz = getClass().getClassLoader().loadClass( classname );
            if( Assembler.class.isAssignableFrom( clazz ) )
            {
                //noinspection unchecked
                return (Class<? extends Assembler>) clazz;
            }
            throw new AssemblyException( "Class " + classname + " is not implementing " + Assembler.class.getName() );
        }
        catch( Exception e )
        {
            throw new AssemblyException( "Unable to load class " + classname, e );
        }
    }

    private Assembler createAssemblerInstance( Class<? extends Assembler> assemblerClass )
        throws AssemblyException
    {
        Assembler assembler;
        try
        {
            assembler = assemblerClass.newInstance();
        }
        catch( Exception e )
        {
            throw new AssemblyException( "Unable to instantiate " + assemblerClass, e );
        }
        return assembler;
    }
}
