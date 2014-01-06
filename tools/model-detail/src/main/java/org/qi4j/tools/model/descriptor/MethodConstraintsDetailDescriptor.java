/*
 * Copyright (c) 2008, Edward Yakop. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.tools.model.descriptor;

import java.util.LinkedList;
import java.util.List;
import org.qi4j.api.constraint.ConstraintsDescriptor;

import static org.qi4j.api.util.NullArgumentException.validateNotNull;

public final class MethodConstraintsDetailDescriptor
{
    private final ConstraintsDescriptor descriptor;
    private CompositeMethodDetailDescriptor method;
    private final List<MethodConstraintDetailDescriptor> constraints;

    MethodConstraintsDetailDescriptor( ConstraintsDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        descriptor = aDescriptor;
        constraints = new LinkedList<MethodConstraintDetailDescriptor>();
    }

    /**
     * @return Descriptor of this {@code CompositeMethodConstrainsDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final ConstraintsDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Constraints of this {@code CompositeMethodConstrainsDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final Iterable<MethodConstraintDetailDescriptor> constraints()
    {
        return constraints;
    }

    /**
     * @return Method that owns this {@code CompositeMethodConstrainsDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final CompositeMethodDetailDescriptor method()
    {
        return method;
    }

    final void setMethod( CompositeMethodDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        method = aDescriptor;
    }

    final void addConstraint( MethodConstraintDetailDescriptor aDescriptor )
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setConstraints( this );
        constraints.add( aDescriptor );
    }
}