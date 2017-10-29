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

import org.apache.polygene.library.sql.generator.grammar.common.datatypes.Decimal;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLDataType;
import org.apache.polygene.library.sql.generator.implementation.TypeableImpl;

/**
 *
 */
public final class DecimalImpl extends TypeableImpl<SQLDataType, Decimal>
    implements Decimal
{
    private final Integer _precision;
    private final Integer _scale;

    public DecimalImpl( Integer precision, Integer scale )
    {
        super( Decimal.class );

        this._precision = precision;
        this._scale = scale;
    }

    @Override
    protected boolean doesEqual( Decimal another )
    {
        return bothNullOrEquals( this._precision, another.getPrecision() )
               && bothNullOrEquals( this._scale, another.getScale() );
    }

    /**
     * Returns the precision (first integer) for this {@code DECIMAL}.
     *
     * @return The precision for this {@code DECIMAL}.
     */
    public Integer getPrecision()
    {
        return this._precision;
    }

    /**
     * Returns the scale (second integer) for this {@code DECIMAL}.
     *
     * @return The precision for this {@code DECIMAL}.
     */
    public Integer getScale()
    {
        return this._scale;
    }

    /**
     * This instance represents {@code DECIMAL} without precision and scale.
     */
    public static final Decimal PLAIN_DECIMAL = new DecimalImpl( null, null );
}