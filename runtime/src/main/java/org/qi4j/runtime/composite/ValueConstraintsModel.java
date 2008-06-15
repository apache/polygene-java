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

import java.lang.reflect.Method;
import java.util.List;
import org.qi4j.runtime.structure.ModelVisitor;

/**
 * TODO
 */
public final class ValueConstraintsModel
{
    private final List<ConstraintModel> constraintModels;

    public ValueConstraintsModel( List<ConstraintModel> constraintModels )
    {
        this.constraintModels = constraintModels;
    }

    public ValueConstraintsInstance newInstance()
    {
        return new ValueConstraintsInstance( constraintModels );
    }

    public boolean isConstrained()
    {
        return !constraintModels.isEmpty();
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        for( ConstraintModel constraintModel : constraintModels )
        {
            constraintModel.visitModel( modelVisitor );
        }
    }
}
