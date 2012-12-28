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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.injection.InjectionScope;
import org.qi4j.api.util.Classes;
import org.qi4j.api.util.Fields;
import org.qi4j.functional.Function;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.VisitableHierarchy;

import static org.qi4j.api.util.Annotations.hasAnnotation;
import static org.qi4j.api.util.Annotations.type;
import static org.qi4j.functional.Iterables.filter;
import static org.qi4j.functional.Iterables.first;
import static org.qi4j.functional.Iterables.iterable;
import static org.qi4j.functional.Specifications.translate;

/**
 * JAVADOC
 */
public final class InjectedFieldsModel
    implements Dependencies, VisitableHierarchy<Object, Object>
{
    private final List<InjectedFieldModel> fields = new ArrayList<InjectedFieldModel>();

    public InjectedFieldsModel( Class fragmentClass )
    {
        Iterable<Field> mappedFields = Fields.FIELDS_OF.map( fragmentClass );
        for( Field field : mappedFields )
        {
            Annotation injectionAnnotation = first( filter( translate( type(), hasAnnotation( InjectionScope.class ) ), iterable( field
                                                                                                                                      .getAnnotations() ) ) );
            if( injectionAnnotation != null )
            {
                addModel( fragmentClass, field, injectionAnnotation );
            }
        }
    }

    private void addModel( Class fragmentClass, Field field, Annotation injectionAnnotation )
    {
        Type genericType = field.getGenericType();
        if( genericType instanceof ParameterizedType )
        {
            genericType = new ParameterizedTypeInstance( ( (ParameterizedType) genericType ).getActualTypeArguments(), ( (ParameterizedType) genericType )
                .getRawType(), ( (ParameterizedType) genericType ).getOwnerType() );

            for( int i = 0; i < ( (ParameterizedType) genericType ).getActualTypeArguments().length; i++ )
            {
                Type type = ( (ParameterizedType) genericType ).getActualTypeArguments()[ i ];
                if( type instanceof TypeVariable )
                {
                    type = Classes.resolveTypeVariable( (TypeVariable) type, field.getDeclaringClass(), fragmentClass );
                    ( (ParameterizedType) genericType ).getActualTypeArguments()[ i ] = type;
                }
            }
        }

        boolean optional = DependencyModel.isOptional( injectionAnnotation, field.getAnnotations() );
        DependencyModel dependencyModel = new DependencyModel( injectionAnnotation, genericType, fragmentClass, optional, field
            .getAnnotations() );
        InjectedFieldModel injectedFieldModel = new InjectedFieldModel( field, dependencyModel );
        this.fields.add( injectedFieldModel );
    }

    @Override
    public Iterable<DependencyModel> dependencies()
    {
        return Iterables.map( new Function<InjectedFieldModel, DependencyModel>()
        {
            @Override
            public DependencyModel map( InjectedFieldModel injectedFieldModel )
            {
                return injectedFieldModel.dependency();
            }
        }, fields );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> modelVisitor )
        throws ThrowableType
    {
        if( modelVisitor.visitEnter( this ) )
        {
            for( InjectedFieldModel field : fields )
            {
                if( !field.accept( modelVisitor ) )
                {
                    break;
                }
            }
        }
        return modelVisitor.visitLeave( this );
    }

    public void inject( InjectionContext context, Object instance )
    {
        for( InjectedFieldModel field : fields )
        {
            field.inject( context, instance );
        }
    }
}