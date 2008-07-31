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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.spi.composite.MethodConstraintsDescriptor;

/**
 * TODO
 */
public final class MethodConstraintsModel
    implements MethodConstraintsDescriptor
{
    private List<ValueConstraintsModel> parameterConstraintModels;
    private final Method method;

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
            ValueConstraintsModel parameterConstraintsModel = constraintsModel.constraintsFor( parameterAnnotation, parameterTypes[ i ] );
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

    public Method method()
    {
        return method;
    }

    public boolean isConstrained()
    {
        return !parameterConstraintModels.isEmpty();
    }

    public MethodConstraintsInstance newInstance()
    {
        return parameterConstraintModels == null ? new MethodConstraintsInstance() : new MethodConstraintsInstance( method, parameterConstraintModels );
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );
        if( parameterConstraintModels == null )
        {
            return;
        }

        for( ValueConstraintsModel parameterConstraintModel : parameterConstraintModels )
        {
            parameterConstraintModel.visitModel( modelVisitor );
        }
    }
}
