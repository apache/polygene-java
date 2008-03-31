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
package org.qi4j.query.grammar;

/**
 * An expression related to {@link org.qi4j.association.Association}.
 */
public interface AssociationReference
{

    /**
     * Get the name of the association, which is equal to the name of the method that declared it.
     *
     * @return the name of the association
     */
    String getAssociationReferenceName();

    /**
     * Get the type of the interface that declared the association.
     *
     * @return the type of property that declared the association
     */
    Class getAssociationReferenceDeclaringType();

    /**
     * Get the type of the assocition. If the association is declared as Association<X> then X is returned.
     *
     * @return the association type
     */
    Class getAssociationReferenceType();

    /**
     * Gets the traversed association used to get to this association or null if there was no traversal involved.
     *
     * @return traversed association used to get to this association.
     */
    AssociationReference getTraversedAssociation();

}