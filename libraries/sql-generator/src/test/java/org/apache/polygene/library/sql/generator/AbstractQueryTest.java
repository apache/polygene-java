/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
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

import org.apache.polygene.library.sql.generator.grammar.booleans.BooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.builders.query.ColumnsBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.query.QuerySpecificationBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.query.TableReferenceBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.SetQuantifier;
import org.apache.polygene.library.sql.generator.grammar.factories.BooleanFactory;
import org.apache.polygene.library.sql.generator.grammar.factories.ColumnsFactory;
import org.apache.polygene.library.sql.generator.grammar.factories.LiteralFactory;
import org.apache.polygene.library.sql.generator.grammar.factories.QueryFactory;
import org.apache.polygene.library.sql.generator.grammar.factories.TableReferenceFactory;
import org.apache.polygene.library.sql.generator.grammar.query.ColumnReferenceByName;
import org.apache.polygene.library.sql.generator.grammar.query.Ordering;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpression;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpressionBody;
import org.apache.polygene.library.sql.generator.grammar.query.joins.JoinType;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;
import org.junit.jupiter.api.Test;

/**
 * Contains the tests for various queries to test functionality of parser.
 * <p>
 * TODO how to actually verify queries? Currently this only outputs them.
 *
 */
public abstract class AbstractQueryTest extends AbstractSQLSyntaxTest
{
    protected void logQuery( SQLVendor vendor, QueryExpression query )
    {
        this.logStatement( "Query", vendor, query );
    }

    @Test
    public void query1()
        throws Exception
    {
        // @formatter:off
        /*
        
          SELECT t0.entity_identity
          FROM (
          SELECT DISTINCT t0.entity_pk, t0.entity_identity
            FROM qi4j.entities t0
            JOIN qi4j.qname_6 t1 ON (t0.entity_pk = t1.entity_pk AND t1.parent_qname IS NULL)
            JOIN qi4j.qname_14 t2 ON (t1.qname_id = t2.parent_qname AND t1.entity_pk = t2.entity_pk)
            JOIN qi4j.qname_15 t3 ON (t2.qname_id = t3.parent_qname AND t2.entity_pk = t3.entity_pk)
            WHERE t0.entity_type_id IN (3, 4) AND
            ((t3.qname_value IS NOT NULL AND t3.qname_value = ?) )
            ) AS t0
         
         */

        SQLVendor vendor = this.getVendor();

        QueryFactory q = vendor.getQueryFactory();
        BooleanFactory b = vendor.getBooleanFactory();
        TableReferenceFactory t = vendor.getTableReferenceFactory();
        LiteralFactory l = vendor.getLiteralFactory();
        ColumnsFactory c = vendor.getColumnsFactory();

        ColumnsBuilder innerSelectCols = q.columnsBuilder( SetQuantifier.DISTINCT ).addUnnamedColumns(
            c.colName( "t0", "entity_pk" ), c.colName( "t0", "entity_identity" ) );

        TableReferenceBuilder join = t.tableBuilder(
            t.table( t.tableName( "qi4j", "entities" ), t.tableAlias( "t0" ) )
                                                   ).addQualifiedJoin(
            JoinType.INNER,
            t.table( t.tableName( "qi4j", "qname_6" ), t.tableAlias( "t1" ) ),
            t.jc(
                b.booleanBuilder(
                    b.eq( c.colName( "t0", "entity_pk" ), c.colName( "t1", "entity_pk" ) )
                                ).and(
                    b.isNull( c.colName( "t1", "parent_qname" ) )
                                     ).createExpression()
                )
                                                                     ).addQualifiedJoin(
            JoinType.INNER,
            t.table( t.tableName( "qi4j", "qname_14" ), t.tableAlias( "t2" ) ),
            t.jc(
                b.booleanBuilder(
                    b.eq( c.colName( "t1", "qname_id" ), c.colName( "t2", "parent_qname" ) )
                                ).and(
                    b.eq( c.colName( "t1", "entity_pk" ), c.colName( "t2", "entity_pk" ) )
                                     ).createExpression()
                )
                                                                                       ).addQualifiedJoin(
            JoinType.INNER,
            t.table( t.tableName( "qi4j", "qname_15" ), t.tableAlias( "t3" ) ),
            t.jc(
                b.booleanBuilder(
                    b.eq( c.colName( "t2", "qname_id" ), c.colName( "t3", "parent_qname" ) )
                                ).and(
                    b.eq( c.colName( "t2", "entity_pk" ), c.colName( "t3", "entity_pk" ) )
                                     ).createExpression()
                )
                                                                                                         );

        BooleanExpression innerWhere = b.booleanBuilder(
            b.in( c.colName( "t0", "entity_type_id" ), l.n( 3 ), l.n( 4 ) )
                                                       ).and( b.isNotNull( c.colName( "t3", "qname_value" ) )
                                                            ).and( b.eq( c.colName( "t3", "qname_value" ), l.param() )
                                                                 ).createExpression();

        QuerySpecificationBuilder builder = q.querySpecificationBuilder();
        builder.setSelect( innerSelectCols );
        builder.getFrom().addTableReferences( join );
        builder.getWhere().reset( innerWhere );

        QuerySpecificationBuilder select =
            q.querySpecificationBuilder();
        select.getSelect().addUnnamedColumns( c.colName( "t0", "entity_identity" ) );
        select.getFrom().addTableReferences( t.tableBuilder( t.table( q.createQuery( builder.createExpression() ), t.tableAlias( "t0" ) ) ) );

        QueryExpression query = q.createQuery( q.queryBuilder( select.createExpression() ).createExpression() );

        this.logQuery( vendor, query );

        // @formatter:on

    }

    @Test
    public void query2()
        throws Exception
    {
        // @formatter:off
        /*
          SELECT t0.entity_identity
          FROM (SELECT DISTINCT t0.entity_pk, t0.entity_identity
            FROM qi4j.entities t0
            WHERE t0.entity_type_id IN (3, 4)
            EXCEPT
            SELECT DISTINCT t0.entity_pk, t0.entity_identity
            FROM qi4j.entities t0
            JOIN qi4j.qname_6 t1 ON (t0.entity_pk = t1.entity_pk AND t1.parent_qname IS NULL)
            JOIN qi4j.qname_11 t2 ON (t1.qname_id = t2.parent_qname AND t1.entity_pk = t2.entity_pk)
            LEFT JOIN qi4j.qname_12 t3 ON (t2.qname_id = t3.parent_qname AND t2.entity_pk = t3.entity_pk)
            LEFT JOIN qi4j.qname_13 t4 ON (t2.qname_id = t4.parent_qname AND t2.entity_pk = t4.entity_pk)
            WHERE t0.entity_type_id IN (3, 4)
            GROUP BY t0.entity_pk, t0.entity_identity
            HAVING COUNT(t2.qname_value) >= 2
            ORDER BY t0.entity_pk ASC
          ) AS t0
        */
        // @formatter:on

        SQLVendor vendor = this.getVendor();

        QueryFactory q = vendor.getQueryFactory();
        BooleanFactory b = vendor.getBooleanFactory();
        TableReferenceFactory t = vendor.getTableReferenceFactory();
        LiteralFactory l = vendor.getLiteralFactory();
        ColumnsFactory c = vendor.getColumnsFactory();

        ColumnReferenceByName innerFirstCol = c.colName( "t0", "entity_pk" );
        ColumnReferenceByName innerSecondCol = c.colName( "t0", "entity_identity" );
        ColumnsBuilder innerSelectCols = q.columnsBuilder( SetQuantifier.DISTINCT ).addUnnamedColumns( innerFirstCol,
                                                                                                       innerSecondCol );

        BooleanExpression where = b.in( c.colName( "t0", "entity_type_id" ), l.n( 3 ), l.n( 4 ) );
        QuerySpecificationBuilder firstInnerQuery = q.querySpecificationBuilder();
        firstInnerQuery.setSelect( innerSelectCols );

        firstInnerQuery.getFrom().addTableReferences(
            t.tableBuilder( t.table( t.tableName( "qi4j", "entities" ), t.tableAlias( "t0" ) ) ) );

        firstInnerQuery.getWhere().reset( where );

        TableReferenceBuilder join = t
            .tableBuilder( t.table( t.tableName( "qi4j", "entities" ), t.tableAlias( "t0" ) ) )
            .addQualifiedJoin(
                JoinType.INNER,
                t.table( t.tableName( "qi4j", "qname_6" ), t.tableAlias( "t1" ) ),
                t.jc( b.booleanBuilder( b.eq( c.colName( "t0", "entity_pk" ), c.colName( "t1", "entity_pk" ) ) )
                       .and( b.isNull( c.colName( "t1", "parent_qname" ) ) ).createExpression() ) )
            .addQualifiedJoin(
                JoinType.INNER,
                t.table( t.tableName( "qi4j", "qname_11" ), t.tableAlias( "t2" ) ),
                t.jc( b.booleanBuilder( b.eq( c.colName( "t1", "qname_id" ), c.colName( "t2", "parent_qname" ) ) )
                       .and( b.eq( c.colName( "t1", "entity_pk" ), c.colName( "t2", "entity_pk" ) ) ).createExpression() ) )
            .addQualifiedJoin(
                JoinType.LEFT_OUTER,
                t.table( t.tableName( "qi4j", "qname_12" ), t.tableAlias( "t3" ) ),
                t.jc( b.booleanBuilder( b.eq( c.colName( "t2", "qname_id" ), c.colName( "t3", "parent_qname" ) ) )
                       .and( b.eq( c.colName( "t2", "entity_pk" ), c.colName( "t3", "entity_pk" ) ) ).createExpression() ) )
            .addQualifiedJoin(
                JoinType.LEFT_OUTER,
                t.table( t.tableName( "qi4j", "qname_13" ), t.tableAlias( "t4" ) ),
                t.jc( b.booleanBuilder( b.eq( c.colName( "t3", "qname_id" ), c.colName( "t4", "parent_qname" ) ) )
                       .and( b.eq( c.colName( "t2", "entity_pk" ), c.colName( "t4", "entity_pk" ) ) ).createExpression() ) );

        QuerySpecificationBuilder secondBuilder = q.querySpecificationBuilder();
        secondBuilder.setSelect( innerSelectCols );
        secondBuilder.getFrom().addTableReferences( join );
        secondBuilder.getWhere().reset( where );
        secondBuilder.getGroupBy().addGroupingElements( q.groupingElement( innerFirstCol ),
                                                        q.groupingElement( innerSecondCol ) );
        secondBuilder.getHaving().reset( b.geq( l.func( "COUNT", c.colName( "t2", "qname_value" ) ), l.n( 2 ) ) );
        secondBuilder.getOrderBy().addSortSpecs( q.sortSpec( c.colName( "t0", "entity_pk" ), Ordering.ASCENDING ) );

        QueryExpressionBody innerQuery = q.queryBuilder( firstInnerQuery.createExpression() )
                                          .except( secondBuilder.createExpression() ).createExpression();

        QuerySpecificationBuilder select = q.querySpecificationBuilder().setSelect(
            q.columnsBuilder().addUnnamedColumns( c.colName( "t0", "entity_identity" ) ) );
        select.getFrom().addTableReferences(
            t.tableBuilder( t.table( q.createQuery( innerQuery ), t.tableAlias( "t0" ) ) ) );

        QueryExpression query = q.createQuery( q.queryBuilder( select.createExpression() ).createExpression() );

        this.logQuery( vendor, query );
    }

    @Test
    public void query3()
        throws Exception
    {
        // @formatter:off
        // SELECT COUNT(firstCol), MAX(secondCol)
        // FROM schema.table;
        // @formatter:on

        // This test is related to bug reported by Paul Merlin
        // The simple query builder was iterating the columns in erroneus way,
        // thus skipping all non-aliased columns
        SQLVendor vendor = this.getVendor();

        QueryFactory q = vendor.getQueryFactory();
        TableReferenceFactory t = vendor.getTableReferenceFactory();

        String firstCol = "firstCol";
        String secondCol = "secondCol";
        String schemaName = "schema";
        String tableName = "table";

        QueryExpression query = q.simpleQueryBuilder().select( "COUNT(" + firstCol + ")", "MAX(" + secondCol + ")" )
                                 .from( t.tableName( schemaName, tableName ) ).createExpression();

        this.logQuery( vendor, query );
    }

    @Test
    public void query4()
        throws Exception
    {
        // @formatter:off
        // SELECT * FROM schema.function_name(6, 'param2');
        // @formatter:on

        SQLVendor vendor = this.getVendor();
        QueryFactory q = vendor.getQueryFactory();
        LiteralFactory l = vendor.getLiteralFactory();

        this.logQuery( vendor, q.callFunction( "schema", l.func( "function_name", l.n( 6 ), l.s( "param2" ) ) ) );
    }

    @Test
    public void query5()
        throws Exception
    {
        // @formatter:off
        /*
          SELECT *
          FROM table
          WHERE table.value = ?
          ORDER BY 1 ASC
          OFFSET 3 ROWS
          FETCH FIRST 6 ROWS ONLY
        */

        SQLVendor vendor = this.getVendor();

        BooleanFactory b = vendor.getBooleanFactory();
        ColumnsFactory c = vendor.getColumnsFactory();

        QueryExpression query = vendor.getQueryFactory().simpleQueryBuilder()
                                      .selectAllColumns()
                                      .from( vendor.getTableReferenceFactory().tableName( "table" ) )
                                      .where( b.eq( c.colName( "table", "value" ), vendor.getLiteralFactory().param() ) )
                                      .orderByAsc( "1" )
                                      .limit( 6 )
                                      .offset( 3 )
                                      .createExpression();
        // @formatter:on

        this.logQuery( vendor, query );
    }

    @Test
    public void query6()
        throws Exception
    {
        // @formatter:off
        /*
          SELECT *
          FROM table
          WHERE table.value = ?
          ORDER BY 1 ASC
          OFFSET 3 ROWS
        */

        SQLVendor vendor = this.getVendor();

        BooleanFactory b = vendor.getBooleanFactory();
        ColumnsFactory c = vendor.getColumnsFactory();

        QueryExpression query = vendor.getQueryFactory().simpleQueryBuilder()
                                      .selectAllColumns()
                                      .from( vendor.getTableReferenceFactory().tableName( "table" ) )
                                      .where( b.eq( c.colName( "table", "value" ), vendor.getLiteralFactory().param() ) )
                                      .orderByAsc( "1" )
                                      .offset( 3 )
                                      .createExpression();
        // @formatter:on

        this.logQuery( vendor, query );
    }

    @Test
    public void query7()
        throws Exception
    {
        // @formatter:off
        /*
          SELECT *
          FROM table
          WHERE table.value = ?
          ORDER BY 1 ASC
          FETCH FIRST 6 ROWS ONLY
        */

        SQLVendor vendor = this.getVendor();

        BooleanFactory b = vendor.getBooleanFactory();
        ColumnsFactory c = vendor.getColumnsFactory();

        QueryExpression query = vendor.getQueryFactory().simpleQueryBuilder()
                                      .selectAllColumns()
                                      .from( vendor.getTableReferenceFactory().tableName( "table" ) )
                                      .where( b.eq( c.colName( "table", "value" ), vendor.getLiteralFactory().param() ) )
                                      .orderByAsc( "1" )
                                      .limit( 6 )
                                      .createExpression();
        // @formatter:on

        this.logQuery( vendor, query );
    }
}
