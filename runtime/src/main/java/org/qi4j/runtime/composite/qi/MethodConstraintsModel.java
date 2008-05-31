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

package org.qi4j.runtime.composite.qi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public final class MethodConstraintsModel
{
    private List<ValueConstraintsModel> parameterConstraintModels;
    private Method method;

    public MethodConstraintsModel( Method method, ConstraintsModel constraintsModel )
    {
        this.method = method;
        parameterConstraintModels = null;
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Class<?>[] parameterTypes = method.getParameterTypes();
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

    public boolean isConstrained()
    {
        return !parameterConstraintModels.isEmpty();
    }

    public MethodConstraintsInstance newInstance()
    {
        return parameterConstraintModels == null ? new MethodConstraintsInstance() : new MethodConstraintsInstance( method, parameterConstraintModels );
    }
}
