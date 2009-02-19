/*
 * Copyright 2009 Niclas Hedhman.
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
package org.qi4j.library.jini.importer;

import java.io.IOException;
import java.util.Iterator;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.library.jini.importer.org.qi4j.library.jini.tests.InterpreterService;

/**
 * This testcase starts an external non-Qi4j, Jini application and then makes sure that such Jini service will appear
 * as a Qi4j service via the ServiceImporter mechanism.
 */
public class ImportExternalJiniServiceTest
{
    private Process process;

    @Ignore("Doesn't seem to work right now")
    @Test
    public void givenExternalJiniServicePresentInitiallyWhenQi4jApplicationStartsExpectServiceToBeImported()
        throws IOException, InterruptedException
    {
        startExternalJiniService();

        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.importServices( InterpreterService.class ).importedBy( JiniImporter.class );
            }
        };
        final ServiceFinder finder = assembler.serviceFinder();
        final Iterable<ServiceReference<InterpreterService>> iterable = finder.findServices( InterpreterService.class );
        final Iterator<ServiceReference<InterpreterService>> iterator = iterable.iterator();
        final String serviceName = InterpreterService.class.getName();
        assertTrue( "Service proxy not found: " + serviceName, iterator.hasNext() );
//        assertTrue( "Service is not active: " + serviceName, iterator.next().isActive() );
        InterpreterService service = iterator.next().get();
        service.push( 123 );
        service.push( 321 );
        service.addition();
        assertEquals( 444, (int) service.popLong() );
        shutdownExternalJiniService();
    }

    private void startExternalJiniService()
        throws IOException
    {
        String[] args = { "org.qi4j.library.jini.tests.Main" };
        process = Runtime.getRuntime().exec( "java", args );
    }

    private void shutdownExternalJiniService()
        throws IOException, InterruptedException
    {
        process.destroy();
        process.waitFor();
    }
}
