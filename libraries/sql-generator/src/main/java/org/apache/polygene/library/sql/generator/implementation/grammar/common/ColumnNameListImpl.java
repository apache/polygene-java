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
package org.apache.polygene.library.sql.generator.implementation.grammar.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.common.ColumnNameList;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class ColumnNameListImpl extends SQLSyntaxElementBase<ColumnNameList, ColumnNameList>
    implements ColumnNameList
{

    private final List<String> _columnNames;

    public ColumnNameListImpl( SQLProcessorAggregator processor, Collection<String> columnNames )
    {
        this( processor, ColumnNameList.class, columnNames );
    }

    protected ColumnNameListImpl( SQLProcessorAggregator processor,
                                  Class<? extends ColumnNameList> implClass,
                                  Collection<String> columnNames )
    {
        super( processor, implClass );
        Objects.requireNonNull( columnNames, "column names" );

        if( columnNames.isEmpty() )
        {
            throw new IllegalArgumentException( "Column name list must have at least one column." );
        }

        for( String columnName : columnNames )
        {
            Objects.requireNonNull( columnName, "column name" );
        }

        this._columnNames = Collections.unmodifiableList( new ArrayList<String>( columnNames ) );
    }

    public ColumnNameListImpl( SQLProcessorAggregator processor, String... columnNames )
    {
        this( processor, Arrays.asList( columnNames ) );
    }

    public List<String> getColumnNames()
    {
        return this._columnNames;
    }

    @Override
    protected boolean doesEqual( ColumnNameList another )
    {
        return this._columnNames.equals( another.getColumnNames() );
    }
}
