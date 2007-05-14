/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.api.persistence.modifier;

import org.qi4j.api.annotation.Modifies;
import org.qi4j.api.persistence.ObjectNotFoundException;
import org.qi4j.api.persistence.PersistentStorage;
import org.qi4j.api.persistence.binding.PersistenceBinding;

/**
 * This modifier traces calls to a persistent storage
 */
public final class PersistentStorageTraceModifier
    implements PersistentStorage
{
    private static boolean enabled = true;
    
    @Modifies private PersistentStorage storage;

    public void create( PersistenceBinding aProxy )
    {
        storage.create( aProxy );
        if( enabled )
        {
            System.out.println( "Created " + aProxy.getIdentity() );
        }
    }

    public void read( PersistenceBinding aProxy ) throws ObjectNotFoundException
    {
        storage.read( aProxy );
        if( enabled )
        {
            System.out.println( "Read " + aProxy.getIdentity() );
        }
    }

    public void update( PersistenceBinding aProxy, Object aMixin )
    {
        storage.update( aProxy, aMixin );
        if( enabled )
        {
            System.out.println( "Updated mixin " + aMixin.getClass().getSimpleName() + " for " + aProxy.getIdentity() );
        }
    }

    public void delete( PersistenceBinding aProxy )
    {
        storage.delete( aProxy );
        if( enabled )
        {
            System.out.println( "Deleted " + aProxy.getIdentity() );
        }
    }

    public static boolean isEnabled()
    {
        return enabled;
    }

    public static void setEnabled( boolean enable )
    {
        enabled = enable;
    }
}
