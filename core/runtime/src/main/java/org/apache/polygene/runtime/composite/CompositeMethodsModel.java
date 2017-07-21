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
package org.apache.polygene.runtime.composite;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.composite.MissingMethodException;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.util.HierarchicalVisitor;
import org.apache.polygene.api.util.VisitableHierarchy;
import org.apache.polygene.runtime.injection.Dependencies;
import org.apache.polygene.runtime.injection.DependencyModel;

/**
 * Model for Composite methods. This includes both private and public methods.
 */
public final class CompositeMethodsModel
    implements VisitableHierarchy<Object, Object>, Dependencies
{
    private static final String NL = System.getProperty( "line.separator" );

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
            Class<?> declaringClass = method.getDeclaringClass();
            if( declaringClass.equals( Object.class ) )
            {
                return mixins.invokeObject( proxy, args, method );
            }

            // TODO: Figure out what was the intention of this code block, added by Rickard in 2009. It doesn't do anything useful.
            // Update (niclas): My guess is that this is preparation for mixins in Objects.
            if( !declaringClass.isInterface() )
            {
                compositeMethod = mixinsModel.mixinTypes().map( aClass ->
                                                                {
                                                                    try
                                                                    {
                                                                        Method realMethod = aClass.getMethod( method.getName(), method.getParameterTypes() );
                                                                        return methods.get( realMethod );
                                                                    }
                                                                    catch( NoSuchMethodException | SecurityException e )
                                                                    {

                                                                    }
                                                                    return null;
                                                                } ).filter( Objects::nonNull ).findFirst().orElse( null );
                return compositeMethod.invoke( proxy, args, mixins, moduleInstance );
            }
            if( method.isDefault() )
            {
                if( proxy instanceof Composite )
                {
                    throw new InternalError( "This shouldn't happen!" );
                }
                // Does this next line actually make any sense? Can we have a default method on an interface where the instance is not a Composite? Maybe... Let's try to trap a usecase by disallowing it.
//                return method.invoke( proxy, args );
                String message = "We have detected a default method on an interface that is not backed by a Composite. "
                                 + "Please report this to dev@polygene.apache.org together with the information below, "
                                 + "that/those class(es) and the relevant assembly information. Thank you"
                                 + NL + "Method:"
                                 + method.toGenericString()
                                 + NL + "Declaring Class:"
                                 + method.getDeclaringClass().toGenericString()
                                 + NL + "Types:"
                                 + mixinsModel.mixinTypes()
                                              .map( Class::toGenericString )
                                              .collect( Collectors.joining( NL ) );
                throw new UnsupportedOperationException( message );
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
