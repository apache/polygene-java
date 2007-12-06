/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.spi.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Proxy;
import org.qi4j.Qi4j;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.InvalidApplicationException;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntitySession;
import org.qi4j.spi.composite.CompositeState;

/**
 * TODO
 */
final class CompositeInputStream extends ObjectInputStream
{
    private EntitySession session;
    private Qi4j is;
    private CompositeBuilderFactory factory;

    public CompositeInputStream( InputStream in, EntitySession session, CompositeBuilderFactory factory, Qi4j is )
        throws IOException
    {
        super( in );
        this.factory = factory;
        this.session = session;
        this.is = is;
        enableResolveObject( true );
    }

    protected Object resolveObject( Object obj ) throws IOException
    {
        if( obj instanceof SerializedEntity )
        {
            SerializedEntity holder = (SerializedEntity) obj;
            Class<EntityComposite> clazz = holder.getPersistentCompositeClass();
            String id = holder.getIdentity();
            Object instance = session.find( id, clazz );
            return instance;
        }

        if( obj instanceof SerializedComposite )
        {
            // TODO Fix this!!
            SerializedComposite holder = (SerializedComposite) obj;
            Class<Composite> compositeInterface = holder.getCompositeInterface();
            Object[] mixins = holder.getMixins();

            CompositeBuilder<Composite> builder = null;
            do
            {
                try
                {
                    builder = factory.newCompositeBuilder( compositeInterface );
                }
                catch( InvalidApplicationException e )
                {
                    // Could not find this Composite - try superinterfaces
                    compositeInterface = is.getSuperComposite( compositeInterface );
                    if( compositeInterface == null )
                    {
                        throw e;
                    }
                }
            }
            while( builder == null );

            // CompositeBuilder found
            Composite composite = builder.newInstance();
            CompositeState mixinHandler = (CompositeState) Proxy.getInvocationHandler( composite );
            mixinHandler.setMixins( mixins );
            return composite;
        }
        return obj;
    }
}
