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

package org.qi4j.runtime.composite.qi;

import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.runtime.structure.qi.ModuleInstance;

/**
 * TODO
 */
public class CompositeBuilderInstance<T>
    implements CompositeBuilder<T>
{
    private ModuleInstance moduleInstance;
    private CompositeModel compositeModel;
    private Set<Object> uses;
    private Class<T> compositeType;

    public CompositeBuilderInstance( ModuleInstance moduleInstance, CompositeModel compositeModel )
    {
        this.moduleInstance = moduleInstance;

        this.compositeModel = compositeModel;
        compositeType = (Class<T>) compositeModel.type();
    }

    public CompositeBuilder<T> use( Object... usedObjects )
    {
        getUses().addAll( asList( usedObjects ) );

        return this;
    }

    public T stateOfComposite()
    {
        return null;
    }

    public <K> K stateFor( Class<K> mixinType )
    {
        return null;
    }

    public T newInstance() throws org.qi4j.composite.InstantiationException
    {
        CompositeInstance compositeInstance = compositeModel.newCompositeInstance( moduleInstance, Collections.emptySet(), null );
        return compositeType.cast( compositeInstance.proxy() );
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

    protected Set<Object> getUses()
    {
        if( uses == null )
        {
            uses = new LinkedHashSet<Object>();
        }
        return uses;
    }

}
