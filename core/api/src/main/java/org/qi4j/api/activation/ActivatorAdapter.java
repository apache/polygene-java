/*
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
package org.qi4j.api.activation;

/**
 * Adapter for Activator.
 *
 * @param ActivateeType Type of the activatee.
 */
public class ActivatorAdapter<ActivateeType>
    implements Activator<ActivateeType>
{

    /**
     * Called before activatee activation.
     */
    @Override
    public void beforeActivation( ActivateeType activating )
        throws Exception
    {
    }

    /**
     * Called after activatee activation.
     */
    @Override
    public void afterActivation( ActivateeType activated )
        throws Exception
    {
    }

    /**
     * Called before activatee passivation.
     */
    @Override
    public void beforePassivation( ActivateeType passivating )
        throws Exception
    {
    }

    /**
     * Called after activatee passivation.
     */
    @Override
    public void afterPassivation( ActivateeType passivated )
        throws Exception
    {
    }
}
