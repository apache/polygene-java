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

package org.qi4j.runtime.composite;

import java.lang.reflect.Method;
import java.util.HashMap;
import org.qi4j.api.composite.MissingMethodException;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.injection.Dependencies;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.structure.ModuleInstance;

import static org.qi4j.functional.Iterables.map;

/**
 * Model for Composite methods. This includes both private and public methods.
 */
public final class CompositeMethodsModel
    implements VisitableHierarchy<Object, Object>
{
    private HashMap<Method, CompositeMethodModel> methods;
    private final MixinsModel mixinsModel;

    public CompositeMethodsModel( MixinsModel mixinsModel )
    {
        methods = new HashMap<Method, CompositeMethodModel>();
        this.mixinsModel = mixinsModel;
    }

    public Iterable<DependencyModel> dependencies()
    {
        return Iterables.flattenIterables( map( Dependencies.DEPENDENCIES_FUNCTION, methods.values() ) );
    }

    // Context
    public Object invoke( MixinsInstance mixins,
                          Object proxy,
                          Method method,
                          Object[] args,
                          ModuleInstance moduleInstance
    )
        throws Throwable
    {
        CompositeMethodModel compositeMethod = methods.get( method );

        if( compositeMethod == null )
        {
            if( method.getDeclaringClass().equals( Object.class ) )
            {
                return mixins.invokeObject( proxy, args, method );
            }

            if( !method.getDeclaringClass().isInterface() )
            {
                Iterable<Class<?>> types = mixinsModel.mixinTypes();
                for( Class<?> aClass : types )
                {
                    try
                    {
                        Method realMethod = aClass.getMethod( method.getName(), method.getParameterTypes() );
                        compositeMethod = methods.get( realMethod );
                        break;
                    }
                    catch( NoSuchMethodException e )
                    {
                    }
                    catch( SecurityException e )
                    {
                    }
                }
            }
//            return mixins.invokeObject( proxy, args, method );
            throw new MissingMethodException( "Method '" + method + "' is not implemented" );
        }
        else
        {
            return compositeMethod.invoke( proxy, args, mixins, moduleInstance );
        }
    }

    public void addMethod( CompositeMethodModel methodModel )
    {
        methods.put( methodModel.method(), methodModel );
    }

    public boolean isImplemented( Method method )
    {
        return methods.containsKey( method );
    }

    public Iterable<Method> methods()
    {
        return methods.keySet();
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> modelVisitor )
        throws ThrowableType
    {
        if( modelVisitor.visitEnter( this ) )
        {
            for( CompositeMethodModel compositeMethodModel : methods.values() )
            {
                if( !compositeMethodModel.accept( modelVisitor ) )
                {
                    break;
                }
            }
        }
        return modelVisitor.visitLeave( this );
    }

    @Override
    public String toString()
    {
        return methods().toString();
    }
}
