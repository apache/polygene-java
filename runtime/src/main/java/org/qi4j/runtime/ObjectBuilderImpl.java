/*  Copyright 2007 Rickard …berg.
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
package org.qi4j.runtime;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.qi4j.ObjectBuilder;
import org.qi4j.runtime.structure.ModuleContext;
import org.qi4j.spi.composite.ObjectBinding;
import org.qi4j.spi.dependency.InjectionContext;
import org.qi4j.spi.dependency.ObjectInjectionContext;

/**
 *
 */
public class ObjectBuilderImpl<T>
    implements ObjectBuilder<T>
{
    private ObjectBinding objectBinding;
    private ModuleContext moduleContext;
    private InstanceFactory instanceFactory;

    private Set adaptContext;
    private Object decoratedObject;


    ObjectBuilderImpl( ObjectBinding objectBinding, ModuleContext moduleContext, InstanceFactory instanceFactory )
    {
        this.objectBinding = objectBinding;
        this.moduleContext = moduleContext;
        this.instanceFactory = instanceFactory;
    }

    public void adapt( Object adaptedObject )
    {
        getAdaptContext().add( adaptedObject );
    }

    public void decorate( Object decoratedObject )
    {
        this.decoratedObject = decoratedObject;
    }

    public T newInstance()
    {
        // Instantiate object
        Set adapt = adaptContext == null ? Collections.EMPTY_SET : adaptContext;

        // TODO Fix refs to app and module!!
        InjectionContext context = new ObjectInjectionContext( null, null, moduleContext.getModuleBinding(), adapt, decoratedObject );
        T instance = (T) instanceFactory.newInstance( objectBinding, context );
        return instance;
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
        Set adapt = adaptContext == null ? Collections.EMPTY_SET : adaptContext;

        InjectionContext context = new ObjectInjectionContext( null, null, moduleContext.getModuleBinding(), adapt, decoratedObject );
        instanceFactory.inject( instance, objectBinding, context );
    }

    // Private ------------------------------------------------------
    private Set getAdaptContext()
    {
        if( adaptContext == null )
        {
            adaptContext = new LinkedHashSet();
        }
        return adaptContext;
    }
}