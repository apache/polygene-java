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
import org.qi4j.persistence.EntityComposite;
import org.qi4j.runtime.CompositeInvocationHandler;

class CreateOperation
    implements Operation
{
    private EntityComposite composite;

    public CreateOperation( EntityComposite composite )
    {
        this.composite = composite;
    }

    public void perform( RecordManager recordManager )
    {
        CompositeInvocationHandler handler = CompositeInvocationHandler.getInvocationHandler( composite );
        Object[] mixins = handler.getMixins();

        Map<Class, Serializable> persistentMixins = new HashMap<Class, Serializable>();
        for( Object mixin : mixins )
        {
            if( mixin instanceof Serializable )
            {
                persistentMixins.put( mixin.getClass(), (Serializable) mixin );
            }
        }
        String objectId = composite.getIdentity();
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
        return composite.getIdentity();
    }

    public void playback( String identity, Object[] newMixinsToPopulate )
    {
        if( identity.equals( composite.getIdentity() ) && newMixinsToPopulate != null )
        {
            CompositeInvocationHandler handler = CompositeInvocationHandler.getInvocationHandler( composite );
            Object[] mixins = handler.getMixins();

            // TODO Needs to be fixed
            int i = 0;
            for( Object mixin : mixins )
            {
                if( mixin instanceof Serializable )
                {
                    newMixinsToPopulate[ i ] = mixin;
                }
                i++;
            }
        }
    }
}
