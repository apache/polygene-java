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

package org.qi4j.core.test.osgi.internal;

import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.entity.helpers.UuidIdentityGeneratorService;
import org.qi4j.spi.structure.ApplicationSPI;

public final class Activator
    implements BundleActivator
{
    private static final String MODULE_NAME = "Single Module.";
    private static final String LAYER_NAME = "Single Layer.";

    private ApplicationSPI application;
    private ServiceRegistration moduleRegistration;

    public void start( BundleContext bundleContext )
        throws Exception
    {
        Energy4Java boot = new Energy4Java();
        ApplicationAssembler assembler = new MyApplicationAssembler();
        application = boot.newApplication( assembler );
        application.activate();

        Module module = application.findModule( LAYER_NAME, MODULE_NAME );
        moduleRegistration = bundleContext.registerService( Module.class.getName(), module, new Hashtable() );
    }

    public void stop( BundleContext bundleContext )
        throws Exception
    {
        moduleRegistration.unregister();
        application.passivate();

        moduleRegistration = null;
        application = null;
    }


    private static class MyApplicationAssembler
        implements ApplicationAssembler
    {
        public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
            throws AssemblyException
        {
            ApplicationAssembly applicationAssembly = applicationFactory.newApplicationAssembly();
            LayerAssembly layerAssembly = applicationAssembly.newLayerAssembly( LAYER_NAME );
            ModuleAssembly moduleAssembly = layerAssembly.newModuleAssembly( MODULE_NAME );

            moduleAssembly.addComposites( APrivateComposite.class );
            moduleAssembly.addEntities( AnEntityComposite.class );
            moduleAssembly.addServices( MemoryEntityStoreService.class );
            moduleAssembly.addServices( UuidIdentityGeneratorService.class );

            return applicationAssembly;
        }
    }
}
