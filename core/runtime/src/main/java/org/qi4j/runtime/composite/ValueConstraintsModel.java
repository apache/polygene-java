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

import java.util.List;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.VisitableHierarchy;

/**
 * JAVADOC
 */
public final class ValueConstraintsModel
    implements VisitableHierarchy<Object, Object>
{
    private final List<AbstractConstraintModel> constraintModels;
    private String name;
    private boolean optional;

    public ValueConstraintsModel( List<AbstractConstraintModel> constraintModels, String name, boolean optional )
    {
        this.constraintModels = constraintModels;
        this.name = name;
        this.optional = optional;
    }

    public ValueConstraintsInstance newInstance()
    {
        return new ValueConstraintsInstance( constraintModels, name, optional );
    }

    public boolean isConstrained()
    {
        if( !constraintModels.isEmpty() )
        {
            return true;
        }

        return !optional;
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> modelVisitor )
        throws ThrowableType
    {
        for( AbstractConstraintModel constraintModel : constraintModels )
        {
            if( constraintModel.accept( modelVisitor ) )
            {
                return false;
            }
        }
        return true;
    }
}
