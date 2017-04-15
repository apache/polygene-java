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

import org.apache.polygene.library.sql.generator.grammar.common.datatypes.Numeric;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLDataType;
import org.apache.polygene.library.sql.generator.implementation.TypeableImpl;

/**
 * @author Stanislav Muhametsin
 */
public final class NumericImpl extends TypeableImpl<SQLDataType, Numeric>
    implements Numeric
{
    private final Integer _precision;
    private final Integer _scale;

    public NumericImpl( Integer precision, Integer scale )
    {
        super( Numeric.class );
        this._precision = precision;
        this._scale = scale;
    }

    @Override
    protected boolean doesEqual( Numeric another )
    {
        return bothNullOrEquals( this._precision, another.getPrecision() )
               && bothNullOrEquals( this._scale, another.getScale() );
    }

    /**
     * Returns the precision (first integer) for this {@code NUMERIC}.
     *
     * @return The precision for this {@code NUMERIC}.
     */
    public Integer getPrecision()
    {
        return this._precision;
    }

    /**
     * Returns the scale (second integer) for this {@code NUMERIC}.
     *
     * @return The precision for this {@code NUMERIC}.
     */
    public Integer getScale()
    {
        return this._scale;
    }

    /**
     * This instance represents {@code NUMERIC} without precision and scale.
     */
    public static final Numeric PLAIN_NUMERIC = new NumericImpl( null, null );
}