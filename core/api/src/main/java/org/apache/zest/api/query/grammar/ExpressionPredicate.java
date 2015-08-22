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
package org.apache.zest.api.query.grammar;

import java.util.function.Predicate;
import org.apache.zest.api.composite.Composite;

import static org.apache.zest.functional.Iterables.append;
import static org.apache.zest.functional.Iterables.iterable;

/**
 * Base expression Specification.
 */
public abstract class ExpressionPredicate
    implements Predicate<Composite>
{

//    @SuppressWarnings( "unchecked" )
//    public AndPredicate and( Predicate<Composite> specification )
//    {
//        if( this instanceof AndPredicate )
//        {
//            return new AndPredicate( append( specification, ( (AndPredicate) this ).operands() ) );
//        }
//        else
//        {
//            return new AndPredicate( iterable( this, specification ) );
//        }
//    }
//
//    @SuppressWarnings( "unchecked" )
//    public OrPredicate or( Predicate<Composite> specification )
//    {
//        if( this instanceof OrPredicate )
//        {
//            return new OrPredicate( append( specification, ( (OrPredicate) this ).operands() ) );
//        }
//        else
//        {
//            return new OrPredicate( iterable( this, specification ) );
//        }
//    }
//
}
