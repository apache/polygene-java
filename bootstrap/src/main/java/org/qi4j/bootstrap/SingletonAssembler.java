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

import org.qi4j.api.Qi4j;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.spi.structure.ApplicationSPI;

/**
 * Base class for Assembler that creates an Application
 * with one Layer and one Module. Create a subclass of this
 * and implement the {@link Assembler#assemble(ModuleAssembly)} method.
 * Once the SingletonAssembler is instantiated it will have created and activated
 * an Application which can be accessed from {@link org.qi4j.bootstrap.SingletonAssembler#application()}.
 * You can also easily access any resources specific for the single Module, such as the TransientBuilderFactory.
 */
public abstract class SingletonAssembler
    implements Assembler
{
    private Energy4Java qi4j;
    private ApplicationSPI applicationInstance;
    private Module moduleInstance;

    /**
     * Creates a Qi4j Runtime instance containing one Layer with one Module.
     * The Layer will be named "Layer 1" and the Module will be named "Module 1". It is possible to add
     * additional layers and modules via the Assembler interface that must be implemented in the subclass of this
     * class.
     *
     * @throws IllegalStateException Either if the model can not be created from the disk, or some inconsistency in
     *                               the programming model makes it impossible to create it.
     */
    public SingletonAssembler()
        throws IllegalStateException
    {
        qi4j = new Energy4Java();
        try
        {
            applicationInstance = qi4j.newApplication( new ApplicationAssembler()
            {
                public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                    throws AssemblyException
                {
                    return applicationFactory.newApplicationAssembly( SingletonAssembler.this );
                }
            } );
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
        return qi4j.spi();
    }

    public final ApplicationSPI application()
    {
        return applicationInstance;
    }

    public final Module module()
    {
        return moduleInstance;
    }

    public final TransientBuilderFactory transientBuilderFactory()
    {
        return moduleInstance.transientBuilderFactory();
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

    public final ValueBuilderFactory valueBuilderFactory()
    {
        return moduleInstance.valueBuilderFactory();
    }

    public final QueryBuilderFactory queryBuilderFactory()
    {
        return moduleInstance.queryBuilderFactory();
    }
}
