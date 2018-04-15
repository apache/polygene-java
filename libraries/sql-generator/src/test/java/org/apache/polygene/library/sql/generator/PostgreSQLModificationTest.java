/*
 * Copyright (c) 2012, Stanislav Muhametsin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.polygene.library.sql.generator;

import org.apache.polygene.library.sql.generator.grammar.builders.modification.pgsql.PgSQLInsertStatementBuilder;
import org.apache.polygene.library.sql.generator.grammar.factories.ColumnsFactory;
import org.apache.polygene.library.sql.generator.grammar.factories.LiteralFactory;
import org.apache.polygene.library.sql.generator.grammar.factories.ModificationFactory;
import org.apache.polygene.library.sql.generator.grammar.factories.QueryFactory;
import org.apache.polygene.library.sql.generator.grammar.factories.TableReferenceFactory;
import org.apache.polygene.library.sql.generator.grammar.modification.InsertStatement;
import org.apache.polygene.library.sql.generator.grammar.modification.ValueSource;
import org.apache.polygene.library.sql.generator.vendor.PostgreSQLVendor;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;
import org.apache.polygene.library.sql.generator.vendor.SQLVendorProvider;
import org.junit.jupiter.api.Test;

public class PostgreSQLModificationTest extends AbstractModificationTest
{

    @Override
    protected SQLVendor loadVendor()
        throws Exception
    {
        return SQLVendorProvider.createVendor( PostgreSQLVendor.class );
    }

    @Test
    public void pgInsert1()
    {
        // INSERT INTO some_schema.some_table
        // VALUES (DEFAULT, "SomeString")
        // RETURNING id_column;
        SQLVendor vendor = this.getVendor();

        QueryFactory q = vendor.getQueryFactory();
        TableReferenceFactory t = vendor.getTableReferenceFactory();
        ModificationFactory m = vendor.getModificationFactory();
        LiteralFactory l = vendor.getLiteralFactory();
        ColumnsFactory c = vendor.getColumnsFactory();

        InsertStatement insert =
            ( (PgSQLInsertStatementBuilder) m.insert() )
                .setReturningClause(
                    q.columnsBuilder().addUnnamedColumns( c.colName( "id_column" ) )
                     .createExpression() )
                .setTableName( t.tableName( "some_schema", "some_table" ) )
                .setColumnSource(
                    m.columnSourceByValues()
                     .addValues( ValueSource.Default.INSTANCE, l.s( "SomeString" ) )
                     .createExpression()
                                ).createExpression();

        logStatement( "PGSQL table modification", vendor, insert );
    }
}
