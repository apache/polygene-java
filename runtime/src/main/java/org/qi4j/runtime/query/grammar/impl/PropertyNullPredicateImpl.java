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

import org.qi4j.api.query.grammar.PropertyNullPredicate;
import org.qi4j.api.query.grammar.PropertyReference;

/**
 * Generic {@link org.qi4j.api.query.grammar.PropertyNullPredicate} implementation.
 */
abstract class PropertyNullPredicateImpl<T>
    implements PropertyNullPredicate<T>
{

    /**
     * Property reference.
     */
    private final PropertyReference<T> propertyReference;

    /**
     * Constructor.
     *
     * @param propertyReference property reference; cannot be null
     *
     * @throws IllegalArgumentException - If property reference is null
     */
    PropertyNullPredicateImpl( final PropertyReference<T> propertyReference )
    {
        if( propertyReference == null )
        {
            throw new IllegalArgumentException( "Property reference cannot be null" );
        }
        this.propertyReference = propertyReference;
    }

    /**
     * @see org.qi4j.api.query.grammar.PropertyNullPredicate#propertyReference()
     */
    public PropertyReference<T> propertyReference()
    {
        return propertyReference;
    }
}