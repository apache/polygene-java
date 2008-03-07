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

package org.qi4j.test;

import junit.framework.TestCase;
import org.qi4j.Qi4j;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.ApplicationFactory;
import org.qi4j.bootstrap.Assembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.entity.EntitySessionFactory;
import org.qi4j.runtime.Energy4Java;
import org.qi4j.runtime.Qi4jRuntime;
import org.qi4j.runtime.structure.ApplicationContext;
import org.qi4j.runtime.structure.ApplicationInstance;
import org.qi4j.runtime.structure.LayerInstance;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.Qi4jSPI;

/**
 * Base class for Composite tests
 */
public abstract class AbstractQi4jTest extends TestCase
    implements Assembly
{
    protected Qi4j api;
    protected Qi4jSPI spi;
    protected Qi4jRuntime runtime;

    protected ApplicationFactory applicationFactory;
    protected ApplicationInstance application;

    protected CompositeBuilderFactory compositeBuilderFactory;
    protected ObjectBuilderFactory objectBuilderFactory;
    protected EntitySessionFactory entitySessionFactory;
    protected ModuleInstance moduleInstance;
    protected LayerInstance layerInstance;

    @Override protected void setUp() throws Exception
    {
        api = spi = runtime = new Energy4Java();
        applicationFactory = new ApplicationFactory( runtime, new ApplicationAssemblyFactory() );
        application = newApplication();
        application.activate();

        // Assume only one module
        layerInstance = application.getLayerInstances().iterator().next();
        moduleInstance = layerInstance.getModuleInstances().iterator().next();
        compositeBuilderFactory = moduleInstance.getCompositeBuilderFactory();
        objectBuilderFactory = moduleInstance.getObjectBuilderFactory();
        entitySessionFactory = moduleInstance.getEntitySessionFactory();
        super.setUp();
    }

    protected ApplicationInstance newApplication()
        throws AssemblyException
    {
        ApplicationContext applicationContext = applicationFactory.newApplication( this );
        return applicationContext.newApplicationInstance( "Test application" );
    }

    @Override protected void tearDown() throws Exception
    {
        if( application != null )
        {
            application.passivate();
        }
        super.tearDown();
    }

}
