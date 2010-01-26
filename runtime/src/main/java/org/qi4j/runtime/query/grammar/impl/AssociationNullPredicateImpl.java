/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.qi4j.runtime.query.grammar.impl;

import org.qi4j.api.query.grammar.AssociationNullPredicate;
import org.qi4j.api.query.grammar.AssociationReference;

/**
 * Generic {@link org.qi4j.api.query.grammar.AssociationNullPredicate} implementation.
 */
abstract class AssociationNullPredicateImpl
    implements AssociationNullPredicate
{

    /**
     * Association reference.
     */
    private final AssociationReference associationReference;

    /**
     * Constructor.
     *
     * @param associationReference association reference; cannot be null
     *
     * @throws IllegalArgumentException - If association reference is null
     */
    AssociationNullPredicateImpl( final AssociationReference associationReference )
    {
        if( associationReference == null )
        {
            throw new IllegalArgumentException( "Association reference cannot be null" );
        }
        this.associationReference = associationReference;
    }

    /**
     * @see org.qi4j.api.query.grammar.AssociationNullPredicate#associationReference()
     */
    public AssociationReference associationReference()
    {
        return associationReference;
    }
}