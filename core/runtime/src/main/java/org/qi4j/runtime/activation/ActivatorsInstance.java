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

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.activation.Activator;
import org.qi4j.api.service.PassivationException;

public class ActivatorsInstance<T>
        implements Activator<T>
{

    private final List<Activator<T>> activators;

    public ActivatorsInstance( List<Activator<T>> activators )
    {
        this.activators = activators;
    }

    public void beforeActivation( T activating )
            throws Exception
    {
        for( Activator<T> activator : activators ) {
            activator.beforeActivation( activating );
        }
    }

    public void afterActivation( T activated )
            throws Exception
    {
        for( Activator<T> activator : activators ) {
            activator.afterActivation( activated );
        }
    }

    public void beforePassivation( T passivating )
            throws Exception
    {
        List<Exception> exceptions = new ArrayList<Exception>();
        for( Activator<T> activator : activators ) {
            try
            {
                activator.beforePassivation( passivating );
            }
            catch( Exception ex ) {
                exceptions.add( ex );
            }
        }
        if( exceptions.isEmpty() )
        {
            return;
        }
        if( exceptions.size() == 1 )
        {
            throw exceptions.get( 0 );
        }
        throw new PassivationException( exceptions );
    }

    public void afterPassivation( T passivated )
            throws Exception
    {
        List<Exception> exceptions = new ArrayList<Exception>();
        for( Activator<T> activator : activators ) {
            try
            {
                activator.afterPassivation( passivated );
            }
            catch( Exception ex ) {
                exceptions.add( ex );
            }
        }
        if( exceptions.isEmpty() )
        {
            return;
        }
        if( exceptions.size() == 1 )
        {
            throw exceptions.get( 0 );
        }
        throw new PassivationException( exceptions );
    }

}
