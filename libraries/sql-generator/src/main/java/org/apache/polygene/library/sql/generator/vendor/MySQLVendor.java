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
package org.apache.polygene.library.sql.generator.vendor;

/**
 * This is vendor for MySQL database. MySQL typically does not understand schema-qualified names, so this vendor doesn't
 * process any schema name it encounters when creating a SQL string.
 *
 * @author Stanislav Muhametsin
 */
public interface MySQLVendor
    extends SQLVendor
{

    /**
     * <p>
     * Returns whether the legacy LIMIT expression is used instead of the of the newer LIMIT/OFFSET. In a legacy syntax,
     * the {@code LIMIT <n> OFFSET <skip>} is written as {@code LIMIT <skip>, <n> } instead. This method is not
     * thread-safe.
     * </p>
     *
     * @return {@code true} if the legacy LIMIT expressions are used; {@code false} otherwise.
     */
    boolean legacyLimit();

    /**
     * <p>
     * Sets the switch to use legacy LIMIT expression instead of the newer LIMIT/OFFSET expressions. In a legacy syntax,
     * the {@code LIMIT <n> OFFSET <skip>} is written as {@code LIMIT <skip>, <n> } instead. Set this to true only if
     * you really require it. This method is not thread-safe.
     * </p>
     *
     * @param useLegacyLimit Whether to use legacy LIMIT expression instead of the newer LIMIT/OFFSET expressions.
     */
    void setLegacyLimit( boolean useLegacyLimit );
}
