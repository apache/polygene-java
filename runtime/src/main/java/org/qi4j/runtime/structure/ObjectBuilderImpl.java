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

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.qi4j.composite.ObjectBuilder;
import org.qi4j.composite.PropertyValue;
import org.qi4j.runtime.composite.ObjectContext;

/**
 *
 */
public final class ObjectBuilderImpl<T>
    implements ObjectBuilder<T>
{
    private ObjectContext objectContext;
    private ModuleInstance moduleInstance;

    private Set<Object> adaptContext;
    private Object decoratedObject;
    private Map<String, Object> propertyContext;

    ObjectBuilderImpl( ModuleInstance moduleInstance, ObjectContext objectBinding )
    {
        this.objectContext = objectBinding;
        this.moduleInstance = moduleInstance;

        adaptContext = emptySet();
        propertyContext = emptyMap();
    }

    public void adapt( Object anAdaptedObject )
    {
        Set<Object> context = getAdaptContext();
        context.add( anAdaptedObject );
    }

    public void decorate( Object aDecoratedObject )
    {
        decoratedObject = aDecoratedObject;
    }

    public void properties( PropertyValue... properties )
    {
        Map<String, Object> props = getPropertyContext();
        for( PropertyValue property : properties )
        {
            props.put( property.getName(), property );
        }
    }

    public T newInstance()
    {
        return (T) objectContext.newObjectInstance( moduleInstance, adaptContext, decoratedObject, propertyContext );
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
        objectContext.inject( instance, moduleInstance, adaptContext, decoratedObject, propertyContext );
    }

    // Private ------------------------------------------------------
    private Set<Object> getAdaptContext()
    {
        if( adaptContext == emptySet() )
        {
            adaptContext = new LinkedHashSet<Object>();
        }

        return adaptContext;
    }

    private Map<String, Object> getPropertyContext()
    {
        if( !( propertyContext instanceof LinkedHashMap ) )
        {
            propertyContext = new LinkedHashMap<String, Object>();
        }

        return propertyContext;
    }
}