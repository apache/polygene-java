/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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
package org.qi4j.library.registry;

import java.util.HashMap;
import java.util.LinkedList;
import org.qi4j.injection.scope.Service;
import org.qi4j.library.exception.ExceptionHandling;

public class RegistryMixin<K, V>
    implements Registry<K, V>
{
    @Service( optional = true ) private ExceptionHandling exceptionHandling;

    private final HashMap<K, V> registrations;
    private LinkedList<RegistryObserver<K, V>> observers;

    public RegistryMixin()
    {
        this.registrations = new HashMap<K, V>();
    }

    public V lookup( K key )
    {
        V compositeType;
        synchronized( registrations )
        {
            compositeType = registrations.get( key );
        }
        return compositeType;
    }

    public void register( K key, V value )
    {
        synchronized( registrations )
        {
            registrations.put( key, value );
            for( RegistryObserver<K, V> observer : observers )
            {
                sendNotification( key, value, observer );
            }
        }
    }

    public void unregister( K key )
    {
        synchronized( registrations )
        {
            registrations.remove( key );
        }
    }

    public void addRegistryObserver( RegistryObserver<K, V> observer )
    {
        synchronized( this )
        {
            LinkedList<RegistryObserver<K, V>> clone;
            clone = new LinkedList<RegistryObserver<K, V>>();
            if( observers != null )
            {
                clone.addAll( observers );
            }
            clone.add( observer );
            observers = clone;
        }
    }

    public void removeRegistryObserver( RegistryObserver<K, V> observer )
    {
        synchronized( this )
        {
            if( observers == null )
            {
                return;
            }
            if( observers.contains( observer ) )
            {
                if( observers.size() == 1 )
                {
                    observers = null;
                    return;
                }
                LinkedList<RegistryObserver<K, V>> clone = new LinkedList<RegistryObserver<K, V>>();
                clone.addAll( observers );
                observers = clone;
            }
        }
    }

    private void sendNotification( K key, V value, RegistryObserver<K, V> observer )
    {
        try
        {
            observer.registration( this, key, value );
        }
        catch( Exception e )
        {
            if( exceptionHandling != null )
            {
                exceptionHandling.exceptionOccurred( "Observer " + observer + " threw an exception", this, e );
            }
        }
    }
}
