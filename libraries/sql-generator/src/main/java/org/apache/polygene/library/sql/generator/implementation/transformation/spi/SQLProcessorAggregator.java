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
package org.apache.polygene.library.sql.generator.implementation.transformation.spi;

import org.apache.polygene.library.sql.generator.Typeable;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;

/**
 *
 */
public interface SQLProcessorAggregator
{
    void process( Typeable<?> object, StringBuilder builder );

    SQLVendor getVendor();

    /**
     * <p>
     * This provides a way to access current path in a SQL syntax tree. The first element is the root one, then the next
     * one is its child, etc. This stack is read-only.
     * </p>
     * <p>
     * Currently I'm not sure if there is an efficient way to acquire read-only stacks rather than creating a new one
     * from contents of another.
     * </p>
     *
     * @return A current path in SQL syntax tree.
     */
    // public Stack<Typeable<?>> getCurrentSyntaxTree();
}
