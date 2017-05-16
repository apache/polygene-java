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
package org.apache.polygene.library.sql.generator.implementation.grammar.common.datatypes;

import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLDataType;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLFloat;
import org.apache.polygene.library.sql.generator.implementation.TypeableImpl;

/**
 * @author Stanislav Muhametsin
 */
public final class SQLFloatImpl extends TypeableImpl<SQLDataType, SQLFloat>
    implements SQLFloat
{
    private final Integer _precision;

    public SQLFloatImpl( Integer precision )
    {
        super( SQLFloat.class );
        this._precision = precision;
    }

    @Override
    protected boolean doesEqual( SQLFloat another )
    {
        return bothNullOrEquals( this._precision, another.getPrecision() );
    }

    /**
     * Returns the precision for this {@code FLOAT}.
     *
     * @return The precision for this {@code FLOAT}.
     */
    public Integer getPrecision()
    {
        return this._precision;
    }

    /**
     * This instance represents {@code FLOAT} without precision.
     */
    public static final SQLFloat PLAIN_FLOAT = new SQLFloatImpl( null );
}