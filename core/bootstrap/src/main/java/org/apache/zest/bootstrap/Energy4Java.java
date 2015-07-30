/*
 * Copyright 2008-2011 Niclas Hedhman. All rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package org.apache.zest.bootstrap;

import org.apache.zest.api.ZestAPI;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.ApplicationDescriptor;
import org.apache.zest.spi.ZestSPI;

/**
 * Main bootstrap class for starting Zest and creating new applications.
 * <p>
 * Instantiate this and call one of the factory methods to get started.
 * </p>
 * <p>
 * This class will use the Service Loader mechanism in Java to try to locate a runtime that implements
 * the ZestRuntime interface. This avoids a direct dependency from the bootstrap to the runtime.
 * </p>
 */
public final class Energy4Java
{
    private ZestRuntime runtime;

    public Energy4Java( RuntimeFactory runtimeFactory )
    {
        this( runtimeFactory.createRuntime() );
    }

    public Energy4Java()
    {
        this( new RuntimeFactory.StandaloneApplicationRuntimeFactory().createRuntime() );
    }

    public Energy4Java( ZestRuntime runtime )
    {
        if( runtime == null )
        {
            throw new BootstrapException( "Can not create Zest without a Zest Runtime." );
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
            return modelFactory.newApplicationModel( assembly );
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

    public ZestSPI spi()
    {
        return runtime.spi();
    }

    public ZestAPI api()
    {
        return runtime.spi();
    }
}
