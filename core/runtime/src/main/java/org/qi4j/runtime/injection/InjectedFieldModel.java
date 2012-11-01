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

package org.qi4j.runtime.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collection;
import org.qi4j.api.composite.InjectedFieldDescriptor;
import org.qi4j.api.util.Classes;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.bootstrap.InjectionException;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.Specification;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.composite.TransientInstance;
import org.qi4j.runtime.model.Resolution;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

/**
 * JAVADOC
 */
public final class InjectedFieldModel
    implements InjectedFieldDescriptor, VisitableHierarchy<InjectedFieldModel, DependencyModel>
{
    private DependencyModel dependencyModel;
    private Field injectedField;

    public InjectedFieldModel( Field injectedField, DependencyModel dependencyModel )
    {
        injectedField.setAccessible( true );
        this.injectedField = injectedField;
        this.dependencyModel = dependencyModel;
    }

    @Override
    public DependencyModel dependency()
    {
        return dependencyModel;
    }

    @Override
    public Field field()
    {
        return injectedField;
    }

    public void bind( Resolution resolution )
        throws BindingException
    {
        dependencyModel.bind( resolution.forField( injectedField ) );
    }

    public void inject( InjectionContext context, Object instance )
    {
        Object value = dependencyModel.inject( context );
        try
        {
            injectedField.set( instance, value );
        }
        catch( IllegalAccessException e )
        {
            throw new InjectionException( e );
        }
        catch( IllegalArgumentException e )
        {
            String valueClassName;
            if( Proxy.isProxyClass( value.getClass() ) )
            {
                InvocationHandler invocationHandler = Proxy.getInvocationHandler( value );
                if( invocationHandler instanceof TransientInstance )
                {
                    TransientInstance handler = (TransientInstance) invocationHandler;
                    valueClassName = Classes.toString( handler.descriptor().types() )
                                     + " in [" + handler.module().name() + "] of [" + handler.module()
                        .layerInstance()
                        .name() + "]";
                }
                else
                {
                    valueClassName = invocationHandler.toString();
                }
            }
            else
            {
                valueClassName = value.getClass().getName();
            }
            StringBuilder annotBuilder = new StringBuilder();
            for( Annotation annot : injectedField.getAnnotations() )
            {
                String s = annot.toString();
                annotBuilder.append( "@" ).append( s.substring( s.lastIndexOf( '.' ) + 1, s.length() - 2 ) );
                annotBuilder.append( " " );
            }
            String annots = annotBuilder.toString();
            String message = "Can not inject the field\n    "
                             + injectedField.getDeclaringClass()
                             + "\n    {\n        " + annots + "\n        "
                             + injectedField.getType().getSimpleName() + " " + injectedField.getName()
                             + "\n    }\nwith value \n    " + value + "\nof type\n    "
                             + valueClassName;
            throw new InjectionException( message, e );
        }
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super InjectedFieldModel, ? super DependencyModel, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            visitor.visit( dependencyModel );
        }
        return visitor.visitLeave( this );
    }

    public Collection<DependencyModel> filter( Specification<DependencyModel> specification )
    {
        if( specification.satisfiedBy( dependencyModel ) )
        {
            return singleton( dependencyModel );
        }
        else
        {
            return emptyList();
        }
    }
}
