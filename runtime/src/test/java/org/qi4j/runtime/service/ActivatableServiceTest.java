/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.service;

import org.junit.Test;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

import static org.junit.Assert.*;

/**
 * Test of activatable services
 */
public class ActivatableServiceTest
{
    @Service
    ServiceReference<Activatable> service;

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
                module.addObjects( ActivatableServiceTest.class );
                module.addServices( ActivatableComposite.class ).instantiateOnStartup();
            }
        };

        assertTrue( isActive );

        assembly.objectBuilderFactory().newObjectBuilder( ActivatableServiceTest.class ).injectTo( this );

        assertTrue( isActive );

        service.get();

        assertTrue( isActive );

        assembly.application().passivate();

        assertFalse( isActive );
    }

    @Mixins( ActivatableMixin.class )
    public static interface ActivatableComposite
        extends Activatable, ServiceComposite
    {
    }

    public static class ActivatableMixin
        implements Activatable
    {
        public void activate()
            throws Exception
        {
            isActive = true;
        }

        public void passivate()
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
