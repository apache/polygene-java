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

package org.qi4j.service;

import junit.framework.TestCase;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.scope.Service;

/**
 * TODO
 */
public class ActivatableServiceTest
    extends TestCase
{
    @Service ServiceReference<Activatable> service;

    public static boolean isActive;

    public void testActivatable()
        throws Exception
    {
        SingletonAssembler assembly = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addObjects( ActivatableServiceTest.class );
                module.addServices( ActivatableComposite.class ).activateOnStartup();
            }
        };

        assertTrue( isActive );

        assembly.getObjectBuilderFactory().newObjectBuilder( ActivatableServiceTest.class ).inject( this );

        assertTrue( isActive );

        service.getService();

        assertTrue( isActive );

        assembly.getApplicationInstance().passivate();

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
        public void activate() throws Exception
        {
            isActive = true;
        }

        public void passivate() throws Exception
        {
            if( isActive == false )
            {
                throw new Exception( "Not active!" );
            }

            isActive = false;
        }
    }
}
