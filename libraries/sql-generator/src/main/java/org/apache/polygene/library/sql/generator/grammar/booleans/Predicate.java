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
package org.apache.polygene.library.sql.generator.grammar.booleans;

/**
 * A common interfaces for predicates (boolean expressions not containing other boolean expressions).
 *
 * @author Stanislav Muhametsin
 */
public interface Predicate
    extends BooleanExpression
{
    /**
     * A class representing empty predicate. The result of empty predicate is empty string. If empty predicate is
     * encountered inside {@link Conjunction} or {@link Disjunction}, their operator is omitted. So, <i>empty
     * predicate</i> {@code AND} <i>something</i> becomes just <i>something</i>.
     *
     * @author Stanislav Muhametsin
     */
    final class EmptyPredicate
        implements Predicate
    {
        private EmptyPredicate()
        {
        }

        /**
         * Returns {@link EmptyPredicate}.
         */
        public Class<? extends BooleanExpression> getImplementedType()
        {
            return EmptyPredicate.class;
        }

        /**
         * Singleton instance of {@link EmptyPredicate}.
         */
        public static final EmptyPredicate INSTANCE = new EmptyPredicate();
    }
}
