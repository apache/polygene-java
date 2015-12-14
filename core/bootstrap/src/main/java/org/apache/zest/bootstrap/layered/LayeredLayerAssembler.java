/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.bootstrap.layered;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import org.apache.zest.bootstrap.LayerAssembly;
import org.apache.zest.bootstrap.ModuleAssembly;

public abstract class LayeredLayerAssembler
    implements LayerAssembler
{
    private HashMap<Class<? extends ModuleAssembler>, ModuleAssembler> assemblers = new HashMap<>();

    protected ModuleAssembly createModule( LayerAssembly layer, Class<? extends ModuleAssembler> modulerAssemblerClass )
    {
        try
        {
            ModuleAssembler moduleAssembler = instantiateAssembler( layer, modulerAssemblerClass );
            String moduleName = createModuleName( modulerAssemblerClass );
            LayeredApplicationAssembler.setNameIfPresent( modulerAssemblerClass, moduleName );
            ModuleAssembly module = layer.module( moduleName );
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

    protected String createModuleName( Class<? extends ModuleAssembler> modulerAssemblerClass )
    {
        String moduleName = modulerAssemblerClass.getSimpleName();
        if( moduleName.endsWith( "Module" ) )
        {
            moduleName = moduleName.substring( 0, moduleName.length() - 6 ) + " Module";
        }
        return moduleName;
    }

    private ModuleAssembler instantiateAssembler( LayerAssembly layer,
                                                  Class<? extends ModuleAssembler> modulerAssemblerClass
    )
        throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException, NoSuchMethodException
    {
        ModuleAssembler moduleAssembler;
        try
        {
            Constructor<? extends ModuleAssembler> assemblyConstructor = modulerAssemblerClass.getDeclaredConstructor( ModuleAssembly.class );
            assemblyConstructor.setAccessible( true );
            moduleAssembler = assemblyConstructor.newInstance( layer );
        }
        catch( NoSuchMethodException e )
        {
            Constructor<? extends ModuleAssembler> assemblyConstructor = modulerAssemblerClass.getDeclaredConstructor();
            assemblyConstructor.setAccessible( true );
            moduleAssembler = assemblyConstructor.newInstance();
        }
        return moduleAssembler;
    }

    @SuppressWarnings( "unchecked" )
    protected <T extends ModuleAssembler> T assemblerOf( Class<T> moduleAssemblerType )
    {
        return (T) assemblers.get( moduleAssemblerType );
    }
}
