/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.api.constraint;

import java.lang.reflect.Type;
import org.qi4j.api.common.InvalidApplicationException;

public class ConstraintImplementationNotFoundException
    extends InvalidApplicationException
{
    private static final long serialVersionUID = 1L;

    private final Class compositeType;
    private final Class constraintType;
    private final Type valueType;

    public ConstraintImplementationNotFoundException( Class compositeType, Class constraintType, Type valueType )
    {
        super( "Cannot find implementation of constraint @" + constraintType.getSimpleName() + " for " + valueType + " in composite " + compositeType
            .getName() );
        this.compositeType = compositeType;
        this.constraintType = constraintType;
        this.valueType = valueType;
    }

    public Class compositeType()
    {
        return compositeType;
    }

    public Class constraintType()
    {
        return constraintType;
    }

    public Type valueType()
    {
        return valueType;
    }
}