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
package org.apache.polygene.runtime.activation;

import org.apache.polygene.api.activation.Activator;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.Layer;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class LayerActivationTest
{

    private static int activationLevel = 0;

    private static int passivationLevel = 0;

    public static class TestedActivator
        implements Activator<Layer>
    {

        public void beforeActivation( Layer activating )
        {
            activationLevel++;
        }

        public void afterActivation( Layer activated )
        {
            activationLevel++;
        }

        public void beforePassivation( Layer passivating )
        {
            passivationLevel++;
        }

        public void afterPassivation( Layer passivated )
        {
            passivationLevel++;
        }
    }

    @Test
    public void testLayersActivators()
        throws Exception
    {
        SingletonAssembler assembly = new SingletonAssembler(
            module -> module.layer().withActivators( TestedActivator.class )
        );
        // Activate
        Application application = assembly.application();

        // Assert activated
        assertThat( "Activation Level", activationLevel, equalTo( 2 ) );

        // Passivate
        application.passivate();

        // Assert passivated
        assertThat( "Passivation Level", passivationLevel, equalTo( 2 ) );
    }
}
