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

package org.apache.polygene.bootstrap;

import org.apache.polygene.api.PolygeneAPI;
import org.apache.polygene.api.composite.InvalidCompositeException;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.ApplicationDescriptor;
import org.apache.polygene.spi.PolygeneSPI;

/**
 * Main bootstrap class for starting Polygene and creating new applications.
 * <p>
 * Instantiate this and call one of the factory methods to get started.
 * </p>
 * <p>
 * This class will use the Service Loader mechanism in Java to try to locate a runtime that implements
 * the PolygeneRuntime interface. This avoids a direct dependency from the bootstrap to the runtime.
 * </p>
 */
public final class Energy4Java
{
    private PolygeneRuntime runtime;

    public Energy4Java( RuntimeFactory runtimeFactory )
    {
        this( runtimeFactory.createRuntime() );
    }

    public Energy4Java()
    {
        this( new RuntimeFactory.StandaloneApplicationRuntimeFactory().createRuntime() );
    }

    public Energy4Java( PolygeneRuntime runtime )
    {
        if( runtime == null )
        {
            throw new BootstrapException( "Can not create Polygene without a Polygene Runtime." );
        }
        this.runtime = runtime;
    }

    public ApplicationDescriptor newApplicationModel( ApplicationAssembler assembler )
        throws AssemblyException
    {
        ApplicationAssembly assembly = assembler.assemble( runtime.applicationAssemblyFactory() );

        if( assembly == null )
        {
            throw new AssemblyException( "Application assembler did not return any ApplicationAssembly" );
        }

        try
        {
            ApplicationModelFactory modelFactory = runtime.applicationModelFactory();
            ApplicationDescriptor model = modelFactory.newApplicationModel( assembly );
            String modelReport = InvalidCompositeException.modelReport();
            if( modelReport != null )
            {
                String nl = System.getProperty( "line.separator" );
                throw new AssemblyException( "Composition problems" + nl + nl + modelReport );
            }
            return model;
        }
        catch( AssemblyReportException e )
        {
            e.attacheModelReport( InvalidCompositeException.modelReport() );
            throw e;
        }
        catch( AssemblyException e )
        {
            throw e;
        }
        catch( RuntimeException e )
        {
            throw new AssemblyException( "Unable to create Application Model.", e );
        }
    }

    public Application newApplication( ApplicationAssembler assembler, Object... importedServiceInstances )
        throws AssemblyException
    {
        ApplicationDescriptor model = newApplicationModel( assembler );
        return model.newInstance( runtime.spi(), importedServiceInstances );
    }

    public PolygeneSPI spi()
    {
        return runtime.spi();
    }

    public PolygeneAPI api()
    {
        return runtime.spi();
    }
}
