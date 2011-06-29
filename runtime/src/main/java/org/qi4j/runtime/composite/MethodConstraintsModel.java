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

import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.util.HierarchicalVisitor;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.util.VisitableHierarchy;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.spi.constraint.MethodConstraintsDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.qi4j.api.util.Annotations.isType;

/**
 * JAVADOC
 */
public final class MethodConstraintsModel
    implements MethodConstraintsDescriptor, VisitableHierarchy<Object, Object>
{
    private List<ValueConstraintsModel> parameterConstraintModels;
    private Method method;
    private static MethodConstraintsInstance EMPTY_CONSTRAINTS = new MethodConstraintsInstance();

    public MethodConstraintsModel( Method method, ConstraintsModel constraintsModel )
    {
        this.method = method;
        parameterConstraintModels = null;
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Type[] parameterTypes = method.getGenericParameterTypes();
        boolean constrained = false;
        for( int i = 0; i < parameterAnnotations.length; i++ )
        {
            Annotation[] parameterAnnotation = parameterAnnotations[ i ];

            Name nameAnnotation = (Name) Iterables.first( Iterables.filter( isType( Name.class ), Iterables.iterable( parameterAnnotation ) ) );
            String name = nameAnnotation == null ? "param" + ( i + 1 ) : nameAnnotation.value();

            boolean optional = Iterables.first( Iterables.filter( isType( Optional.class ), Iterables.iterable( parameterAnnotation ) ) ) != null;
            ValueConstraintsModel parameterConstraintsModel = constraintsModel.constraintsFor( Arrays.asList( parameterAnnotation ), parameterTypes[ i ], name, optional );
            if( parameterConstraintsModel.isConstrained() )
            {
                constrained = true;
            }

            if( parameterConstraintModels == null )
            {
                parameterConstraintModels = new ArrayList<ValueConstraintsModel>();
            }
            parameterConstraintModels.add( parameterConstraintsModel );
        }

        if( !constrained )
        {
            parameterConstraintModels = null; // No constraints for this method
        }
    }

    public Iterable<DependencyModel> dependencies()
    {
        return null;
    }

    public boolean isConstrained()
    {
        return !parameterConstraintModels.isEmpty();
    }

    public MethodConstraintsInstance newInstance()
    {
        return parameterConstraintModels == null ? EMPTY_CONSTRAINTS : new MethodConstraintsInstance( method, parameterConstraintModels );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> modelVisitor ) throws ThrowableType
    {
        if (modelVisitor.visitEnter( this ))
        {
            if( parameterConstraintModels != null )
            {
                for( ValueConstraintsModel parameterConstraintModel : parameterConstraintModels )
                {
                    if (!parameterConstraintModel.accept( modelVisitor ))
                        break;
                }
            }
        }
        return modelVisitor.visitLeave( this );
    }
}
