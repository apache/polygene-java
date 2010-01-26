/*
 * Copyright 2008 Alin Dreghiciu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.api.query.grammar;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * An expression related to {@link org.qi4j.api.entity.association.Association}.
 */
public interface AssociationReference
{
    enum ReferenceType
    {
        NONE, ASSOCIATION, ROLE
    }

    /**
     * Get the name of the association, which is equal to the name of the method that declared it.
     *
     * @return the name of the association
     */
    String associationName();

    /**
     * Get the type of the interface that declared the association.
     *
     * @return the type of property that declared the association
     */
    Class<?> associationDeclaringType();

    /**
     * Get the accessor method for the association.
     *
     * @return accessor method
     */
    Method associationAccessor();

    /**
     * Get the type of the assocition. If the association is declared as Association<X> then X is returned.
     *
     * @return the association type
     */
    Type associationType();

    /**
     * Gets the traversed association used to get to this association or null if there was no traversal involved.
     *
     * @return traversed association used to get to this association.
     */
    AssociationReference traversedAssociation();

    /**
     * Evaluates the association reference against a target object.
     *
     * @param target target object
     *
     * @return associated instance from the target
     */
    Object eval( Object target );
}