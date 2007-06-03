/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.extension.persistence.jdbm;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import jdbm.RecordManager;
import org.qi4j.api.persistence.binding.PersistenceBinding;
import org.qi4j.runtime.ObjectInvocationHandler;

class CreateOperation
    implements Operation
{
    private PersistenceBinding binding;

    public CreateOperation( PersistenceBinding binding )
    {
        this.binding = binding;
    }

    public void perform( RecordManager recordManager )
    {
        ObjectInvocationHandler handler = ObjectInvocationHandler.getInvocationHandler( binding );
        Map<Class, Object> mixins = handler.getMixins();

        Map<Class, Serializable> persistentMixins = new HashMap<Class, Serializable>();
        for( Map.Entry<Class, Object> entry : mixins.entrySet() )
        {
            Object value = entry.getValue();
            if( value instanceof Serializable )
            {
                persistentMixins.put( entry.getKey(), (Serializable) value );
            }
        }
        String objectId = binding.getIdentity();
        try
        {
            long recordId = recordManager.insert( persistentMixins );
            recordManager.setNamedObject( objectId, recordId );
        }
        catch( IOException e )
        {
            throw new CreateOperationException( "Unable to insert object [" + objectId + "] into storage.", e );
        }
    }

    public String getIdentity()
    {
        return binding.getIdentity();
    }

    public void playback( String identity, Map<Class, Object> newMixinsToPopulate )
    {
        if( identity.equals( binding.getIdentity() ) && newMixinsToPopulate != null )
        {
            ObjectInvocationHandler handler = ObjectInvocationHandler.getInvocationHandler( binding );
            Map<Class, Object> mixins = handler.getMixins();

            for( Map.Entry<Class, Object> entry : mixins.entrySet() )
            {
                Object value = entry.getValue();
                if( value instanceof Serializable )
                {
                    newMixinsToPopulate.put( entry.getKey(), (Serializable) value );
                }
            }
        }
    }
}
