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
package org.apache.polygene.library.sql.generator.implementation.grammar.builders.modification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.builders.modification.ColumnSourceByValuesBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.ColumnNameList;
import org.apache.polygene.library.sql.generator.grammar.common.ValueExpression;
import org.apache.polygene.library.sql.generator.grammar.modification.ColumnSourceByValues;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.ColumnNameListImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLBuilderBase;
import org.apache.polygene.library.sql.generator.implementation.grammar.modification.ColumnSourceByValuesImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class ColumnSourceByValuesBuilderImpl extends SQLBuilderBase
    implements ColumnSourceByValuesBuilder
{

    private final List<ValueExpression> _values;

    private final List<String> _columnNames;

    public ColumnSourceByValuesBuilderImpl( SQLProcessorAggregator processor )
    {
        super( processor );
        this._values = new ArrayList<ValueExpression>();
        this._columnNames = new ArrayList<String>();
    }

    public ColumnSourceByValues createExpression()
    {
        ColumnNameList list = null;
        if( !this._columnNames.isEmpty() )
        {
            list = new ColumnNameListImpl( this.getProcessor(), this._columnNames );
        }
        return new ColumnSourceByValuesImpl( this.getProcessor(), list, this._values );
    }

    public ColumnSourceByValuesBuilder addValues( ValueExpression... values )
    {
        for( ValueExpression exp : values )
        {
            Objects.requireNonNull( exp, "value" );
            this._values.add( exp );
        }
        return this;
    }

    public List<ValueExpression> getValues()
    {
        return Collections.unmodifiableList( this._values );
    }

    public ColumnSourceByValuesBuilder addColumnNames( String... columnNames )
    {
        for( String str : columnNames )
        {
            Objects.requireNonNull( str, "column name" );
            this._columnNames.add( str );
        }

        return this;
    }

    public List<String> getColumnNames()
    {
        return Collections.unmodifiableList( this._columnNames );
    }
}
