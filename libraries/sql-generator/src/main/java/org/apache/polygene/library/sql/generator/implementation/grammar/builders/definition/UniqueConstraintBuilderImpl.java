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
package org.apache.polygene.library.sql.generator.implementation.grammar.builders.definition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.builders.definition.UniqueConstraintBuilder;
import org.apache.polygene.library.sql.generator.grammar.definition.table.UniqueConstraint;
import org.apache.polygene.library.sql.generator.grammar.definition.table.UniqueSpecification;
import org.apache.polygene.library.sql.generator.grammar.factories.ColumnsFactory;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLBuilderBase;
import org.apache.polygene.library.sql.generator.implementation.grammar.definition.table.UniqueConstraintImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class UniqueConstraintBuilderImpl extends SQLBuilderBase
    implements UniqueConstraintBuilder
{

    private UniqueSpecification _uniqueness;
    private final List<String> _columns;

    private final ColumnsFactory _c;

    public UniqueConstraintBuilderImpl( SQLProcessorAggregator processor, ColumnsFactory c )
    {
        super( processor );
        Objects.requireNonNull( c, "Columns factory" );

        this._c = c;
        this._columns = new ArrayList<String>();
    }

    public UniqueConstraint createExpression()
    {
        return new UniqueConstraintImpl( this.getProcessor(), this._c.colNames( this._columns ), this._uniqueness );
    }

    public UniqueConstraintBuilder setUniqueness( UniqueSpecification uniqueness )
    {
        this._uniqueness = uniqueness;
        return this;
    }

    public UniqueConstraintBuilder addColumns( String... columnNames )
    {
        for( String col : columnNames )
        {
            this._columns.add( col );
        }
        return this;
    }

    public UniqueSpecification getUniqueness()
    {
        return this._uniqueness;
    }

    public List<String> getColumns()
    {
        return this._columns;
    }
}
