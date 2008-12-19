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
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.spi.entity.helpers.UuidIdentityGeneratorService;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;

public final class Activator
    implements BundleActivator
{
    private static final String MODULE_NAME = "Single Module.";
    private static final String LAYER_NAME = "Single Layer.";

    private Application application;
    private ServiceRegistration moduleRegistration;

    public void start( BundleContext bundleContext )
        throws Exception
    {
        Energy4Java boot = new Energy4Java();
        Assembler assembler = new ApplicationAssembler();
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


    private static class ApplicationAssembler
        implements Assembler
    {
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.layerAssembly().setName( LAYER_NAME );
            module.setName( MODULE_NAME );

            module.addComposites( APrivateComposite.class );
            module.addEntities( AnEntityComposite.class );
            module.addServices( MemoryEntityStoreService.class );
            module.addServices( UuidIdentityGeneratorService.class );
        }
    }
}
