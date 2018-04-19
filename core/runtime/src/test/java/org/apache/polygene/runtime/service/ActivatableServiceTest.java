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

import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Test of activatable services
 */
public class ActivatableServiceTest
{
    @Service
    ServiceReference<ActivatableComposite> service;

    public static boolean isActive;

    @Test
    public void testActivatable()
        throws Exception
    {
        SingletonAssembler assembly = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.objects( ActivatableServiceTest.class );
                module.services( ActivatableComposite.class )
                    .withActivators( TestActivator.class )
                    .instantiateOnStartup();
            }
        };

        assertThat( isActive, is( true ) );

        assembly.module().injectTo( this );

        assertThat( isActive, is( true ) );

        service.get();

        assertThat( isActive, is( true ) );

        assembly.application().passivate();

        assertThat( isActive, is( false ) );
    }

    public static interface ActivatableComposite
        extends ServiceComposite
    {
    }

    public static class TestActivator
        extends org.apache.polygene.api.activation.ActivatorAdapter<Object>
    {

        @Override
        public void afterActivation( Object activated )
        {
            isActive = true;
        }

        @Override
        public void afterPassivation( Object passivated )
            throws Exception
        {
            if( !isActive )
            {
                throw new Exception( "Not active!" );
            }
            isActive = false;
        }
    }
}
