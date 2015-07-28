/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.qi4j.api.query.grammar;

import java.util.Collection;
import org.qi4j.api.composite.Composite;
import org.qi4j.functional.Iterables;

/**
 * Contains All Specification.
 */
public class ContainsAllSpecification<T>
    extends ExpressionSpecification
{
    private PropertyFunction<? extends Collection<T>> collectionProperty;
    private Iterable<T> valueCollection;

    public ContainsAllSpecification( PropertyFunction<? extends Collection<T>> collectionProperty,
                                     Iterable<T> valueCollection
    )
    {
        this.collectionProperty = collectionProperty;
        this.valueCollection = valueCollection;
    }

    public PropertyFunction<? extends Collection<T>> collectionProperty()
    {
        return collectionProperty;
    }

    public Iterable<T> containedValues()
    {
        return valueCollection;
    }

    @Override
    public boolean satisfiedBy( Composite item )
    {
        Collection<T> collection = collectionProperty.map( item ).get();

        if( collection == null )
        {
            return false;
        }

        for( T value : valueCollection )
        {
            if( !collection.contains( value ) )
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString()
    {
        return collectionProperty + " contains " + Iterables.toList( valueCollection );
    }
}
