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

package org.qi4j.runtime.service;

import java.util.ArrayList;
import org.junit.Test;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.PassivationException;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

import static org.junit.Assert.*;

public class PassivationTest
{

    @Test
    public void givenSuccessPassivationWhenPassivatingExpectNoExceptions()
        throws Exception
    {
        SingletonAssembler assembly = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addServices( SuccessDataService.class );
                module.addServices( SuccessDataService.class );
            }
        };

        Iterable<ServiceReference<DataAccess>> iterable = assembly.serviceFinder().findServices( DataAccess.class );
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
                module.addServices( SuccessDataService.class );
                module.addServices( SuccessDataService.class );
                module.addServices( SuccessDataService.class );
                module.addServices( FailingDataService.class );
                module.addServices( SuccessDataService.class );
                module.addServices( SuccessDataService.class );
                module.addServices( FailingDataService.class );
                module.addServices( SuccessDataService.class );
                module.addServices( SuccessDataService.class );
                module.addServices( FailingDataService.class );
                module.addServices( SuccessDataService.class );
                module.addServices( SuccessDataService.class );
                module.addServices( FailingDataService.class );
            }
        };

        ArrayList<Data> datas = new ArrayList<Data>();

        Iterable<ServiceReference<DataAccess>> iterable = assembly.serviceFinder().findServices( DataAccess.class );
        for( ServiceReference<DataAccess> service : iterable )
        {
            assertTrue( "Service should not be Active before accessed", !service.isActive() );
            Data data = service.get().data();
            if( SuccessDataService.class.isInstance( service.get() ) )
            {
                // Collect the expected successes.
                datas.add( data );
            }
            assertTrue( data.activated );
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

        // Still ensure that all services has been shutdown.
        for( Data data : datas )
        {
            assertTrue( !data.activated );
        }
    }

    @Test
    public void givenFailingPassivationWhenPassivatingExpectSingleExceptionToBubbleUp()
        throws Exception
    {
        SingletonAssembler assembly = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addServices( FailingDataService.class );
            }
        };

        ServiceReference<DataAccess> service = assembly.serviceFinder().findService( DataAccess.class );
        assertTrue( "Service should not be Active before accessed", !service.isActive() );
        assertTrue( service.get().data().activated );
        assertTrue( "Service should be Active after access.", service.isActive() );

        try
        {
            assembly.application().passivate();
            fail( "Exception should have been thrown." );
        }
        catch( IllegalStateException e )
        {
            // Expected
        }
    }

    @Test
    public void givenMultipleFailingPassivationWhenPassivatingExpectPassivationExceptionToBubbleUp()
        throws Exception
    {
        SingletonAssembler assembly = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addServices( FailingDataService.class );
                module.addServices( FailingDataService.class );
            }
        };

        Iterable<ServiceReference<DataAccess>> iterable = assembly.serviceFinder().findServices( DataAccess.class );
        for( ServiceReference<DataAccess> service : iterable )
        {
            assertTrue( "Service should not be Active before accessed", !service.isActive() );
            assertTrue( service.get().data().activated );
            assertTrue( "Service should be Active after access.", service.isActive() );
        }
        try
        {
            assembly.application().passivate();
            fail( "Exception should have been thrown." );
        }
        catch( PassivationException e )
        {
            // Expected
        }
    }

    @Mixins( PassivationFailureMixin.class )
    public interface FailingDataService
        extends DataAccess, ServiceComposite
    {
    }

    @Mixins( PassivationSuccessMixin.class )
    public interface SuccessDataService
        extends DataAccess, ServiceComposite
    {
    }

    public interface DataAccess
    {
        Data data();
    }

    public static class PassivationFailureMixin
        implements DataAccess, Activatable
    {
        Data data = new Data();

        public void activate()
            throws Exception
        {
            data.activated = true;
        }

        public void passivate()
            throws Exception
        {
            throw new IllegalStateException();
        }

        public Data data()
        {
            return data;
        }
    }

    public static class PassivationSuccessMixin
        implements DataAccess, Activatable
    {
        Data data = new Data();

        public void activate()
            throws Exception
        {
            data.activated = true;
        }

        public void passivate()
            throws Exception
        {
            data.activated = false;
        }

        public Data data()
        {
            return data;
        }
    }

    public static class Data
    {
        boolean activated = false;
    }
}
