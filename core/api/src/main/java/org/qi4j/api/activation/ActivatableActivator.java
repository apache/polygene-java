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
package org.qi4j.api.activation;

import org.qi4j.api.service.Activatable;

public final class ActivatableActivator
        implements Activator<Activatable>
{

    public void beforeActivation( Activatable activating )
            throws Exception
    {
        // TODO What to do here ?
    }

    public void afterActivation( Activatable activated )
            throws Exception
    {
        // TODO What to do here ?
        activated.activate();
    }

    public void beforePassivation( Activatable passivating )
            throws Exception
    {
        // TODO What to do here ?
        passivating.passivate();
    }

    public void afterPassivation( Activatable passivated )
            throws Exception
    {
        // TODO What to do here ?
    }

}
