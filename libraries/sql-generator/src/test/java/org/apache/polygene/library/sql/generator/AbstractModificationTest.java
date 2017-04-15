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

import org.apache.polygene.library.sql.generator.grammar.factories.LiteralFactory;
import org.apache.polygene.library.sql.generator.grammar.factories.ModificationFactory;
import org.apache.polygene.library.sql.generator.grammar.factories.QueryFactory;
import org.apache.polygene.library.sql.generator.grammar.factories.TableReferenceFactory;
import org.apache.polygene.library.sql.generator.grammar.modification.InsertStatement;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;
import org.junit.Test;

public abstract class AbstractModificationTest extends AbstractSQLSyntaxTest
{

    @Test
    public void modification1()
    {
        // INSERT INTO schema.table
        // VALUES (5, 'String', SELECT column FROM schema.other_table);

        SQLVendor vendor = this.getVendor();

        QueryFactory q = vendor.getQueryFactory();
        TableReferenceFactory t = vendor.getTableReferenceFactory();
        ModificationFactory m = vendor.getModificationFactory();
        LiteralFactory l = vendor.getLiteralFactory();

        InsertStatement insert = this.getVendor().getModificationFactory().insert()
                                     .setTableName( t.tableName( "schema", "table" ) )
                                     .setColumnSource(
                                         m.columnSourceByValues().addValues(
                                             l.n( 5 ),
                                             l.s( "String" ),
                                             q.simpleQueryBuilder()
                                              .select( "column" )
                                              .from( t.tableName( "schema", "other_table" ) )
                                              .createExpression()
                                                                           ).createExpression()
                                                     )
                                     .createExpression();

        this.logStatement( "Table modification", vendor, insert );
    }
}
