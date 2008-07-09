/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.bootstrap;

import org.qi4j.Qi4j;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.object.ObjectBuilderFactory;
import org.qi4j.service.ServiceFinder;
import org.qi4j.structure.Application;
import org.qi4j.structure.Module;

/**
 * Base class for Assembler that creates an Application
 * with one Layer and one Module. Create a subclass of this
 * and implement the {@link Assembler#assemble(ModuleAssembly)} method.
 * Once the SingletonAssembler is instantiated it will have created and activated
 * an Application which can be accessed from {@link org.qi4j.bootstrap.SingletonAssembler#application()}.
 * You can also easily access any resources specific for the single Module, such as the CompositeBuilderFactory.
 */
public abstract class SingletonAssembler
    implements Assembler
{
    private Energy4Java is = new Energy4Java();

    private Application applicationInstance;
    private Module moduleInstance;

    public SingletonAssembler()
        throws IllegalStateException
    {
        try
        {
            applicationInstance = is.newApplication( this );
        }
        catch( AssemblyException e )
        {
            throw new IllegalStateException( "Could not instantiate application", e );
        }

        try
        {
            applicationInstance.activate();
        }
        catch( Exception e )
        {
            throw new IllegalStateException( "Could not activate application", e );
        }

        moduleInstance = applicationInstance.findModule( "Layer 1", "Module 1" );
    }

    public final Qi4j runtime()
    {
        return applicationInstance.runtime();
    }

    public final Application application()
    {
        return applicationInstance;
    }

    public final Module module()
    {
        return moduleInstance;
    }

    public final CompositeBuilderFactory compositeBuilderFactory()
    {
        return moduleInstance.compositeBuilderFactory();
    }

    public final ObjectBuilderFactory objectBuilderFactory()
    {
        return moduleInstance.objectBuilderFactory();
    }

    public final UnitOfWorkFactory unitOfWorkFactory()
    {
        return moduleInstance.unitOfWorkFactory();
    }

    public final ServiceFinder serviceFinder()
    {
        return moduleInstance.serviceFinder();
    }
}
