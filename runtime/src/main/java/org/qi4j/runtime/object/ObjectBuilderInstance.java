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

package org.qi4j.runtime.object;

import java.util.Iterator;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.runtime.injection.InjectionContext;

/**
 * JAVADOC
 */
public final class ObjectBuilderInstance<T>
    implements ObjectBuilder<T>
{
    protected final ObjectModel objectModel;
    private final Class<T> objectType;
    private InjectionContext injectionContext;

    public ObjectBuilderInstance( InjectionContext injectionContext, ObjectModel objectModel )
    {
        this.injectionContext = injectionContext;
        this.objectModel = objectModel;
        objectType = (Class<T>) objectModel.type();
    }

    public Class<T> objectType()
    {
        return objectType;
    }

    public ObjectBuilder<T> use( Object... usedObjects )
    {
        injectionContext.setUses( injectionContext.uses().use( usedObjects ) );
        return this;
    }

    public T newInstance()
        throws ConstructionException
    {
        Object instance = objectModel.newInstance( injectionContext );
        return objectType.cast( instance );
    }

    public void injectTo( T instance )
        throws ConstructionException
    {
        objectModel.inject( injectionContext, instance );
    }

    public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {
            public boolean hasNext()
            {
                return true;
            }

            public T next()
            {
                return newInstance();
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }
}