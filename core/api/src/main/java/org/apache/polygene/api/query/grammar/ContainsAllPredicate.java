/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.api.query.grammar;

import java.util.Collection;
import java.util.Collections;
import org.apache.polygene.api.composite.Composite;

/**
 * Contains All Specification.
 */
public class ContainsAllPredicate<T>
    extends ExpressionPredicate
{
    private PropertyFunction<? extends Collection<T>> collectionProperty;
    private Collection<T> valueCollection;

    public ContainsAllPredicate( PropertyFunction<? extends Collection<T>> collectionProperty,
                                 Collection<T> valueCollection
    )
    {
        this.collectionProperty = collectionProperty;
        this.valueCollection = Collections.unmodifiableCollection( valueCollection );
    }

    public PropertyFunction<? extends Collection<T>> collectionProperty()
    {
        return collectionProperty;
    }

    public Collection<T> containedValues()
    {
        return valueCollection;
    }

    @Override
    public boolean test( Composite item )
    {
        Collection<T> collection = collectionProperty.apply( item ).get();

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
        return collectionProperty + " contains " + valueCollection;
    }
}
