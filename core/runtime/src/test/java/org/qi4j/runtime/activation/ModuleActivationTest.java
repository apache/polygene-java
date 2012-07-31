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
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

public class ModuleActivationTest
{

    private static int activationLevel = 0;

    private static int passivationLevel = 0;

    public static class TestedActivator
            implements Activator<Module>
    {

        public void beforeActivation( Module activating )
        {
            activationLevel++;
        }

        public void afterActivation( Module activated )
        {
            activationLevel++;
        }

        public void beforePassivation( Module passivating )
        {
            passivationLevel++;
        }

        public void afterPassivation( Module passivated )
        {
            passivationLevel++;
        }

    }

    @Test
    public void testModulesActivators()
            throws Exception
    {
        SingletonAssembler assembly = new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                module.withActivators( TestedActivator.class );
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
