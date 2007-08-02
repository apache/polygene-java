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
import java.lang.reflect.Proxy;
import java.util.Map;
import jdbm.RecordManager;
import org.qi4j.api.persistence.EntityComposite;
import org.qi4j.runtime.ProxyReferenceInvocationHandler;

class UpdateOperation
    implements Operation
{
    private EntityComposite composite;
    private Serializable mixin;

    public UpdateOperation( EntityComposite composite, Serializable mixin )
    {
        this.composite = composite;
        this.mixin = mixin;
    }

    public void perform( RecordManager recordManager )
    {
        ProxyReferenceInvocationHandler handler = (ProxyReferenceInvocationHandler) Proxy.getInvocationHandler( composite );
        String identity = composite.getIdentity();
        try
        {
            long recordId = recordManager.getNamedObject( identity );
            Map<Class, Serializable> mixins = (Map<Class, Serializable>) recordManager.fetch( recordId );
            if( mixins != null )
            {
                Class mixinType = handler.getMixinType();
                Serializable oldValueObject = mixins.get( mixinType );

                // Only update if there already were a value object. Otherwise ignore.
                if( oldValueObject != null )
                {
                    mixins.put( mixinType, mixin );
                    recordManager.update( recordId, mixins );
                }
            }
        }
        catch( IOException e )
        {
            // TODO Better message
            throw new UpdateOperationException( "Unable to update object [" + identity + "]", e );
        }
    }

    public String getIdentity()
    {
        return composite.getIdentity();
    }

    public void playback( String identity, Map<Class, Object> mixins )
    {
        if( mixins == null )
        {
            return;
        }
        ProxyReferenceInvocationHandler handler = (ProxyReferenceInvocationHandler) Proxy.getInvocationHandler( composite );
        if( identity.equals( composite.getIdentity() ) )
        {
            Class mixinType = handler.getMixinType();
            Object oldValueObject = mixins.get( mixinType );

            // Only update if there already were a value object. Otherwise ignore.
            if( oldValueObject != null )
            {
                mixins.put( mixinType, mixin );
            }
        }
    }
}
