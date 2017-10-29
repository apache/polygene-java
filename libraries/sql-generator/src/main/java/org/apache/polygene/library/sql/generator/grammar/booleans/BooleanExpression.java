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

import org.apache.polygene.library.sql.generator.grammar.common.ValueExpression;

/**
 * A common interface for all boolean expressions in SQL language.
 *
 */
public interface BooleanExpression
    extends ValueExpression
{

    /**
     * This class represents a boolean expression which always evaluates to true ({@code TRUE}).
     *
     */
    final class True
        implements BooleanExpression
    {
        private True()
        {

        }

        /**
         * Returns {@link True}.
         */
        public Class<? extends ValueExpression> getImplementedType()
        {
            return True.class;
        }

        /**
         * Returns the singleton instance representing {@code TRUE}.
         */
        public static final True INSTANCE = new True();
    }

    /**
     * This class represents a boolean expression which always evaluates to false ({@code FALSE}.
     *
     */
    final class False
        implements BooleanExpression
    {
        private False()
        {

        }

        /**
         * Returns {@link False}.
         */
        public Class<? extends ValueExpression> getImplementedType()
        {
            return False.class;
        }

        /**
         * Returns the singleton instance representing {@code FALSE}.
         */
        public static final False INSTANCE = new False();
    }
}
