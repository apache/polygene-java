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

package org.apache.zest.runtime.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.zest.api.injection.InjectionScope;
import org.apache.zest.api.util.Classes;
import org.apache.zest.api.util.Fields;
import org.apache.zest.api.util.HierarchicalVisitor;
import org.apache.zest.api.util.VisitableHierarchy;

import static org.apache.zest.api.util.Annotations.typeHasAnnotation;

/**
 * JAVADOC
 */
public final class InjectedFieldsModel
    implements Dependencies, VisitableHierarchy<Object, Object>
{
    private final List<InjectedFieldModel> fields = new ArrayList<>();

    public InjectedFieldsModel( Class fragmentClass )
    {
        Fields.fieldsOf( fragmentClass ).forEach( field ->
            Arrays.stream( field.getAnnotations() )
                  .filter( typeHasAnnotation( InjectionScope.class ) )
                  .filter( Objects::nonNull )
                  .forEach( injectionAnnotation ->  addModel( fragmentClass, field, injectionAnnotation )
            )
        );
    }

    private void addModel( Class fragmentClass, Field field, Annotation injectionAnnotation )
    {
        Type genericType = field.getGenericType();
        if( genericType instanceof ParameterizedType )
        {
            Type[] actualTypeArguments = ( (ParameterizedType) genericType ).getActualTypeArguments();
            Type rawType = ( (ParameterizedType) genericType ).getRawType();
            Type ownerType = ( (ParameterizedType) genericType ).getOwnerType();
            genericType = new ParameterizedTypeInstance( actualTypeArguments, rawType, ownerType );

            for( int i = 0; i < actualTypeArguments.length; i++ )
            {
                Type type = actualTypeArguments[ i ];
                if( type instanceof TypeVariable )
                {
                    type = Classes.resolveTypeVariable( (TypeVariable) type, field.getDeclaringClass(), fragmentClass );
                    actualTypeArguments[ i ] = type;
                }
            }
        }

        boolean optional = DependencyModel.isOptional( injectionAnnotation, field.getAnnotations() );
        DependencyModel dependencyModel = new DependencyModel( injectionAnnotation, genericType, fragmentClass, optional, field.getAnnotations() );
        InjectedFieldModel injectedFieldModel = new InjectedFieldModel( field, dependencyModel );
        this.fields.add( injectedFieldModel );
    }

    @Override
    public Stream<DependencyModel> dependencies()
    {
        return fields.stream().flatMap( Dependencies::dependencies );
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