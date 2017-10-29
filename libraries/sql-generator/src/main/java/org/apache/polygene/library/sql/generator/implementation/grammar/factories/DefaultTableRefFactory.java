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

import org.apache.polygene.library.sql.generator.grammar.booleans.BooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.builders.query.TableReferenceBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.ColumnNameList;
import org.apache.polygene.library.sql.generator.grammar.common.TableName;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameFunction;
import org.apache.polygene.library.sql.generator.grammar.literals.SQLFunctionLiteral;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpression;
import org.apache.polygene.library.sql.generator.grammar.query.TableAlias;
import org.apache.polygene.library.sql.generator.grammar.query.TableReferenceByExpression;
import org.apache.polygene.library.sql.generator.grammar.query.TableReferenceByName;
import org.apache.polygene.library.sql.generator.grammar.query.TableReferencePrimary;
import org.apache.polygene.library.sql.generator.grammar.query.joins.JoinCondition;
import org.apache.polygene.library.sql.generator.grammar.query.joins.NamedColumnsJoin;
import org.apache.polygene.library.sql.generator.implementation.grammar.builders.query.TableReferenceBuilderImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.TableNameDirectImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.TableNameFunctionImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.query.TableAliasImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.query.TableReferenceByExpressionImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.query.TableReferenceByNameImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.query.joins.JoinConditionImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.query.joins.NamedColumnsJoinImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;

/**
 *
 */
public class DefaultTableRefFactory extends AbstractTableRefFactory
{

    public DefaultTableRefFactory( SQLVendor vendor, SQLProcessorAggregator processor )
    {
        super( vendor, processor );
    }

    public JoinCondition jc( BooleanExpression condition )
    {
        return new JoinConditionImpl( this.getProcessor(), condition );
    }

    public NamedColumnsJoin nc( ColumnNameList columnNames )
    {
        return new NamedColumnsJoinImpl( this.getProcessor(), columnNames );
    }

    public TableReferenceByName table( TableName tableName, TableAlias alias )
    {
        return new TableReferenceByNameImpl( this.getProcessor(), tableName, alias );
    }

    public TableNameDirect tableName( String schemaName, String tableName )
    {
        return new TableNameDirectImpl( this.getProcessor(), schemaName, tableName );
    }

    public TableNameFunction tableName( String schemaName, SQLFunctionLiteral function )
    {
        return new TableNameFunctionImpl( this.getProcessor(), schemaName, function );
    }

    public TableAlias tableAliasWithCols( String tableNameAlias, String... colNames )
    {
        ColumnNameList colNameList = null;
        if( colNames.length > 0 )
        {
            colNameList = this.getVendor().getColumnsFactory().colNames( colNames );
        }

        return new TableAliasImpl( this.getProcessor(), tableNameAlias, colNameList );
    }

    public TableReferenceByExpression table( QueryExpression query, TableAlias alias )
    {
        return new TableReferenceByExpressionImpl( this.getProcessor(), query, alias );
    }

    public TableReferenceBuilder tableBuilder( TableReferencePrimary firstTable )
    {
        return new TableReferenceBuilderImpl( this.getProcessor(), firstTable );
    }
}
