/*
 * Copyright (c) 2012, Paul Merlin.
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
package org.qi4j.runtime.activation;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.activation.Activator;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

public class ApplicationActivationTest
{

    private static int activationLevel = 0;

    private static int passivationLevel = 0;

    public static class TestedActivator
            implements Activator<Application>
    {

        public void beforeActivation( Application activating )
        {
            activationLevel++;
        }

        public void afterActivation( Application activated )
        {
            activationLevel++;
        }

        public void beforePassivation( Application passivating )
        {
            passivationLevel++;
        }

        public void afterPassivation( Application passivated )
        {
            passivationLevel++;
        }

    }

    @Test
    public void testApplicationActivator()
            throws Exception
    {
        SingletonAssembler assembly = new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                module.layer().application().withActivators( TestedActivator.class );
            }

        };

        // Activate
        Application application = assembly.application();

        // Assert activated
        Assert.assertEquals( "Activation Level", 2, activationLevel );

        // Passivate
        application.passivate();

        // Assert passivated
        Assert.assertEquals( "Passivation Level", 2, passivationLevel );
    }

}
