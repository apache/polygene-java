/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

import java.io.Serializable;
import java.lang.annotation.Annotation;

/**
 * When a constraint violation has occurred (ie Constraint.isValid has returned false) it
 * is put in a collection of all violations that have occurred for this value check.
 */
public final class ConstraintViolation
    implements Serializable
{
    private String name;
    private final Annotation constraint;
    private final Object value;

    public ConstraintViolation( String name, Annotation constraint, Object value )
    {
        this.name = name;
        this.constraint = constraint;
        this.value = value;
    }

    public String name()
    {
        return name;
    }

    public Annotation constraint()
    {
        return constraint;
    }

    public Object value()
    {
        return value;
    }
}
