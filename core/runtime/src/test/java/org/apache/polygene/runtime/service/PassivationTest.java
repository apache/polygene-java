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

package org.apache.polygene.runtime.service;

import java.util.ArrayList;
import org.apache.polygene.api.activation.ActivatorAdapter;
import org.apache.polygene.api.activation.PassivationException;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

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

        assembly.module().findServices( DataAccess.class ).forEach(
            service ->
            {
                assertThat( "Service should not be Active before accessed", !service.isActive(), is( true ) );
                assertThat( service.get().data().activated, is( true ) );
                assertThat( "Service should be Active after access.", service.isActive(), is( true ) );
            }
        );
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

        assembly.module().findServices( DataAccess.class ).forEach(
            service ->
            {
                assertThat( "Service should not be Active before accessed", !service.isActive(), is( true ) );
                Data data = service.get().data();
                if( DataAccessService.class.isInstance( service.get() ) )
                {
                    // Collect the expected successes.
                    datas.add( data );
                }
                assertThat( "Data should indicate that the service is activated", data.activated, is( true ) );
                assertThat( "Service should be Active after access.", service.isActive(), is( true ) );
            }
        );
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
        assembly.module().findServices( DataAccess.class ).forEach(
            service ->
            {
                assertThat( "All services should have been shutdown", service.isActive(), is( false ) );
            }
        );
    }

    @Test
    public void givenMultipleFailingPassivationWhenPassivatingExpectPassivationExceptionToBubbleUp()
        throws Exception
    {
        assertThrows( PassivationException.class, () -> {
            SingletonAssembler assembly = new SingletonAssembler()
            {
                public void assemble( ModuleAssembly module )
                    throws AssemblyException
                {
                    module.addServices( DataAccessService.class ).withActivators( PassivationFailureActivator.class );
                    module.addServices( DataAccessService.class ).withActivators( PassivationFailureActivator.class );
                }
            };

            assembly.module().findServices( DataAccess.class ).forEach(
                service ->
                {
                    assertThat( "Service should not be Active before accessed", !service.isActive(), is( true ) );
                    assertThat( service.get().data().activated, is( true ) );
                    assertThat( "Service should be Active after access.", service.isActive(), is( true ) );
                }
            );
            assembly.application().passivate();
        } );
    }

    @Mixins( DataAccessMixin.class )
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
