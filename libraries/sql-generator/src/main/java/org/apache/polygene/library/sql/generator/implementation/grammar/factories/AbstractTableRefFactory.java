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

import org.apache.polygene.library.sql.generator.grammar.common.TableName;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameFunction;
import org.apache.polygene.library.sql.generator.grammar.factories.TableReferenceFactory;
import org.apache.polygene.library.sql.generator.grammar.literals.SQLFunctionLiteral;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpression;
import org.apache.polygene.library.sql.generator.grammar.query.TableAlias;
import org.apache.polygene.library.sql.generator.grammar.query.TableReferenceByExpression;
import org.apache.polygene.library.sql.generator.grammar.query.TableReferenceByName;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLFactoryBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;

/**
 *
 */
public abstract class AbstractTableRefFactory extends SQLFactoryBase
    implements TableReferenceFactory
{

    protected AbstractTableRefFactory( SQLVendor vendor, SQLProcessorAggregator processor )
    {
        super( vendor, processor );
    }

    public TableReferenceByName table( TableName tableName )
    {
        return this.table( tableName, null );
    }

    public TableNameDirect tableName( String tableName )
    {
        return this.tableName( null, tableName );
    }

    public TableAlias tableAlias( String tableNameAlias )
    {
        return this.tableAliasWithCols( tableNameAlias );
    }

    public TableReferenceByExpression table( QueryExpression query )
    {
        return this.table( query, null );
    }

    public TableNameFunction tableName( SQLFunctionLiteral function )
    {
        return this.tableName( null, function );
    }
}
