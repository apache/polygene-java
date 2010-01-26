/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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

package org.qi4j.bootstrap;

import java.io.IOException;
import org.qi4j.bootstrap.internal.ServiceLoader;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.structure.ApplicationModelSPI;
import org.qi4j.spi.structure.ApplicationSPI;

/**
 * Main bootstrap class for starting Qi4j and creating new applications. Instantiate this
 * and call one of the factory methods to get started.
 * <p/>
 * This class will use the Service Loader mechanism in Java to try to locate a runtime that implements
 * the Qi4jRuntime interface. This avoids a direct dependency from the bootstrap to the runtime.
 */
public final class Energy4Java
{
    private static ServiceLoader serviceLoader;

    private Qi4jRuntime runtime;

    static
    {
        serviceLoader = new ServiceLoader();
    }

    public static ServiceLoader getServiceLoader()
    {
        return serviceLoader;
    }

    public static void setServiceLoader( ServiceLoader aServiceLoader )
    {
        serviceLoader = aServiceLoader;
    }

    public Energy4Java()
    {
        this( findQi4jRuntime() );
    }

    public Energy4Java( Qi4jRuntime runtime )
    {
        this.runtime = runtime;
    }

    public ApplicationModelSPI newApplicationModel( ApplicationAssembler assembler )
        throws AssemblyException
    {
        ApplicationAssembly assembly = assembler.assemble( runtime.applicationAssemblyFactory() );
        ApplicationModelFactory modelFactory = runtime.applicationModelFactory();
        return modelFactory.newApplicationModel( assembly );
    }

    public ApplicationSPI newApplication( ApplicationAssembler assembler )
        throws AssemblyException
    {
        ApplicationModelSPI model = newApplicationModel( assembler );
        return model.newInstance( runtime.spi() );
    }

    public Qi4jSPI spi()
    {
        return runtime.spi();
    }

    private static Qi4jRuntime findQi4jRuntime()
        throws BootstrapException
    {

        try
        {
            final Qi4jRuntime runtime = serviceLoader.firstProvider( Qi4jRuntime.class );
            if( runtime != null )
            {
                return runtime;
            }
            throw new BootstrapException( "No Qi4j runtime providers found." );
        }
        catch( IOException e )
        {
            throw new BootstrapException( "Unable to load a Qi4j runtime provider.", e );
        }
    }

    public static void notifyRuntimeShutdown()
    {
        // TODO What????
    }
}
