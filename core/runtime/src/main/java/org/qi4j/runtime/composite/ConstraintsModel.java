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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.qi4j.api.constraint.ConstraintsDescriptor;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.VisitableHierarchy;

/**
 * JAVADOC
 */
public final class ConstraintsModel
    implements ConstraintsDescriptor, VisitableHierarchy<Object, Object>
{
    private List<ValueConstraintsModel> parameterConstraintModels;

    private static ConstraintsInstance EMPTY_CONSTRAINTS = new ConstraintsInstance( Collections.<ValueConstraintsInstance>emptyList() );

    public ConstraintsModel( List<ValueConstraintsModel> parameterConstraintModels )
    {
        this.parameterConstraintModels = parameterConstraintModels;
    }

    public ConstraintsInstance newInstance()
    {
        if( parameterConstraintModels.isEmpty() )
        {
            return EMPTY_CONSTRAINTS;
        }
        else
        {
            List<ValueConstraintsInstance> parameterConstraintsInstances = new ArrayList<ValueConstraintsInstance>( parameterConstraintModels
                                                                                                                        .size() );
            for( ValueConstraintsModel parameterConstraintModel : parameterConstraintModels )
            {
                parameterConstraintsInstances.add( parameterConstraintModel.newInstance() );
            }
            return new ConstraintsInstance( parameterConstraintsInstances );
        }
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> modelVisitor )
        throws ThrowableType
    {
        if( modelVisitor.visitEnter( this ) )
        {
            if( parameterConstraintModels != null )
            {
                for( ValueConstraintsModel parameterConstraintModel : parameterConstraintModels )
                {
                    if( !parameterConstraintModel.accept( modelVisitor ) )
                    {
                        break;
                    }
                }
            }
        }
        return modelVisitor.visitLeave( this );
    }
}
