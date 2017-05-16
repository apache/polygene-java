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
package org.apache.polygene.library.sql.generator.implementation.grammar.definition.table;

import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.common.ColumnNameList;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableConstraint;
import org.apache.polygene.library.sql.generator.grammar.definition.table.UniqueConstraint;
import org.apache.polygene.library.sql.generator.grammar.definition.table.UniqueSpecification;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class UniqueConstraintImpl extends SQLSyntaxElementBase<TableConstraint, UniqueConstraint>
    implements UniqueConstraint
{

    private final ColumnNameList _columns;
    private final UniqueSpecification _uniqueness;

    public UniqueConstraintImpl( SQLProcessorAggregator processor, ColumnNameList columns,
                                 UniqueSpecification uniqueness )
    {
        this( processor, UniqueConstraint.class, columns, uniqueness );
    }

    protected UniqueConstraintImpl( SQLProcessorAggregator processor,
                                    Class<? extends UniqueConstraint> realImplementingType, ColumnNameList columns, UniqueSpecification uniqueness )
    {
        super( processor, realImplementingType );

        Objects.requireNonNull( columns, "Columns" );
        Objects.requireNonNull( uniqueness, "Uniqueness" );

        this._columns = columns;
        this._uniqueness = uniqueness;
    }

    @Override
    protected boolean doesEqual( UniqueConstraint another )
    {
        return this._uniqueness.equals( another.getUniquenessKind() )
               && this._columns.equals( another.getColumnNameList() );
    }

    public ColumnNameList getColumnNameList()
    {
        return this._columns;
    }

    public UniqueSpecification getUniquenessKind()
    {
        return this._uniqueness;
    }
}
