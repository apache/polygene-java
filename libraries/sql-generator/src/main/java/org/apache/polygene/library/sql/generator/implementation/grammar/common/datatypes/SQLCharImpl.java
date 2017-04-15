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

import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLChar;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLDataType;
import org.apache.polygene.library.sql.generator.implementation.TypeableImpl;

public final class SQLCharImpl extends TypeableImpl<SQLDataType, SQLChar>
    implements SQLChar
{
    private final Boolean _isVarying;
    private final Integer _length;

    public SQLCharImpl( Boolean isVarying, Integer length )
    {
        super( SQLChar.class );
        this._isVarying = isVarying;
        this._length = length;
    }

    @Override
    protected boolean doesEqual( SQLChar another )
    {
        return this._isVarying.equals( another.isVarying() ) && bothNullOrEquals( this._length, another.getLength() );
    }

    /**
     * Returns {@code true} if this is {@code CHARACTER VARYING}; {@code false otherwise}.
     *
     * @return {@code true} if this is {@code CHARACTER VARYING}; {@code false otherwise}.
     */
    public Boolean isVarying()
    {
        return this._isVarying;
    }

    /**
     * Returns the length specification for this {@code CHARACTER} or {@code CHARACTER VARYING}. Returns {@code null} if
     * none specified.
     *
     * @return The length for this {@code CHARACTER} or {@code CHARACTER VARYING}.
     */
    public Integer getLength()
    {
        return this._length;
    }

    public static final SQLChar PLAIN_FIXED = new SQLCharImpl( false, null );

    public static final SQLChar PLAIN_VARYING = new SQLCharImpl( true, null );
}