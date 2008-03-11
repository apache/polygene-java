/*  Copyright 2007 Rickard Öberg.
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
package org.qi4j.runtime.structure;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.qi4j.composite.ObjectBuilder;
import org.qi4j.runtime.composite.ObjectContext;

/**
 *
 */
public final class ObjectBuilderImpl<T>
    implements ObjectBuilder<T>
{
    private ObjectContext objectContext;
    private ModuleInstance moduleInstance;

    private Set<Object> uses;

    ObjectBuilderImpl( ModuleInstance moduleInstance, ObjectContext objectBinding )
    {
        this.objectContext = objectBinding;
        this.moduleInstance = moduleInstance;

    }

    public void uses( Object usedObject )
    {
        Set<Object> context = getUses();
        context.add( usedObject );
    }

    public T newInstance()
    {
        return (T) objectContext.newObjectInstance( moduleInstance, uses );
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
            }
        };
    }

    public void inject( T instance )
    {
        // Inject existing object
        objectContext.inject( instance, moduleInstance, uses );
    }

    // Private ------------------------------------------------------
    private Set<Object> getUses()
    {
        if( uses == null )
        {
            uses = new LinkedHashSet<Object>();
        }

        return uses;
    }
}