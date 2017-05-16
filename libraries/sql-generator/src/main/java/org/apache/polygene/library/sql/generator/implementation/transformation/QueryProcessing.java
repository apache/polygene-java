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
package org.apache.polygene.library.sql.generator.implementation.transformation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.Typeable;
import org.apache.polygene.library.sql.generator.grammar.booleans.BooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.common.NonBooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.common.SQLConstants;
import org.apache.polygene.library.sql.generator.grammar.common.ValueExpression;
import org.apache.polygene.library.sql.generator.grammar.literals.LiteralExpression;
import org.apache.polygene.library.sql.generator.grammar.query.ColumnReferences;
import org.apache.polygene.library.sql.generator.grammar.query.ColumnReferences.ColumnReferenceInfo;
import org.apache.polygene.library.sql.generator.grammar.query.CorrespondingSpec;
import org.apache.polygene.library.sql.generator.grammar.query.FromClause;
import org.apache.polygene.library.sql.generator.grammar.query.GroupByClause;
import org.apache.polygene.library.sql.generator.grammar.query.GroupingElement;
import org.apache.polygene.library.sql.generator.grammar.query.LimitSpecification;
import org.apache.polygene.library.sql.generator.grammar.query.OffsetSpecification;
import org.apache.polygene.library.sql.generator.grammar.query.OrderByClause;
import org.apache.polygene.library.sql.generator.grammar.query.Ordering;
import org.apache.polygene.library.sql.generator.grammar.query.OrdinaryGroupingSet;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpression;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpressionBody;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpressionBodyBinary;
import org.apache.polygene.library.sql.generator.grammar.query.QuerySpecification;
import org.apache.polygene.library.sql.generator.grammar.query.RowDefinition;
import org.apache.polygene.library.sql.generator.grammar.query.RowSubQuery;
import org.apache.polygene.library.sql.generator.grammar.query.RowValueConstructor;
import org.apache.polygene.library.sql.generator.grammar.query.SelectColumnClause;
import org.apache.polygene.library.sql.generator.grammar.query.SetOperation;
import org.apache.polygene.library.sql.generator.grammar.query.SortSpecification;
import org.apache.polygene.library.sql.generator.grammar.query.TableReference;
import org.apache.polygene.library.sql.generator.grammar.query.TableValueConstructor;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.BooleanUtils;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.slf4j.LoggerFactory;

/**
 * @author Stanislav Muhametsin
 */
public class QueryProcessing
{

    public static void processOptionalBooleanExpression( SQLProcessorAggregator processor,
                                                         StringBuilder builder,
                                                         BooleanExpression expression, String prefix, String name )
    {
        if( expression != null && !BooleanUtils.isEmpty( expression ) )
        {
            processOptional( processor, builder, expression, prefix, name );
        }
    }

    public static void processOptional( SQLProcessorAggregator processor, StringBuilder builder,
                                        Typeable<?> element,
                                        String prefix, String name )
    {
        if( element != null )
        {
            builder.append( prefix );
            if( name != null )
            {
                builder.append( name ).append( SQLConstants.TOKEN_SEPARATOR );
            }
            processor.process( element, builder );
        }
    }

    public static class QueryExpressionBinaryProcessor extends
                                                       AbstractProcessor<QueryExpressionBodyBinary>
    {
        private static final Map<SetOperation, String> _defaultSetOperations;

        static
        {
            Map<SetOperation, String> operations = new HashMap<SetOperation, String>();
            operations.put( SetOperation.EXCEPT, "EXCEPT" );
            operations.put( SetOperation.INTERSECT, "INTERSECT" );
            operations.put( SetOperation.UNION, "UNION" );
            _defaultSetOperations = operations;
        }

        private final Map<SetOperation, String> _setOperations;

        public QueryExpressionBinaryProcessor()
        {
            this( _defaultSetOperations );
        }

        public QueryExpressionBinaryProcessor( Map<SetOperation, String> setOperations )
        {
            super( QueryExpressionBodyBinary.class );
            Objects.requireNonNull( setOperations, "set operations" );
            this._setOperations = setOperations;
        }

        @Override
        protected void doProcess( SQLProcessorAggregator processor, QueryExpressionBodyBinary body,
                                  StringBuilder builder )
        {
            Boolean leftIsNotEmpty =
                body.getLeft() != QueryExpressionBody.EmptyQueryExpressionBody.INSTANCE;
            if( leftIsNotEmpty )
            {
                builder.append( SQLConstants.OPEN_PARENTHESIS );
                processor.process( body.getLeft(), builder );
                builder.append( SQLConstants.CLOSE_PARENTHESIS ).append( SQLConstants.NEWLINE );
                this.processSetOperation( body.getSetOperation(), builder );

                builder.append( SQLConstants.TOKEN_SEPARATOR );
                ProcessorUtils.processSetQuantifier( body.getSetQuantifier(), builder );

                CorrespondingSpec correspondingCols = body.getCorrespondingColumns();
                if( correspondingCols != null )
                {
                    builder.append( SQLConstants.TOKEN_SEPARATOR );
                    processor.process( correspondingCols, builder );
                }

                builder.append( SQLConstants.NEWLINE ).append( SQLConstants.OPEN_PARENTHESIS );
            }
            processor.process( body.getRight(), builder );
            if( leftIsNotEmpty )
            {
                builder.append( SQLConstants.CLOSE_PARENTHESIS );
            }
        }

        protected void processSetOperation( SetOperation operation, StringBuilder builder )
        {
            builder.append( this._setOperations.get( operation ) );
        }
    }

    public static class QuerySpecificationProcessor extends AbstractProcessor<QuerySpecification>
    {

        public QuerySpecificationProcessor()
        {
            this( QuerySpecification.class );
        }

        public QuerySpecificationProcessor( Class<? extends QuerySpecification> queryClass )
        {
            super( queryClass );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator processor, QuerySpecification query,
                                  StringBuilder builder )
        {
            builder.append( SQLConstants.SELECT ).append( SQLConstants.TOKEN_SEPARATOR );
            ProcessorUtils.processSetQuantifier( query.getColumns().getSetQuantifier(), builder );
            builder.append( SQLConstants.TOKEN_SEPARATOR );

            processor.process( query.getColumns(), builder );
            processor.process( query.getFrom(), builder );
            QueryProcessing.processOptionalBooleanExpression( processor, builder, query.getWhere(),
                                                              SQLConstants.NEWLINE, SQLConstants.WHERE );
            processor.process( query.getGroupBy(), builder );
            QueryProcessing.processOptionalBooleanExpression( processor, builder,
                                                              query.getHaving(),
                                                              SQLConstants.NEWLINE, SQLConstants.HAVING );
            processor.process( query.getOrderBy(), builder );
            Typeable<?> first = null;
            Typeable<?> second = null;
            if( this.isOffsetBeforeLimit( processor ) )
            {
                first = query.getOffsetSpecification();
                second = query.getLimitSpecification();
            }
            else
            {
                first = query.getLimitSpecification();
                second = query.getOffsetSpecification();
            }

            if( first != null || second != null )
            {
                this.processLimitAndOffset( processor, builder, first, second );
            }

            if( query.getOrderBy() == null
                && ( query.getOffsetSpecification() != null || query.getLimitSpecification() != null ) )
            {
                LoggerFactory.getLogger( this.getClass().getName() ).warn(
                    "Spotted query with " + SQLConstants.OFFSET_PREFIX + " and/or "
                    + SQLConstants.LIMIT_PREFIX
                    + " clause, but without ORDER BY. The result will be unpredictable!"
                    + "\n" + "Query: "
                    + builder.toString() );
            }
        }

        protected boolean isOffsetBeforeLimit( SQLProcessorAggregator processor )
        {
            return true;
        }

        protected void processLimitAndOffset( SQLProcessorAggregator processor,
                                              StringBuilder builder,
                                              Typeable<?> first, Typeable<?> second )
        {
            QueryProcessing.processOptional( processor, builder, first, SQLConstants.NEWLINE, null );
            QueryProcessing
                .processOptional( processor, builder, second, SQLConstants.NEWLINE, null );
        }
    }

    public static class SelectColumnsProcessor extends AbstractProcessor<SelectColumnClause>
    {
        public SelectColumnsProcessor()
        {
            super( SelectColumnClause.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, SelectColumnClause select,
                                  StringBuilder builder )
        {
            if( select instanceof ColumnReferences )
            {
                Iterator<ColumnReferenceInfo> iter =
                    ( (ColumnReferences) select ).getColumns().iterator();
                while( iter.hasNext() )
                {
                    ColumnReferenceInfo info = iter.next();
                    aggregator.process( info.getReference(), builder );
                    String alias = info.getAlias();
                    if( ProcessorUtils.notNullAndNotEmpty( alias ) )
                    {
                        builder.append( SQLConstants.TOKEN_SEPARATOR )
                               .append( SQLConstants.ALIAS_DEFINER )
                               .append( SQLConstants.TOKEN_SEPARATOR ).append( alias );
                    }

                    if( iter.hasNext() )
                    {
                        builder.append( SQLConstants.COMMA ).append( SQLConstants.TOKEN_SEPARATOR );
                    }
                }
            }
            else
            {
                builder.append( SQLConstants.ASTERISK );
            }
        }
    }

    public static class FromProcessor extends AbstractProcessor<FromClause>
    {
        public FromProcessor()
        {
            super( FromClause.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, FromClause from,
                                  StringBuilder builder )
        {
            if( !from.getTableReferences().isEmpty() )
            {
                builder.append( SQLConstants.NEWLINE ).append( SQLConstants.FROM )
                       .append( SQLConstants.TOKEN_SEPARATOR );
                Iterator<TableReference> iter = from.getTableReferences().iterator();
                while( iter.hasNext() )
                {
                    aggregator.process( iter.next().asTypeable(), builder );
                    if( iter.hasNext() )
                    {
                        builder.append( SQLConstants.COMMA ).append( SQLConstants.TOKEN_SEPARATOR );
                    }
                }
            }
        }
    }

    public static class QueryExpressionProcessor extends AbstractProcessor<QueryExpression>
    {
        public QueryExpressionProcessor()
        {
            super( QueryExpression.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator processor, QueryExpression object,
                                  StringBuilder builder )
        {
            processor.process( object.getQueryExpressionBody(), builder );
        }
    }

    public static class CorrespondingSpecProcessor extends AbstractProcessor<CorrespondingSpec>
    {
        public CorrespondingSpecProcessor()
        {
            super( CorrespondingSpec.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator processor, CorrespondingSpec object,
                                  StringBuilder builder )
        {
            builder.append( "CORRESPONDING" );
            if( object.getColumnList() != null )
            {
                builder.append( SQLConstants.TOKEN_SEPARATOR ).append( "BY" )
                       .append( SQLConstants.TOKEN_SEPARATOR );
                processor.process( object.getColumnList(), builder );
            }
        }
    }

    public static class SortSpecificationProcessor extends AbstractProcessor<SortSpecification>
    {
        private static final Map<Ordering, String> _defaultOrderingStrings;

        static
        {
            Map<Ordering, String> map = new HashMap<Ordering, String>();
            map.put( Ordering.ASCENDING, "ASC" );
            map.put( Ordering.DESCENDING, "DESC" );
            _defaultOrderingStrings = map;
        }

        private final Map<Ordering, String> _orderingStrings;

        public SortSpecificationProcessor()
        {
            this( _defaultOrderingStrings );
        }

        public SortSpecificationProcessor( Map<Ordering, String> orderingStrings )
        {
            super( SortSpecification.class );
            Objects.requireNonNull( orderingStrings, "ordering strings" );
            this._orderingStrings = orderingStrings;
        }

        @Override
        protected void doProcess( SQLProcessorAggregator processor, SortSpecification object,
                                  StringBuilder builder )
        {
            processor.process( object.getValueExpression(), builder );
            builder.append( SQLConstants.TOKEN_SEPARATOR ).append(
                this._orderingStrings.get( object.getOrderingSpecification() ) );
        }
    }

    public static class OrdinaryGroupingSetProcessor extends AbstractProcessor<OrdinaryGroupingSet>
    {
        public OrdinaryGroupingSetProcessor()
        {
            super( OrdinaryGroupingSet.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator processor, OrdinaryGroupingSet object,
                                  StringBuilder builder )
        {
            Iterator<NonBooleanExpression> iter = object.getColumns().iterator();
            while( iter.hasNext() )
            {
                processor.process( iter.next(), builder );
                if( iter.hasNext() )
                {
                    builder.append( SQLConstants.COMMA ).append( SQLConstants.TOKEN_SEPARATOR );
                }
            }
        }
    }

    public static class GroupByProcessor extends AbstractProcessor<GroupByClause>
    {
        public GroupByProcessor()
        {
            super( GroupByClause.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, GroupByClause groupBy,
                                  StringBuilder builder )
        {
            if( !groupBy.getGroupingElements().isEmpty() )
            {
                builder.append( SQLConstants.NEWLINE ).append( SQLConstants.GROUP_BY )
                       .append( SQLConstants.TOKEN_SEPARATOR );
                Iterator<GroupingElement> iter = groupBy.getGroupingElements().iterator();
                while( iter.hasNext() )
                {
                    aggregator.process( iter.next(), builder );
                    if( iter.hasNext() )
                    {
                        builder.append( SQLConstants.COMMA ).append( SQLConstants.TOKEN_SEPARATOR );
                    }
                }
            }
        }
    }

    public static class OrderByProcessor extends AbstractProcessor<OrderByClause>
    {
        public OrderByProcessor()
        {
            super( OrderByClause.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, OrderByClause orderBy,
                                  StringBuilder builder )
        {
            if( !orderBy.getOrderingColumns().isEmpty() )
            {
                builder.append( SQLConstants.NEWLINE ).append( SQLConstants.ORDER_BY )
                       .append( SQLConstants.TOKEN_SEPARATOR );
                Iterator<SortSpecification> iter = orderBy.getOrderingColumns().iterator();
                while( iter.hasNext() )
                {
                    aggregator.process( iter.next(), builder );
                    if( iter.hasNext() )
                    {
                        builder.append( SQLConstants.COMMA ).append( SQLConstants.TOKEN_SEPARATOR );
                    }
                }
            }
        }
    }

    public static class TableValueConstructorProcessor extends
                                                       AbstractProcessor<TableValueConstructor>
    {
        public TableValueConstructorProcessor()
        {
            super( TableValueConstructor.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, TableValueConstructor object,
                                  StringBuilder builder )
        {
            builder.append( "VALUES" ).append( SQLConstants.TOKEN_SEPARATOR );
            Iterator<RowValueConstructor> iter = object.getRows().iterator();
            while( iter.hasNext() )
            {
                aggregator.process( iter.next(), builder );
                if( iter.hasNext() )
                {
                    builder.append( SQLConstants.COMMA ).append( SQLConstants.TOKEN_SEPARATOR );
                }
            }
        }
    }

    public static class RowSubQueryProcessor extends AbstractProcessor<RowSubQuery>
    {
        public RowSubQueryProcessor()
        {
            super( RowSubQuery.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, RowSubQuery object,
                                  StringBuilder builder )
        {
            builder.append( SQLConstants.OPEN_PARENTHESIS );
            aggregator.process( object.getQueryExpression(), builder );
            builder.append( SQLConstants.CLOSE_PARENTHESIS );
        }
    }

    public static class RowDefinitionProcessor extends AbstractProcessor<RowDefinition>
    {
        public RowDefinitionProcessor()
        {
            super( RowDefinition.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, RowDefinition object,
                                  StringBuilder builder )
        {
            builder.append( SQLConstants.OPEN_PARENTHESIS );
            Iterator<ValueExpression> vals = object.getRowElements().iterator();
            while( vals.hasNext() )
            {
                aggregator.process( vals.next(), builder );
                if( vals.hasNext() )
                {
                    builder.append( SQLConstants.COMMA ).append( SQLConstants.TOKEN_SEPARATOR );
                }
            }
            builder.append( SQLConstants.CLOSE_PARENTHESIS );
        }
    }

    public static class OffsetSpecificationProcessor extends AbstractProcessor<OffsetSpecification>
    {

        public OffsetSpecificationProcessor()
        {
            super( OffsetSpecification.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, OffsetSpecification object,
                                  StringBuilder builder )
        {
            String prefix = this.getPrefix( aggregator );
            if( prefix != null )
            {
                builder.append( prefix ).append( SQLConstants.TOKEN_SEPARATOR );
            }
            NonBooleanExpression skip = object.getSkip();
            boolean isComplex = !( skip instanceof LiteralExpression );
            if( isComplex )
            {
                builder.append( SQLConstants.OPEN_PARENTHESIS ).append( SQLConstants.NEWLINE );
            }
            aggregator.process( skip, builder );
            if( isComplex )
            {
                builder.append( SQLConstants.CLOSE_PARENTHESIS );
            }
            String postfix = this.getPostfix( aggregator );
            if( postfix != null )
            {
                builder.append( SQLConstants.TOKEN_SEPARATOR ).append( postfix );
            }
        }

        protected String getPrefix( SQLProcessorAggregator processor )
        {
            return SQLConstants.OFFSET_PREFIX;
        }

        protected String getPostfix( SQLProcessorAggregator processor )
        {
            return SQLConstants.OFFSET_POSTFIX;
        }
    }

    public static class LimitSpecificationProcessor extends AbstractProcessor<LimitSpecification>
    {
        public LimitSpecificationProcessor()
        {
            super( LimitSpecification.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, LimitSpecification object,
                                  StringBuilder builder )
        {
            NonBooleanExpression count = this.getRealCount( object.getCount() );
            if( count != null )
            {
                String prefix = this.getPrefix( aggregator );
                if( prefix != null )
                {
                    builder.append( prefix ).append( SQLConstants.TOKEN_SEPARATOR );
                }
                boolean isComplex = !( count instanceof LiteralExpression );
                if( isComplex )
                {
                    builder.append( SQLConstants.OPEN_PARENTHESIS ).append( SQLConstants.NEWLINE );
                }
                aggregator.process( count, builder );
                if( isComplex )
                {
                    builder.append( SQLConstants.CLOSE_PARENTHESIS );
                }
                String postfix = this.getPostfix( aggregator );
                if( postfix != null )
                {
                    builder.append( SQLConstants.TOKEN_SEPARATOR ).append( postfix );
                }
            }
        }

        protected NonBooleanExpression getRealCount( NonBooleanExpression limitCount )
        {
            return limitCount;
        }

        protected String getPrefix( SQLProcessorAggregator processor )
        {
            return SQLConstants.LIMIT_PREFIX;
        }

        protected String getPostfix( SQLProcessorAggregator processor )
        {
            return SQLConstants.LIMIT_POSTFIX;
        }
    }
}
