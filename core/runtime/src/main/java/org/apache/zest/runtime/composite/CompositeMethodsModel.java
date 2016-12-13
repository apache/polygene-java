/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.zest.runtime.composite;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.stream.Stream;
import org.apache.zest.api.composite.MissingMethodException;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.util.HierarchicalVisitor;
import org.apache.zest.api.util.VisitableHierarchy;
import org.apache.zest.runtime.injection.Dependencies;
import org.apache.zest.runtime.injection.DependencyModel;

/**
 * Model for Composite methods. This includes both private and public methods.
 */
public final class CompositeMethodsModel
    implements VisitableHierarchy<Object, Object>, Dependencies
{
    private final LinkedHashMap<Method, CompositeMethodModel> methods;
    private final MixinsModel mixinsModel;

    public CompositeMethodsModel( MixinsModel mixinsModel )
    {
        methods = new LinkedHashMap<>();
        this.mixinsModel = mixinsModel;
    }

    public Stream<DependencyModel> dependencies()
    {
        Collection<CompositeMethodModel> compositeMethods = methods.values();
        return compositeMethods.stream().flatMap( Dependencies.DEPENDENCIES_FUNCTION );
    }

    // Context
    public Object invoke( MixinsInstance mixins,
                          Object proxy,
                          Method method,
                          Object[] args,
                          ModuleDescriptor moduleInstance
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

            // TODO: Figure out what was the intention of this code block, added by Rickard in 2009. It doesn't do anything useful.
            if( !method.getDeclaringClass().isInterface() )
            {
                compositeMethod = mixinsModel.mixinTypes().map( aClass -> {
                    try
                    {
                        Method realMethod = aClass.getMethod( method.getName(), method.getParameterTypes() );
                        return methods.get( realMethod );
                    }
                    catch( NoSuchMethodException | SecurityException e )
                    {

                    }
                    return null;
                }).filter( model -> model != null ).findFirst().orElse( null );
            }
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
