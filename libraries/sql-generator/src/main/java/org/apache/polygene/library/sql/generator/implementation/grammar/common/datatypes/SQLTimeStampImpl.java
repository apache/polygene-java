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
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLTimeStamp;
import org.apache.polygene.library.sql.generator.implementation.TypeableImpl;

/**
 * This class represents {@code TIMESTAMP} data type.
 *
 * @author Stanislav Muhametsin
 */
public final class SQLTimeStampImpl extends TypeableImpl<SQLDataType, SQLTimeStamp>
    implements SQLTimeStamp
{
    private final Integer _precision;
    private final Boolean _withTimeZone;

    public SQLTimeStampImpl( Integer precision, Boolean withTimeZone )
    {
        super( SQLTimeStamp.class );
        this._precision = precision;
        this._withTimeZone = withTimeZone;
    }

    @Override
    protected boolean doesEqual( SQLTimeStamp another )
    {
        return bothNullOrEquals( this._precision, another.getPrecision() )
               && bothNullOrEquals( this._withTimeZone, another.isWithTimeZone() );
    }

    /**
     * Returns the precision for this {@code TIMESTAMP}. May be {@code null}.
     *
     * @return The precision for this {@code TIMESTAMP}.
     */
    public Integer getPrecision()
    {
        return this._precision;
    }

    /**
     * Returns whether the {@code TIMESTAMP} should be with time zone. May be {@code null} if no choice specified.
     *
     * @return
     */
    public Boolean isWithTimeZone()
    {
        return this._withTimeZone;
    }

    public static final SQLTimeStamp PLAIN_TIMESTAMP = new SQLTimeStampImpl( null, null );

    public static final SQLTimeStamp PLAIN_TIMESTAMP_WITHOUT_TZ = new SQLTimeStampImpl( null, false );

    public static final SQLTimeStamp PLAIN_TIMESTAMP_WITH_TZ = new SQLTimeStampImpl( null, true );
}