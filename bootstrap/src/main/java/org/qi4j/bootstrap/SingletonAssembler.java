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
import org.qi4j.api.composite.CompositeBuilderFactory;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

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
    private Energy4Java is;
    private Application applicationInstance;
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
        this( false );
    }

    /**
     * Creates a Qi4j Runtime instance containing one Layer with one Module.
     * The Layer will be named "Layer 1" and the Module will be named "Module 1". It is possible to add
     * additional layers and modules via the Assembler interface that must be implemented in the subclass of this
     * class.
     *
     * @param fastBootSupport If set to true, any existing application instance saved to disk will be retrieved,
     *                        instead of building the model from scratch. If not exist, then the model will be built
     *                        from scratch and then saved to disk for future invocations. If this argument is set to
     *                        true, the caller should ensure that the classes on the classpath are fully-compatible
     *                        with the saved binary. Practically no changes to the application model is compatible with
     *                        a previously stored instance.
     * @throws IllegalStateException Either if the model can not be created from the disk, or some inconsistency in
     *                               the programming model makes it impossible to create it.
     */
    public SingletonAssembler( boolean fastBootSupport )
        throws IllegalStateException
    {
        is = new Energy4Java();
        try
        {
            if( fastBootSupport )
            {
                try
                {
                    applicationInstance = is.loadApplication();
                }
                catch( HibernatingApplicationInvalidException e )
                {
                    e.printStackTrace();
                }
            }
            if( applicationInstance == null )
            {
                applicationInstance = is.newApplication( this );
            }
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
        return is.runtime();
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
