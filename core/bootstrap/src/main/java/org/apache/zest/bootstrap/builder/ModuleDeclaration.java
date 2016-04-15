/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.bootstrap.builder;

import java.util.ArrayList;
import java.util.List;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.LayerAssembly;
import org.apache.zest.bootstrap.ModuleAssembly;

import static org.apache.zest.api.util.Classes.isAssignableFrom;
import static org.apache.zest.functional.Iterables.filter;
import static org.apache.zest.functional.Iterables.toList;

/**
 * Provides declared {@link org.apache.zest.api.structure.Module} information that the {@link ApplicationBuilder} can use.
 */
public class ModuleDeclaration
{
    private final String moduleName;
    private final List<Assembler> assemblers = new ArrayList<>();
    private ModuleAssembly module;

    public ModuleDeclaration( String moduleName )
    {
        this.moduleName = moduleName;
    }

    /**
     * Declare Assembler.
     * @param assembler Assembler instance
     * @return This Module declaration
     */
    public ModuleDeclaration withAssembler( Assembler assembler )
    {
        assemblers.add( assembler );
        return this;
    }

    /**
     * Declare Assembler.
     * @param classname Assembler class name
     * @return This Module declaration
     * @throws AssemblyException if unable to load class, not an Assembler or unable to instanciate
     */
    public ModuleDeclaration withAssembler( String classname )
        throws AssemblyException
    {
        Class<? extends Assembler> clazz = loadClass( classname );
        return withAssembler( clazz );
    }

    /**
     * Declare Assembler.
     * @param assemblerClass Assembler class
     * @return This Module declaration
     * @throws AssemblyException not an Assembler or if unable to instanciate
     */
    public ModuleDeclaration withAssembler( Class<?> assemblerClass )
        throws AssemblyException
    {
        Assembler assembler = createAssemblerInstance( assemblerClass );
        assemblers.add( assembler );
        return this;
    }

    /**
     * Declare Assemblers.
     * <p>Declare several Assemblers from an Iterable of Class objects.</p>
     * <p>Typically used along {@link org.apache.zest.bootstrap.ClassScanner}.</p>
     * @param assemblerClasses Assembler classes
     * @return This Module declaration
     * @throws AssemblyException if one of the Class is not an Assembler or unable to instanciate
     */
    public ModuleDeclaration withAssemblers( Iterable<Class<?>> assemblerClasses )
        throws AssemblyException
    {
        List<Class<?>> notAssemblers = toList(
            filter( isAssignableFrom( Assembler.class ).negate(),
                    assemblerClasses )
        );
        if( !notAssemblers.isEmpty() )
        {
            throw new AssemblyException(
                "Classes " + notAssemblers + " are not implementing " + Assembler.class.getName()
            );
        }
        for( Class<?> assemblerClass : assemblerClasses )
        {
            withAssembler( assemblerClass );
        }
        return this;
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
        Class<?> clazz;
        try
        {
            clazz = getClass().getClassLoader().loadClass( classname );
        }
        catch( Exception e )
        {
            throw new AssemblyException( "Unable to load class " + classname, e );
        }
        if( !Assembler.class.isAssignableFrom( clazz ) )
        {
            throw new AssemblyException(
                "Class " + classname + " is not implementing " + Assembler.class.getName()
            );
        }
        //noinspection unchecked
        return (Class<? extends Assembler>) clazz;
    }

    private Assembler createAssemblerInstance( Class<?> assemblerClass )
        throws AssemblyException
    {
        if( !Assembler.class.isAssignableFrom( assemblerClass ) )
        {
            throw new AssemblyException(
                "Class " + assemblerClass + " is not implementing " + Assembler.class.getName()
            );
        }
        try
        {
            return (Assembler) assemblerClass.newInstance();
        }
        catch( Exception e )
        {
            throw new AssemblyException( "Unable to instantiate " + assemblerClass, e );
        }
    }
}
