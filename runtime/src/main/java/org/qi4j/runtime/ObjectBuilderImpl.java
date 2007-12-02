/*  Copyright 2007 Rickard �berg.
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

import static java.util.Collections.emptySet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.qi4j.CompositeBuilderFactory;
import org.qi4j.ObjectBuilder;
import org.qi4j.ObjectBuilderFactory;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
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

    ObjectBuilderImpl( ObjectBinding anObjectBinding, ModuleContext aModuleContext, InstanceFactory anInstanceFactory )
        throws IllegalArgumentException
    {
        validateNotNull( "anObjectBinding", anObjectBinding );
        validateNotNull( "aModuleContext", aModuleContext );
        validateNotNull( "anInstanceFactory", anInstanceFactory );

        objectBinding = anObjectBinding;
        moduleContext = aModuleContext;
        instanceFactory = anInstanceFactory;

        adaptContext = emptySet();
    }

    @SuppressWarnings( "unchecked" )
    public void adapt( Object anAdaptedObject )
    {
        Set context = getAdaptContext();
        context.add( anAdaptedObject );
    }

    public void decorate( Object aDecoratedObject )
    {
        decoratedObject = aDecoratedObject;
    }

    @SuppressWarnings( "unchecked" )
    public T newInstance()
    {
        // Instantiate object
        CompositeBuilderFactory compBuilderFactory = moduleContext.getCompositeBuilderFactory();
        ObjectBuilderFactory objBuilderFactory = moduleContext.getObjectBuilderFactory();

        InjectionContext context = new ObjectInjectionContext(
            compBuilderFactory, objBuilderFactory, moduleContext.getModuleBinding(), adaptContext, decoratedObject );
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
        Set adapt = adaptContext == null ? emptySet() : adaptContext;

        InjectionContext context = new ObjectInjectionContext( null, null, moduleContext.getModuleBinding(), adapt, decoratedObject );
        instanceFactory.inject( instance, objectBinding, context );
    }

    // Private ------------------------------------------------------
    private Set getAdaptContext()
    {
        if( adaptContext == emptySet() )
        {
            adaptContext = new LinkedHashSet();
        }

        return adaptContext;
    }
}