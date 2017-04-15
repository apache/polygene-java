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
package org.apache.polygene.library.sql.generator.implementation.grammar.factories;

import org.apache.polygene.library.sql.generator.grammar.common.datatypes.Decimal;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.Numeric;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLChar;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLFloat;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLTime;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLTimeStamp;
import org.apache.polygene.library.sql.generator.grammar.factories.DataTypeFactory;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLFactoryBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;

/**
 * @author Stanislav Muhametsin
 */
public abstract class AbstractDataTypeFactory extends SQLFactoryBase
    implements DataTypeFactory
{

    protected AbstractDataTypeFactory( SQLVendor vendor, SQLProcessorAggregator processor )
    {
        super( vendor, processor );
    }

    public Decimal decimal()
    {
        return this.decimal( null, null );
    }

    public Decimal decimal( Integer precision )
    {
        return this.decimal( precision, null );
    }

    public Numeric numeric()
    {
        return this.numeric( null, null );
    }

    public Numeric numeric( Integer precision )
    {
        return this.numeric( precision, null );
    }

    public SQLChar sqlChar()
    {
        return this.sqlChar( null );
    }

    public SQLFloat sqlFloat()
    {
        return this.sqlFloat( null );
    }

    public SQLChar sqlVarChar()
    {
        return this.sqlVarChar( null );
    }

    public SQLTime time()
    {
        return this.time( null, null );
    }

    public SQLTime time( Boolean withTimeZone )
    {
        return this.time( null, withTimeZone );
    }

    public SQLTime time( Integer precision )
    {
        return this.time( precision, null );
    }

    public SQLTimeStamp timeStamp()
    {
        return this.timeStamp( null, null );
    }

    public SQLTimeStamp timeStamp( Boolean withTimeZone )
    {
        return this.timeStamp( null, withTimeZone );
    }

    public SQLTimeStamp timeStamp( Integer precision )
    {
        return this.timeStamp( precision, null );
    }
}
