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

import org.apache.polygene.library.sql.generator.grammar.common.NonBooleanExpression;

/**
 * The interface for syntax element representing SQL expression {@code BETWEEN }x {@code AND} y.
 *
 */
public interface BetweenPredicate
    extends MultiPredicate
{

    /**
     * Returns the minimum value (the expression on the left side of {@code AND}).
     *
     * @return The minimum value.
     */
    NonBooleanExpression getMinimum();

    /**
     * Returns the maxmimum value (the expression on the right side of {@code AND}).
     *
     * @return The maximum value.
     */
    NonBooleanExpression getMaximum();
}
