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

import java.sql.Timestamp;
import org.apache.polygene.library.sql.generator.grammar.common.SQLConstants;
import org.apache.polygene.library.sql.generator.grammar.common.ValueExpression;
import org.apache.polygene.library.sql.generator.grammar.factories.LiteralFactory;
import org.apache.polygene.library.sql.generator.grammar.literals.DirectLiteral;
import org.apache.polygene.library.sql.generator.grammar.literals.NumericLiteral;
import org.apache.polygene.library.sql.generator.grammar.literals.SQLFunctionLiteral;
import org.apache.polygene.library.sql.generator.grammar.literals.StringLiteral;
import org.apache.polygene.library.sql.generator.grammar.literals.TimestampTimeLiteral;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLFactoryBase;
import org.apache.polygene.library.sql.generator.implementation.grammar.literals.DirectLiteralImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.literals.NumericLiteralImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.literals.SQLFunctionLiteralImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.literals.StringLiteralImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.literals.TimestampLiteralImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;

/**
 *
 */
public class DefaultLiteralFactory extends SQLFactoryBase
    implements LiteralFactory
{

    private final DirectLiteral _param;

    public DefaultLiteralFactory( SQLVendor vendor, SQLProcessorAggregator processor )
    {
        super( vendor, processor );

        this._param = new DirectLiteralImpl( this.getProcessor(), SQLConstants.QUESTION_MARK );
    }

    public DirectLiteral l( String literalContents )
    {
        return new DirectLiteralImpl( this.getProcessor(), literalContents );
    }

    public DirectLiteral param()
    {
        return _param;
    }

    public StringLiteral s( String literal )
    {
        return new StringLiteralImpl( this.getProcessor(), literal );
    }

    public TimestampTimeLiteral dt( Timestamp date )
    {
        return new TimestampLiteralImpl( this.getProcessor(), date );
    }

    public NumericLiteral n( Number number )
    {
        return new NumericLiteralImpl( this.getProcessor(), number );
    }

    public SQLFunctionLiteral func( String name, ValueExpression... parameters )
    {
        return new SQLFunctionLiteralImpl( this.getProcessor(), name, parameters );
    }
}
