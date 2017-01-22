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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.polygene.api.constraint.ConstraintsDescriptor;
import org.apache.polygene.api.util.HierarchicalVisitor;
import org.apache.polygene.api.util.VisitableHierarchy;

/**
 * JAVADOC
 */
public final class ConstraintsModel
    implements ConstraintsDescriptor, VisitableHierarchy<Object, Object>
{
    private List<ValueConstraintsModel> parameterConstraintModels;

    private static ConstraintsInstance EMPTY_CONSTRAINTS = new ConstraintsInstance( Collections.emptyList() );

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
