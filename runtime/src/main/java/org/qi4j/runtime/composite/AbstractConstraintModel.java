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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.spi.constraint.ConstraintDescriptor;

/**
 * JAVADOC
 */
public abstract class AbstractConstraintModel
    implements ConstraintDescriptor, Serializable
{
    protected final Annotation annotation;

    public AbstractConstraintModel( Annotation annotation )
    {
        this.annotation = annotation;
    }

    public Annotation annotation()
    {
        return annotation;
    }

    public abstract ConstraintInstance<?, ?> newInstance();

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );
    }
}