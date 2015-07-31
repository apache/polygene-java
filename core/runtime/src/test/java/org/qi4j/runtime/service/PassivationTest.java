/*
 * Copyright 2009 Niclas Hedhman.
 * Copyright 2012 Paul Merlin.
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

package org.qi4j.runtime.service;

import java.util.ArrayList;
import org.junit.Test;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PassivationTest
{

    @Test
    public void givenSuccessPassivationWhenPassivatingExpectNoExceptions()
        throws Throwable
    {
        SingletonAssembler assembly = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addServices( DataAccessService.class ).withActivators( PassivationSuccessActivator.class );
                module.addServices( DataAccessService.class ).withActivators( PassivationSuccessActivator.class );
            }
        };

        Iterable<ServiceReference<DataAccess>> iterable = assembly.module().findServices( DataAccess.class );
        for( ServiceReference<DataAccess> service : iterable )
        {
            assertTrue( "Service should not be Active before accessed", !service.isActive() );
            assertTrue( service.get().data().activated );
            assertTrue( "Service should be Active after access.", service.isActive() );
        }
        assembly.application().passivate();
    }

    @Test
    public void givenMixedSuccessFailurePassivationWhenPassivatingExpectAllPassivateMethodsToBeCalled()
        throws Exception
    {
        SingletonAssembler assembly = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addServices( DataAccessService.class ).withActivators( PassivationSuccessActivator.class );
                module.addServices( DataAccessService.class ).withActivators( PassivationSuccessActivator.class );
                module.addServices( DataAccessService.class ).withActivators( PassivationSuccessActivator.class );
                module.addServices( DataAccessService.class ).withActivators( PassivationFailureActivator.class );
                module.addServices( DataAccessService.class ).withActivators( PassivationSuccessActivator.class );
                module.addServices( DataAccessService.class ).withActivators( PassivationSuccessActivator.class );
                module.addServices( DataAccessService.class ).withActivators( PassivationFailureActivator.class );
                module.addServices( DataAccessService.class ).withActivators( PassivationSuccessActivator.class );
                module.addServices( DataAccessService.class ).withActivators( PassivationSuccessActivator.class );
                module.addServices( DataAccessService.class ).withActivators( PassivationFailureActivator.class );
                module.addServices( DataAccessService.class ).withActivators( PassivationSuccessActivator.class );
                module.addServices( DataAccessService.class ).withActivators( PassivationSuccessActivator.class );
                module.addServices( DataAccessService.class ).withActivators( PassivationFailureActivator.class );
            }
        };

        ArrayList<Data> datas = new ArrayList<Data>();

        Iterable<ServiceReference<DataAccess>> iterable = assembly.module().findServices( DataAccess.class );
        for( ServiceReference<DataAccess> service : iterable )
        {
            assertTrue( "Service should not be Active before accessed", !service.isActive() );
            Data data = service.get().data();
            if( DataAccessService.class.isInstance( service.get() ) )
            {
                // Collect the expected successes.
                datas.add( data );
            }
            assertTrue( "Data should indicate that the service is activated", data.activated );
            assertTrue( "Service should be Active after access.", service.isActive() );
        }
        try
        {
            assembly.application().passivate();
            fail( "PassivationException should have been thrown." );
        }
        catch( PassivationException e )
        {
            // Expected
        }

        // Still ensure that all services have been shutdown.
        for( ServiceReference<DataAccess> service : iterable )
        {
            assertFalse( "All services should have been shutdown", service.isActive() );
        }
    }

    @Test(expected = PassivationException.class)
    public void givenMultipleFailingPassivationWhenPassivatingExpectPassivationExceptionToBubbleUp()
        throws Exception
    {
        SingletonAssembler assembly = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addServices( DataAccessService.class ).withActivators( PassivationFailureActivator.class );
                module.addServices( DataAccessService.class ).withActivators( PassivationFailureActivator.class );
            }
        };

        Iterable<ServiceReference<DataAccess>> iterable = assembly.module().findServices( DataAccess.class );
        for( ServiceReference<DataAccess> service : iterable )
        {
            assertTrue( "Service should not be Active before accessed", !service.isActive() );
            assertTrue( service.get().data().activated );
            assertTrue( "Service should be Active after access.", service.isActive() );
        }
        assembly.application().passivate();
    }

    @Mixins(DataAccessMixin.class)
    public interface DataAccessService
        extends DataAccess, ServiceComposite
    {
    }

    public interface DataAccess
    {
        Data data();
    }

    public static class DataAccessMixin
        implements DataAccess
    {
        Data data = new Data();

        public Data data()
        {
            return data;
        }
    }

    public static class PassivationSuccessActivator
        extends ActivatorAdapter<ServiceReference<DataAccess>>
    {

        @Override
        public void afterActivation( ServiceReference<DataAccess> activated )
            throws Exception
        {
            activated.get().data().activated = true;
        }

        @Override
        public void beforePassivation( ServiceReference<DataAccess> passivating )
            throws Exception
        {
            passivating.get().data().activated = false;
        }
    }

    public static class PassivationFailureActivator
        extends ActivatorAdapter<ServiceReference<DataAccess>>
    {

        @Override
        public void afterActivation( ServiceReference<DataAccess> activated )
            throws Exception
        {
            activated.get().data().activated = true;
        }

        @Override
        public void beforePassivation( ServiceReference<DataAccess> passivating )
            throws Exception
        {
            throw new IllegalStateException();
        }
    }

    public static class Data
    {
        boolean activated = false;
    }
}
