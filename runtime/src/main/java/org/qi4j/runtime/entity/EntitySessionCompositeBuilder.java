/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.entity;

import java.util.Iterator;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.CompositeInstantiationException;
import org.qi4j.composite.PropertyValue;
import org.qi4j.entity.EntityComposite;

/**
 * TODO
 */
public final class EntitySessionCompositeBuilder<T extends EntityComposite>
    implements CompositeBuilder<T>
{
    private CompositeBuilder<T> compositeBuilder;
    private EntitySessionImpl entitySession;

    public EntitySessionCompositeBuilder( CompositeBuilder compositeBuilder, EntitySessionImpl entitySession )
    {
        this.compositeBuilder = compositeBuilder;
        this.entitySession = entitySession;
    }

    public void adapt( Object mixin )
    {
        throw new CompositeInstantiationException( "Entities may not adapt other objects" );
    }

    public <K, T extends K> void decorate( K object )
    {
        throw new CompositeInstantiationException( "Entities may not decorate other objects" );
    }

    public <K> void properties( Class<K> mixinType, PropertyValue... properties )
    {
        compositeBuilder.properties( mixinType, properties );
    }

    public T propertiesOfComposite()
    {
        return compositeBuilder.propertiesOfComposite();
    }

    public <K> K propertiesFor( Class<K> mixinType )
    {
        return compositeBuilder.propertiesFor( mixinType );
    }

    public T newInstance()
    {
        T instance = compositeBuilder.newInstance();
        entitySession.createEntity( instance );
        return instance;
    }

    public Iterator<T> iterator()
    {
        final Iterator<T> decoratedIterator = compositeBuilder.iterator();

        return new Iterator<T>()
        {
            public boolean hasNext()
            {
                return true;
            }

            public T next()
            {
                T instance = decoratedIterator.next();
                entitySession.createEntity( instance );
                return instance;
            }

            public void remove()
            {
            }
        };
    }
}
