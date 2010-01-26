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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.PassivationException;

/**
 * This class will manage a set of Activatable instances.
 */
public final class Activator
{
    private LinkedList<Activatable> active = new LinkedList<Activatable>();

    public void activate( List<? extends Activatable> activatables )
        throws Exception
    {
        try
        {
            for( Activatable activatable : activatables )
            {
                activate( activatable );
            }
        }
        catch( Exception e )
        {
            // Passivate actives
            passivate();
            throw e;
        }
    }

    public void activate( Activatable activatable )
        throws Exception
    {
        if( !active.contains( activatable ) )
        {
            activatable.activate();
            active.addFirst( activatable );
        }
    }

    public void passivate()
        throws Exception
    {
        ArrayList<Exception> exceptions = new ArrayList<Exception>();
        while( !active.isEmpty() )
        {
            passivate( exceptions );
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

    private void passivate( ArrayList<Exception> exceptions )
    {
        Activatable activatable = active.removeFirst();
        try
        {
            activatable.passivate();
        }
        catch( Exception e )
        {
            if( e instanceof PassivationException )
            {
                exceptions.addAll( Arrays.asList( ( (PassivationException) e ).causes() ) );
            }
            else
            {
                exceptions.add( e );
            }
        }
    }

    public boolean isActive()
    {
        return !active.isEmpty();
    }
}
