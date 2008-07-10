/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.quikit.application.jetty;

import java.util.Iterator;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.quikit.application.ServletInfo;
import org.qi4j.quikit.assembly.composites.HttpConfiguration;
import org.qi4j.service.ServiceReference;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.Test;

/**
 * @author edward.yakop@gmail.com
 */
public class JettyServiceTest extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly aModule )
        throws AssemblyException
    {
        aModule.addServices( JettyService.class );
        aModule.addEntities( HttpConfiguration.class );
        aModule.on( HttpConfiguration.class ).to()
            .hostPort().set( 8080 );
    }

    @Test
    public final void testInstantiation()
    {
        Iterable<ServiceReference<JettyService>> services =
            serviceLocator.findServices( JettyService.class );
        assertNotNull( services );

        Iterator<ServiceReference<JettyService>> iterator = services.iterator();
        assertTrue( iterator.hasNext() );

        ServiceReference<JettyService> serviceRef = iterator.next();
        assertNotNull( serviceRef );

        JettyService jettyService = serviceRef.get();
        assertNotNull( jettyService );
    }
}
