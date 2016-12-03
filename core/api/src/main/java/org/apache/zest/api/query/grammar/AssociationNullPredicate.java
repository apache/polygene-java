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
package org.apache.zest.api.query.grammar;

import org.apache.zest.api.association.Association;
import org.apache.zest.api.composite.Composite;

/**
 * Association null Specification.
 */
public class AssociationNullPredicate<T>
    extends ExpressionPredicate
{
    private AssociationFunction<T> association;

    public AssociationNullPredicate( AssociationFunction<T> association )
    {
        this.association = association;
    }

    public AssociationFunction<T> association()
    {
        return association;
    }

    @Override
    public boolean test( Composite item )
    {
        try
        {
            Association<T> assoc = association.apply( item );

            if( assoc == null )
            {
                return true;
            }

            return assoc.get() == null;
        }
        catch( IllegalArgumentException e )
        {
            return true;
        }
    }

    @Override
    public String toString()
    {
        return association.toString() + " is null";
    }
}
