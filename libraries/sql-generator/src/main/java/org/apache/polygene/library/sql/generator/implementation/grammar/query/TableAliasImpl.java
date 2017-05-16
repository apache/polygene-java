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
package org.apache.polygene.library.sql.generator.implementation.grammar.query;

import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.common.ColumnNameList;
import org.apache.polygene.library.sql.generator.grammar.query.TableAlias;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class TableAliasImpl extends SQLSyntaxElementBase<TableAlias, TableAlias>
    implements TableAlias
{
    private final String _tableAlias;

    private final ColumnNameList _columnAliases;

    public TableAliasImpl( SQLProcessorAggregator processor, String tableAlias, ColumnNameList columnNames )
    {
        this( processor, TableAlias.class, tableAlias, columnNames );
    }

    protected TableAliasImpl( SQLProcessorAggregator processor, Class<? extends TableAlias> implementingClass,
                              String tableAlias, ColumnNameList columnNames )
    {
        super( processor, implementingClass );
        Objects.requireNonNull( tableAlias, "table alias table name" );
        this._tableAlias = tableAlias;
        this._columnAliases = columnNames;
    }

    public ColumnNameList getColumnAliases()
    {
        return this._columnAliases;
    }

    public String getTableAlias()
    {
        return this._tableAlias;
    }

    @Override
    protected boolean doesEqual( TableAlias another )
    {
        return this._tableAlias.equals( another.getTableAlias() )
               && bothNullOrEquals( this._columnAliases, another.getColumnAliases() );
    }
}
