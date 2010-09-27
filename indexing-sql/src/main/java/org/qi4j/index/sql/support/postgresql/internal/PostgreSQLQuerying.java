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

package org.qi4j.index.sql.support.postgresql.internal;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;

import org.lwdci.api.context.EmptyExecutionArgs;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.StateHolder.StateVisitor;
import org.qi4j.api.query.grammar.AssociationIsNotNullPredicate;
import org.qi4j.api.query.grammar.AssociationIsNullPredicate;
import org.qi4j.api.query.grammar.AssociationNullPredicate;
import org.qi4j.api.query.grammar.AssociationReference;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.ComparisonPredicate;
import org.qi4j.api.query.grammar.Conjunction;
import org.qi4j.api.query.grammar.ContainsAllPredicate;
import org.qi4j.api.query.grammar.ContainsPredicate;
import org.qi4j.api.query.grammar.Disjunction;
import org.qi4j.api.query.grammar.EqualsPredicate;
import org.qi4j.api.query.grammar.GreaterOrEqualPredicate;
import org.qi4j.api.query.grammar.GreaterThanPredicate;
import org.qi4j.api.query.grammar.LessOrEqualPredicate;
import org.qi4j.api.query.grammar.LessThanPredicate;
import org.qi4j.api.query.grammar.ManyAssociationContainsPredicate;
import org.qi4j.api.query.grammar.MatchesPredicate;
import org.qi4j.api.query.grammar.Negation;
import org.qi4j.api.query.grammar.NotEqualsPredicate;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.query.grammar.OrderBy.Order;
import org.qi4j.api.query.grammar.Predicate;
import org.qi4j.api.query.grammar.PropertyIsNotNullPredicate;
import org.qi4j.api.query.grammar.PropertyIsNullPredicate;
import org.qi4j.api.query.grammar.PropertyNullPredicate;
import org.qi4j.api.query.grammar.PropertyReference;
import org.qi4j.api.query.grammar.SingleValueExpression;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.index.sql.support.api.SQLQuerying;
import org.qi4j.index.sql.support.common.EntityTypeInfo;
import org.qi4j.index.sql.support.common.QNameInfo;
import org.qi4j.spi.query.EntityFinderException;
import org.sql.generation.api.grammar.builders.BooleanBuilder;
import org.sql.generation.api.grammar.builders.GroupByBuilder;
import org.sql.generation.api.grammar.builders.InBuilder;
import org.sql.generation.api.grammar.builders.QueryBuilder;
import org.sql.generation.api.grammar.builders.QuerySpecificationBuilder;
import org.sql.generation.api.grammar.builders.TableReferenceBuilder;
import org.sql.generation.api.grammar.builders.pgsql.PgSQLQuerySpecificationBuilder;
import org.sql.generation.api.grammar.common.NonBooleanExpression;
import org.sql.generation.api.grammar.common.SetQuantifier;
import org.sql.generation.api.grammar.factories.BooleanFactory;
import org.sql.generation.api.grammar.factories.ColumnsFactory;
import org.sql.generation.api.grammar.factories.LiteralFactory;
import org.sql.generation.api.grammar.factories.QueryFactory;
import org.sql.generation.api.grammar.factories.TableReferenceFactory;
import org.sql.generation.api.grammar.factories.pgsql.PgSQLQueryFactory;
import org.sql.generation.api.grammar.literals.LiteralExpression;
import org.sql.generation.api.grammar.query.ColumnReference;
import org.sql.generation.api.grammar.query.ColumnReferenceByName;
import org.sql.generation.api.grammar.query.ColumnReferences;
import org.sql.generation.api.grammar.query.ColumnReferences.ColumnReferenceInfo;
import org.sql.generation.api.grammar.query.Ordering;
import org.sql.generation.api.grammar.query.QueryExpression;
import org.sql.generation.api.grammar.query.QuerySpecification;
import org.sql.generation.api.grammar.query.TableReferenceByName;
import org.sql.generation.api.grammar.query.joins.JoinType;
import org.sql.generation.api.transformation.SQLTransformation;
import org.sql.generation.api.transformation.SQLTransformationContextCreationArgs;
import org.sql.generation.api.transformation.SQLTransformationProvider;
import org.sql.generation.api.vendor.PostgreSQLVendor;
import org.sql.generation.api.vendor.SQLVendor;
import org.sql.generation.api.vendor.SQLVendorProvider;

/**
 * 
 * @author Stanislav Muhametsin
 */
public class PostgreSQLQuerying
    implements SQLQuerying, Activatable
{

    @This
    private PostgreSQLDBState _state;

    @This
    private PostgreSQLTypeHelper _typeHelper;

    @Structure
    private UnitOfWorkFactory _uowf;

    @Structure
    private Module _module;

    public static interface SQLBooleanCreator
    {
        public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
            NonBooleanExpression left, NonBooleanExpression right );
    }

    private static Map<Class<? extends Predicate>, SQLBooleanCreator> _sqlOperators;

    private static Map<Class<? extends Predicate>, JoinType> _joinStyles;

    private static Map<Class<? extends Predicate>, JoinType> _negatedJoinStyles;

    private static final String TABLE_NAME_PREFIX = "t";

    private static final Logger _log = Logger.getLogger( PostgreSQLQuerying.class.getName() );

    static
    {
        _sqlOperators = new HashMap<Class<? extends Predicate>, SQLBooleanCreator>();
        _sqlOperators.put( EqualsPredicate.class, new SQLBooleanCreator()
        {

            public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
                NonBooleanExpression left, NonBooleanExpression right )
            {
                return factory.eq( left, right );
            }
        } );
        _sqlOperators.put( GreaterOrEqualPredicate.class, new SQLBooleanCreator()
        {

            public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
                NonBooleanExpression left, NonBooleanExpression right )
            {
                return factory.geq( left, right );
            }
        } );
        _sqlOperators.put( GreaterThanPredicate.class, new SQLBooleanCreator()
        {

            public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
                NonBooleanExpression left, NonBooleanExpression right )
            {
                return factory.gt( left, right );
            }
        } );
        _sqlOperators.put( LessOrEqualPredicate.class, new SQLBooleanCreator()
        {

            public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
                NonBooleanExpression left, NonBooleanExpression right )
            {
                return factory.leq( left, right );
            }
        } );
        _sqlOperators.put( LessThanPredicate.class, new SQLBooleanCreator()
        {

            public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
                NonBooleanExpression left, NonBooleanExpression right )
            {
                return factory.lt( left, right );
            }
        } );
        _sqlOperators.put( NotEqualsPredicate.class, new SQLBooleanCreator()
        {

            public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
                NonBooleanExpression left, NonBooleanExpression right )
            {
                return factory.neq( left, right );
            }
        } );
        _sqlOperators.put( ManyAssociationContainsPredicate.class, new SQLBooleanCreator()
        {

            public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
                NonBooleanExpression left, NonBooleanExpression right )
            {
                return factory.eq( left, right );
            }
        } );
        _sqlOperators.put( MatchesPredicate.class, new SQLBooleanCreator()
        {

            public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
                NonBooleanExpression left, NonBooleanExpression right )
            {
                return factory.matches( left, right );
            }
        } );
        _sqlOperators.put( ContainsPredicate.class, new SQLBooleanCreator()
        {

            public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
                NonBooleanExpression left, NonBooleanExpression right )
            {
                return factory.eq( left, right );
            }
        } );
        _sqlOperators.put( ContainsAllPredicate.class, new SQLBooleanCreator()
        {

            public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
                NonBooleanExpression left, NonBooleanExpression right )
            {
                return factory.eq( left, right );
            }
        } );

        _joinStyles = new HashMap<Class<? extends Predicate>, JoinType>();
        _joinStyles.put( EqualsPredicate.class, JoinType.INNER );
        _joinStyles.put( GreaterOrEqualPredicate.class, JoinType.INNER );
        _joinStyles.put( GreaterThanPredicate.class, JoinType.INNER );
        _joinStyles.put( LessOrEqualPredicate.class, JoinType.INNER );
        _joinStyles.put( LessThanPredicate.class, JoinType.INNER );
        _joinStyles.put( NotEqualsPredicate.class, JoinType.INNER );
        _joinStyles.put( PropertyIsNullPredicate.class, JoinType.LEFT_OUTER );
        _joinStyles.put( PropertyIsNotNullPredicate.class, JoinType.INNER );
        _joinStyles.put( AssociationIsNullPredicate.class, JoinType.LEFT_OUTER );
        _joinStyles.put( AssociationIsNotNullPredicate.class, JoinType.INNER );
        _joinStyles.put( ManyAssociationContainsPredicate.class, JoinType.INNER );
        _joinStyles.put( MatchesPredicate.class, JoinType.INNER );
        _joinStyles.put( ContainsPredicate.class, JoinType.INNER );
        _joinStyles.put( ContainsAllPredicate.class, JoinType.INNER );

        _negatedJoinStyles = new HashMap<Class<? extends Predicate>, JoinType>();
        _negatedJoinStyles.put( EqualsPredicate.class, JoinType.LEFT_OUTER );
        _negatedJoinStyles.put( GreaterOrEqualPredicate.class, JoinType.LEFT_OUTER );
        _negatedJoinStyles.put( GreaterThanPredicate.class, JoinType.LEFT_OUTER );
        _negatedJoinStyles.put( LessOrEqualPredicate.class, JoinType.LEFT_OUTER );
        _negatedJoinStyles.put( LessThanPredicate.class, JoinType.LEFT_OUTER );
        _negatedJoinStyles.put( NotEqualsPredicate.class, JoinType.INNER );
        _negatedJoinStyles.put( PropertyIsNullPredicate.class, JoinType.INNER );
        _negatedJoinStyles.put( PropertyIsNotNullPredicate.class, JoinType.LEFT_OUTER );
        _negatedJoinStyles.put( AssociationIsNullPredicate.class, JoinType.INNER );
        _negatedJoinStyles.put( AssociationIsNotNullPredicate.class, JoinType.LEFT_OUTER );
        _negatedJoinStyles.put( ManyAssociationContainsPredicate.class, JoinType.INNER );
        _negatedJoinStyles.put( MatchesPredicate.class, JoinType.LEFT_OUTER );
        _negatedJoinStyles.put( ContainsPredicate.class, JoinType.LEFT_OUTER );
        _negatedJoinStyles.put( ContainsAllPredicate.class, JoinType.LEFT_OUTER );
    }

    private interface WhereClauseProcessor
    {

        public void processWhereClause( QuerySpecificationBuilder builder, BooleanBuilder afterWhere,
            JoinType joinStyle, Integer firstTableIndex, Integer lastTableIndex );
    }

    private static class ModifiableInt
    {
        private int _int;

        public ModifiableInt( Integer integer )
        {
            this._int = integer;
        }

        public int getInt()
        {
            return this._int;
        }

        public void setInt( int integer )
        {
            this._int = integer;
        }

        @Override
        public String toString()
        {
            return Integer.toString( this._int );
        }
    }

    private static class QNameJoin
    {
        private QualifiedName _sourceQName;

        private QualifiedName _targetQName;

        private Integer _sourceTableIndex;

        private Integer _targetTableIndex;

        public QNameJoin( QualifiedName sourceQName, QualifiedName targetQName, Integer sourceTableIndex,
            Integer targetTableIndex )
        {
            this._sourceQName = sourceQName;
            this._targetQName = targetQName;
            this._sourceTableIndex = sourceTableIndex;
            this._targetTableIndex = targetTableIndex;
        }

        public QualifiedName getSourceQName()
        {
            return this._sourceQName;
        }

        public QualifiedName getTargetQName()
        {
            return this._targetQName;
        }

        public Integer getSourceTableIndex()
        {
            return this._sourceTableIndex;
        }

        public Integer getTargetTableIndex()
        {
            return this._targetTableIndex;
        }

    }

    public void activate()
        throws Exception
    {
        this._state.sqlVendor().set( SQLVendorProvider.createVendor( PostgreSQLVendor.class ) );
    }

    public void passivate()
        throws Exception
    {

    }

    public Integer getResultSetType( Integer firstResult, Integer maxResults )
    {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    public Boolean isFirstResultSettingSupported()
    {
        return true;
    }

    public String constructQuery( Class<?> resultType, //
        BooleanExpression whereClause, //
        OrderBy[] orderBySegments, //
        Integer firstResult, //
        Integer maxResults, //
        List<Object> values, //
        List<Integer> valueSQLTypes, //
        Boolean countOnly //
    )
        throws EntityFinderException
    {
        SQLVendor vendor = this._state.sqlVendor().get();

        QueryFactory q = vendor.getQueryFactory();
        TableReferenceFactory t = vendor.getFromFactory();
        LiteralFactory l = vendor.getLiteralFactory();
        ColumnsFactory c = vendor.getColumnsFactory();

        ColumnReference mainColumn = c.colName( TABLE_NAME_PREFIX + "0", SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME );
        if( countOnly )
        {
            mainColumn = c.colExp( l.func( "COUNT", l.l( mainColumn ) ) );
        }

        QueryBuilder innerBuilder = this.processBooleanExpression( whereClause, false, vendor,
            this.createTypeCondition( resultType, vendor ), values, valueSQLTypes );

        QuerySpecificationBuilder mainQuery = q.querySpecificationBuilder();
        mainQuery.getSelect().addUnnamedColumns( mainColumn );
        mainQuery.getFrom().addTableReferences(
            t.tableBuilder( t.table( q.createQuery( innerBuilder.createExpression() ),
                t.tableAlias( TABLE_NAME_PREFIX + "0" ) ) ) );

        this.processOrderBySegments( orderBySegments, vendor, mainQuery );

        QueryExpression finalMainQuery = this.finalizeQuery( vendor, mainQuery, resultType, whereClause,
            orderBySegments, firstResult, maxResults, values, valueSQLTypes, countOnly );

        SQLTransformation transform = SQLTransformationProvider.getTransformation();
        String result = transform.createContext( new SQLTransformationContextCreationArgs( vendor, finalMainQuery ) )
            .interaction( EmptyExecutionArgs.EMPTY_ARGS );

        _log.info( "SQL query:\n" + result );
        return result;
    }

    protected org.sql.generation.api.grammar.booleans.BooleanExpression createTypeCondition( Class<?> resultType,
        SQLVendor vendor )
    {
        BooleanFactory b = vendor.getBooleanFactory();
        LiteralFactory l = vendor.getLiteralFactory();
        ColumnsFactory c = vendor.getColumnsFactory();

        List<Integer> typeIDs = this.getEntityTypeIDs( resultType );
        InBuilder in = b
            .inBuilder( l.l( c.colName( TABLE_NAME_PREFIX + "0", SQLs.ENTITY_TYPES_TABLE_PK_COLUMN_NAME ) ) );
        for( Integer i : typeIDs )
        {
            in.addValues( l.n( i ) );
        }

        return in.createExpression();
    }

    protected QueryExpression finalizeQuery( //
        SQLVendor sqlVendor, QuerySpecificationBuilder specBuilder, //
        Class<?> resultType, //
        BooleanExpression whereClause, //
        OrderBy[] orderBySegments, //
        Integer firstResult, //
        Integer maxResults, //
        List<Object> values, //
        List<Integer> valueSQLTypes, //
        Boolean countOnly )
    {
        PgSQLQuerySpecificationBuilder builder = (PgSQLQuerySpecificationBuilder) specBuilder;
        PostgreSQLVendor vendor = (PostgreSQLVendor) sqlVendor;
        Boolean needOffset = firstResult != null && firstResult > 0;
        Boolean needLimit = maxResults != null && maxResults > 0;

        PgSQLQueryFactory q = vendor.getQueryFactory();
        LiteralFactory l = vendor.getLiteralFactory();

        if( needOffset )
        {
            builder.offset( q.offset( firstResult ) );
        }
        if( needLimit )
        {
            builder.limit( q.limit( maxResults ) );
        }

        if( builder.getOrderBy().getSortSpecs().isEmpty() && (needOffset || needLimit) )
        {
            // No ORDER BY specified, but we need it anyway since offset or limit was used
            // Solution: sort by the only column that we select, in ascending order.
            builder.getOrderBy().addSortSpecs(
                q.sortSpec( l.l( builder.getSelect().getColumns().iterator().next().getReference() ),
                    Ordering.ASCENDING ) );
        }

        return q.createQuery( builder.createExpression() );
    }

    private QueryBuilder processBooleanExpression( //
        BooleanExpression expression,//
        Boolean negationActive,//
        SQLVendor vendor,//
        org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition, //
        List<Object> values, List<Integer> valueSQLTypes )
    {
        QueryBuilder result = null;
        if( expression != null )
        {
            if( expression instanceof Conjunction )
            {
                Conjunction conjunction = (Conjunction) expression;
                result = this.processBooleanExpression( conjunction.leftSideExpression(), negationActive, vendor,
                    entityTypeCondition, values, valueSQLTypes ).intersect(
                    this.processBooleanExpression( conjunction.rightSideExpression(), negationActive, vendor,
                        entityTypeCondition, values, valueSQLTypes ).createExpression() );
            }
            else if( expression instanceof Disjunction )
            {
                Disjunction disjunction = (Disjunction) expression;
                result = this.processBooleanExpression( disjunction.leftSideExpression(), negationActive, vendor,
                    entityTypeCondition, values, valueSQLTypes ).union(
                    this.processBooleanExpression( disjunction.rightSideExpression(), negationActive, vendor,
                        entityTypeCondition, values, valueSQLTypes ).createExpression() );
            }
            else if( expression instanceof Negation )
            {
                result = this.processBooleanExpression( ((Negation) expression).expression(), !negationActive, vendor,
                    entityTypeCondition, values, valueSQLTypes );
            }
            else if( expression instanceof MatchesPredicate )
            {
                result = this.processMatchesPredicate( (MatchesPredicate) expression, negationActive, vendor,
                    entityTypeCondition, values, valueSQLTypes );
            }
            else if( expression instanceof ComparisonPredicate<?> )
            {
                result = this.processComparisonPredicate( (ComparisonPredicate<?>) expression, negationActive, vendor,
                    entityTypeCondition, values, valueSQLTypes );
            }
            else if( expression instanceof ManyAssociationContainsPredicate<?> )
            {
                result = this.processManyAssociationContainsPredicate(
                    (ManyAssociationContainsPredicate<?>) expression, negationActive, vendor, entityTypeCondition,
                    values, valueSQLTypes );
            }
            else if( expression instanceof PropertyNullPredicate<?> )
            {
                result = this.processPropertyNullPredicate( (PropertyNullPredicate<?>) expression, negationActive,
                    vendor, entityTypeCondition );
            }
            else if( expression instanceof AssociationNullPredicate )
            {
                result = this.processAssociationNullPredicate( (AssociationNullPredicate) expression, negationActive,
                    vendor, entityTypeCondition );
            }
            else if( expression instanceof ContainsPredicate<?, ?> )
            {
                result = this.processContainsPredicate( (ContainsPredicate<?, ?>) expression, negationActive, vendor,
                    entityTypeCondition, values, valueSQLTypes );
            }
            else if( expression instanceof ContainsAllPredicate<?, ?> )
            {
                result = this.processContainsAllPredicate( (ContainsAllPredicate<?, ?>) expression, negationActive,
                    vendor, entityTypeCondition, values, valueSQLTypes );
            }
            else
            {
                throw new UnsupportedOperationException( "Expression " + expression + " is not supported" );
            }
        }
        else
        {
            QueryFactory q = vendor.getQueryFactory();

            result = q.queryBuilder( this.selectAllEntitiesOfCorrectType( vendor, entityTypeCondition )
                .createExpression() );
        }

        return result;
    }

    protected QuerySpecificationBuilder selectAllEntitiesOfCorrectType( SQLVendor vendor,
        org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition )
    {
        TableReferenceFactory t = vendor.getFromFactory();

        String tableAlias = TABLE_NAME_PREFIX + "0";
        QuerySpecificationBuilder query = this.getSelectClauseForPredicate( vendor, tableAlias );
        query.getFrom().addTableReferences(
            t.tableBuilder( t.table( t.tableName( this._state.schemaName().get(), SQLs.ENTITY_TABLE_NAME ),
                t.tableAlias( tableAlias ) ) ) );
        query.getWhere().reset( entityTypeCondition );

        return query;
    }

    private QueryBuilder processMatchesPredicate( final MatchesPredicate predicate, final Boolean negationActive,
        final SQLVendor vendor, org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition,
        final List<Object> values, final List<Integer> valueSQLTypes )
    {
        return this.singleQuery( //
            predicate, //
            predicate.propertyReference(), //
            null, //
            null, //
            negationActive, //
            vendor, //
            entityTypeCondition, //
            new WhereClauseProcessor()
            {

                public void processWhereClause( QuerySpecificationBuilder builder, BooleanBuilder afterWhere,
                    JoinType joinStyle, Integer firstTableIndex, Integer lastTableIndex )
                {
                    LiteralFactory l = vendor.getLiteralFactory();
                    ColumnsFactory c = vendor.getColumnsFactory();

                    builder.getWhere().reset(
                        vendor.getBooleanFactory().matches(
                            l.l( c.colName( TABLE_NAME_PREFIX + lastTableIndex, SQLs.QNAME_TABLE_VALUE_COLUMN_NAME ) ),
                            l.param() ) );

                    values.add( translateJavaRegexpToPGSQLRegexp( ((SingleValueExpression<String>) predicate
                        .valueExpression()).value() ) );
                    valueSQLTypes.add( Types.VARCHAR );
                }
            } //
            );
    }

    private QueryBuilder processComparisonPredicate( final ComparisonPredicate<?> predicate,
        final Boolean negationActive, final SQLVendor vendor,
        org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition, final List<Object> values,
        final List<Integer> valueSQLTypes )
    {
        return this.singleQuery( //
            predicate, //
            predicate.propertyReference(), //
            null, //
            null, //
            negationActive, //
            vendor, //
            entityTypeCondition, //
            new WhereClauseProcessor()
            {

                public void processWhereClause( QuerySpecificationBuilder builder, BooleanBuilder afterWhere,
                    JoinType joinStyle, Integer firstTableIndex, Integer lastTableIndex )
                {
                    QualifiedName qName = QualifiedName.fromClass( predicate.propertyReference()
                        .propertyDeclaringType(), predicate.propertyReference().propertyName() );
                    String columnName = null;
                    if( qName.type().equals( Identity.class.getName() ) )
                    {
                        columnName = SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME;
                    }
                    else
                    {
                        columnName = SQLs.QNAME_TABLE_VALUE_COLUMN_NAME;
                    }
                    Object value = ((SingleValueExpression<?>) predicate.valueExpression()).value();
                    modifyFromClauseAndWhereClauseToGetValue( qName, value, predicate, negationActive, lastTableIndex,
                        new ModifiableInt( lastTableIndex ), columnName,
                        SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME, vendor, builder.getWhere(), afterWhere,
                        builder.getFrom().getTableReferences().iterator().next(), builder.getGroupBy(),
                        builder.getHaving(), new ArrayList<QNameJoin>(), values, valueSQLTypes );
                }

            } //
            );
    }

    private QueryBuilder processManyAssociationContainsPredicate( final ManyAssociationContainsPredicate<?> predicate,
        final Boolean negationActive, final SQLVendor vendor,
        org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition, final List<Object> values,
        final List<Integer> valueSQLTypes )
    {
        return this.singleQuery( //
            predicate, //
            null, //
            predicate.associationReference(), //
            true, //
            negationActive, //
            vendor, //
            entityTypeCondition, //
            new WhereClauseProcessor()
            {

                public void processWhereClause( QuerySpecificationBuilder builder, BooleanBuilder afterWhere,
                    JoinType joinStyle, Integer firstTableIndex, Integer lastTableIndex )
                {
                    LiteralFactory l = vendor.getLiteralFactory();
                    ColumnsFactory c = vendor.getColumnsFactory();
                    BooleanFactory b = vendor.getBooleanFactory();

                    builder.getWhere().reset(
                        getOperator( predicate )
                            .getExpression(
                                b,
                                l.l( c.colName( TABLE_NAME_PREFIX + lastTableIndex,
                                    SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME ) ), l.param() ) );

                    Object value = ((SingleValueExpression<?>) predicate.valueExpression()).value();
                    // TODO Is it really certain that this value is always instance of EntityComposite?
                    if( value instanceof EntityComposite )
                    {
                        value = _uowf.currentUnitOfWork().get( (EntityComposite) value ).identity().get();
                    }
                    else
                    {
                        value = value.toString();
                    }
                    values.add( value );
                    valueSQLTypes.add( Types.VARCHAR );
                }
            } //
            );
    }

    private QueryBuilder processPropertyNullPredicate( final PropertyNullPredicate<?> predicate,
        final Boolean negationActive, final SQLVendor vendor,
        org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition )
    {
        return this.singleQuery( //
            predicate, //
            predicate.propertyReference(), //
            null, //
            null, //
            negationActive, //
            vendor, //
            entityTypeCondition, //
            new WhereClauseProcessor()
            {

                public void processWhereClause( QuerySpecificationBuilder builder, BooleanBuilder afterWhere,
                    JoinType joinStyle, Integer firstTableIndex, Integer lastTableIndex )
                {
                    if( (predicate instanceof PropertyIsNullPredicate<?> && !negationActive)
                        || (predicate instanceof PropertyIsNotNullPredicate<?> && negationActive) )
                    {
                        LiteralFactory l = vendor.getLiteralFactory();
                        ColumnsFactory c = vendor.getColumnsFactory();
                        BooleanFactory b = vendor.getBooleanFactory();

                        QNameInfo info = _state
                            .qNameInfos()
                            .get()
                            .get(
                                QualifiedName.fromClass( predicate.propertyReference().propertyDeclaringType(),
                                    predicate.propertyReference().propertyName() ) );
                        String colName = null;
                        if( info.getCollectionDepth() > 0 )
                        {
                            colName = SQLs.ALL_QNAMES_TABLE_PK_COLUMN_NAME;
                        }
                        else
                        {
                            colName = SQLs.QNAME_TABLE_VALUE_COLUMN_NAME;
                        }
                        // Last table column might be null because of left joins
                        builder.getWhere().reset(
                            b.isNull( l.l( c.colName( TABLE_NAME_PREFIX + lastTableIndex, colName ) ) ) );
                    }
                }

            } //
            );
    }

    private QueryBuilder processAssociationNullPredicate( final AssociationNullPredicate predicate,
        final Boolean negationActive, final SQLVendor vendor,
        org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition )
    {
        return this.singleQuery( //
            predicate, //
            null, //
            predicate.associationReference(), //
            false, //
            negationActive, //
            vendor, //
            entityTypeCondition, //
            new WhereClauseProcessor()
            {

                public void processWhereClause( QuerySpecificationBuilder builder, BooleanBuilder afterWhere,
                    JoinType joinStyle, Integer firstTableIndex, Integer lastTableIndex )
                {
                    if( (predicate instanceof AssociationIsNullPredicate && !negationActive)
                        || (predicate instanceof AssociationIsNotNullPredicate && negationActive) )
                    {
                        LiteralFactory l = vendor.getLiteralFactory();
                        ColumnsFactory c = vendor.getColumnsFactory();
                        BooleanFactory b = vendor.getBooleanFactory();

                        // Last table column might be null because of left joins
                        builder.getWhere().reset(
                            b.isNull( l.l( c.colName( TABLE_NAME_PREFIX + lastTableIndex,
                                SQLs.QNAME_TABLE_VALUE_COLUMN_NAME ) ) ) );
                    }
                }

            } //
            );
    }

    private QueryBuilder processContainsPredicate( final ContainsPredicate<?, ? extends Collection<?>> predicate,
        final Boolean negationActive, final SQLVendor vendor,
        org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition, final List<Object> values,
        final List<Integer> valueSQLTypes )
    {
        // Path: Top.* (star without braces), value = value
        // ASSUMING value is NOT collection (ie, find all entities, which collection property has value x as leaf item,
        // no matter collection depth)
        QuerySpecification contains = this.constructQueryForPredicate( //
            predicate, //
            predicate.propertyReference(), //
            null, //
            null, //
            false, //
            vendor, //
            entityTypeCondition, //
            new WhereClauseProcessor()
            {
                public void processWhereClause( QuerySpecificationBuilder builder, BooleanBuilder afterWhere,
                    JoinType joinStyle, Integer firstTableIndex, Integer lastTableIndex )
                {
                    BooleanFactory b = vendor.getBooleanFactory();
                    LiteralFactory l = vendor.getLiteralFactory();
                    ColumnsFactory c = vendor.getColumnsFactory();

                    builder.getWhere().reset(
                        b.matches( l.l( c.colName( TABLE_NAME_PREFIX + lastTableIndex,
                            SQLs.QNAME_TABLE_COLLECTION_PATH_COLUMN_NAME ) ), l
                            .l( SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME + ".*{1,}" ) ) );
                    // where.append( TABLE_NAME_PREFIX + lastTableIndex + "."
                    // + SQLs.QNAME_TABLE_COLLECTION_PATH_COLUMN_NAME + " ~ '"
                    // + SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME + ".*{1,}' AND (" );
                    Object value = ((SingleValueExpression<?>) predicate.valueExpression()).value();
                    if( value instanceof Collection<?> )
                    {
                        throw new IllegalArgumentException(
                            "ContainsPredicate may have only either primitive or value composite as value." );
                    }
                    BooleanBuilder condition = b.booleanBuilder();
                    modifyFromClauseAndWhereClauseToGetValue( QualifiedName.fromClass( predicate.propertyReference()
                        .propertyDeclaringType(), predicate.propertyReference().propertyName() ), value, predicate,
                        false, lastTableIndex, new ModifiableInt( lastTableIndex ), SQLs.QNAME_TABLE_VALUE_COLUMN_NAME,
                        SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME, vendor, condition, afterWhere, builder
                            .getFrom().getTableReferences().iterator().next(), builder.getGroupBy(), builder
                            .getHaving(), new ArrayList<QNameJoin>(), values, valueSQLTypes );
                    builder.getWhere().and( condition.createExpression() );
                    // where.append( ")" );
                }
            } //
            );

        return this.finalizeContainsQuery( vendor, contains, entityTypeCondition, negationActive );
    }

    protected QueryBuilder finalizeContainsQuery( SQLVendor vendor, QuerySpecification contains,
        org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition, Boolean negationActive )
    {
        QueryFactory q = vendor.getQueryFactory();
        QueryBuilder result = null;

        if( negationActive )
        {
            result = q.queryBuilder(
                this.selectAllEntitiesOfCorrectType( vendor, entityTypeCondition ).createExpression() ).except(
                contains );
        }
        else
        {
            result = q.queryBuilder( contains );
        }

        return result;
    }

    private QueryBuilder processContainsAllPredicate( final ContainsAllPredicate<?, ? extends Collection<?>> predicate,
        final Boolean negationActive, final SQLVendor vendor,
        org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition, final List<Object> values,
        final List<Integer> valueSQLTypes )
    {
        // has all leaf items in specified collection

        QuerySpecification contains = this.constructQueryForPredicate( //
            predicate, //
            predicate.propertyReference(), //
            null, //
            null, //
            false, //
            vendor, //
            entityTypeCondition, //
            new WhereClauseProcessor()
            {

                public void processWhereClause( QuerySpecificationBuilder builder, BooleanBuilder afterWhere,
                    JoinType joinStyle, Integer firstTableIndex, Integer lastTableIndex )
                {
                    BooleanFactory b = vendor.getBooleanFactory();
                    LiteralFactory l = vendor.getLiteralFactory();
                    ColumnsFactory c = vendor.getColumnsFactory();

                    Collection<?> collection = (Collection<?>) ((SingleValueExpression<?>) predicate.valueExpression())
                        .value();
                    List<QNameJoin> joins = new ArrayList<QNameJoin>();
                    for( Object value : collection )
                    {
                        if( value instanceof Collection<?> )
                        {
                            throw new IllegalArgumentException(
                                "ContainsAllPredicate may not have nested collections as value." );
                        }
                        // if( where.length() > 0 )
                        // {
                        // where.append( " OR " );
                        // }
                        // where.append( "(" + TABLE_NAME_PREFIX + lastTableIndex + "."
                        // + SQLs.QNAME_TABLE_COLLECTION_PATH_COLUMN_NAME + " ~ '"
                        // + SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME + ".*{1,}' AND (" );
                        BooleanBuilder conditionForItem = b.booleanBuilder( b.matches( l.l( c.colName(
                            TABLE_NAME_PREFIX + lastTableIndex, SQLs.QNAME_TABLE_COLLECTION_PATH_COLUMN_NAME ) ), l
                            .l( SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME + ".*{1,}" ) ) );
                        modifyFromClauseAndWhereClauseToGetValue(
                            QualifiedName.fromClass( predicate.propertyReference().propertyDeclaringType(), predicate
                                .propertyReference().propertyName() ), value, predicate, false, lastTableIndex,
                            new ModifiableInt( lastTableIndex ), SQLs.QNAME_TABLE_VALUE_COLUMN_NAME,
                            SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME, vendor, conditionForItem, afterWhere,
                            builder.getFrom().getTableReferences().iterator().next(), builder.getGroupBy(), builder
                                .getHaving(), joins, values, valueSQLTypes );
                        builder.getWhere().or( conditionForItem.createExpression() );
                        // where.append( conditionForItem );
                        // where.append( " ))" + "\n" );
                    }

                    // if( having.length() > 0 )
                    // {
                    // having.append( " AND " );
                    // }
                    builder.getHaving().and(
                        b.geq(
                            l.func( "COUNT", l.l( c.colName( TABLE_NAME_PREFIX + lastTableIndex,
                                SQLs.QNAME_TABLE_VALUE_COLUMN_NAME ) ) ), l.n( collection.size() ) ) );
                    // having.append( "COUNT(" + TABLE_NAME_PREFIX + lastTableIndex + "."
                    // + SQLs.QNAME_TABLE_VALUE_COLUMN_NAME + ") >= " + collection.size() );
                }

            } //
            );

        return this.finalizeContainsQuery( vendor, contains, entityTypeCondition, negationActive );
    }

    private QueryBuilder singleQuery( //
        Predicate predicate, //
        PropertyReference<?> propRef, //
        AssociationReference assoRef, //
        Boolean includeLastAssoPathTable, //
        Boolean negationActive, //
        SQLVendor vendor, //
        org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition, //
        WhereClauseProcessor whereClauseGenerator//
    )
    {
        return vendor.getQueryFactory().queryBuilder(
            this.constructQueryForPredicate( predicate, propRef, assoRef, includeLastAssoPathTable, negationActive,
                vendor, entityTypeCondition, whereClauseGenerator ) );
    }

    private QuerySpecification constructQueryForPredicate( //
        Predicate predicate, //
        PropertyReference<?> propRef, //
        AssociationReference assoRef, //
        Boolean includeLastAssoPathTable, //
        Boolean negationActive, //
        SQLVendor vendor, //
        org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition, //
        WhereClauseProcessor whereClauseGenerator//
    )
    {
        Integer startingIndex = 0;
        TableReferenceFactory t = vendor.getFromFactory();

        QuerySpecificationBuilder builder = this
            .getSelectClauseForPredicate( vendor, TABLE_NAME_PREFIX + startingIndex );
        TableReferenceBuilder from = t.tableBuilder( t.table(
            t.tableName( this._state.schemaName().get(), SQLs.ENTITY_TABLE_NAME ),
            t.tableAlias( TABLE_NAME_PREFIX + startingIndex ) ) );

        Integer lastTableIndex = null;
        JoinType joinStyle = this.getTableJoinStyle( predicate, negationActive );
        if( propRef == null )
        {
            lastTableIndex = this.traverseAssociationPath( assoRef, startingIndex, vendor, from, joinStyle,
                includeLastAssoPathTable );
        }
        else if( assoRef == null )
        {
            lastTableIndex = this.traversePropertyPath( propRef, startingIndex, vendor, from, joinStyle );
        }
        else
        {
            throw new InternalError(
                "Can not have both property reference and association reference (non-)nulls [propRef=" + propRef
                    + ", assoRef=" + assoRef + ", predicate=" + predicate + "]." );
        }

        builder.getFrom().addTableReferences( from );

        BooleanBuilder afterWhere = vendor.getBooleanFactory().booleanBuilder();
        whereClauseGenerator.processWhereClause( builder, afterWhere, joinStyle, startingIndex, lastTableIndex );

        BooleanBuilder where = builder.getWhere();
        if( negationActive )
        {
            where.not();
        }
        where.and( afterWhere.createExpression() );

        where.and( entityTypeCondition );

        if( builder.getHaving().createExpression() != org.sql.generation.api.grammar.booleans.Predicate.EmptyPredicate.INSTANCE )
        {
            // Having was added, and it most likely doesn't have selected columns
            QueryFactory q = vendor.getQueryFactory();
            LiteralFactory l = vendor.getLiteralFactory();

            Iterator<ColumnReferenceInfo> iter = builder.getSelect().getColumns().iterator();
            ColumnReference first = iter.next().getReference();
            ColumnReference second = iter.next().getReference();

            builder.getGroupBy().addGroupingElements( q.groupingElement( l.l( first ) ),
                q.groupingElement( l.l( second ) ) );
        }

        return builder.createExpression();
    }

    private SQLBooleanCreator getOperator( Predicate predicate )
    {
        return this.findFromLookupTables( _sqlOperators, null, predicate, false );
    }

    private JoinType getTableJoinStyle( Predicate predicate, Boolean negationActive )
    {
        return this.findFromLookupTables( _joinStyles, _negatedJoinStyles, predicate, negationActive );
    }

    private <ReturnType> ReturnType findFromLookupTables( Map<Class<? extends Predicate>, ReturnType> normal,
        Map<Class<? extends Predicate>, ReturnType> negated, Predicate predicate, Boolean negationActive )
    {
        Class<? extends Predicate> predicateClass = predicate.getClass();
        ReturnType result = null;
        Set<Map.Entry<Class<? extends Predicate>, ReturnType>> entries = negationActive ? negated.entrySet() : normal
            .entrySet();
        for( Map.Entry<Class<? extends Predicate>, ReturnType> entry : entries )
        {
            if( entry.getKey().isAssignableFrom( predicateClass ) )
            {
                result = entry.getValue();
                break;
            }
        }

        if( result == null )
        {
            throw new UnsupportedOperationException( "Predicate [" + predicateClass.getName() + "] is not supported" );
        }

        return result;
    }

    private QuerySpecificationBuilder getSelectClauseForPredicate( SQLVendor vendor, String tableAlias )
    {
        QueryFactory q = vendor.getQueryFactory();
        ColumnsFactory c = vendor.getColumnsFactory();
        QuerySpecificationBuilder result = q.querySpecificationBuilder();
        result
            .getSelect()
            .setSetQuantifier( SetQuantifier.DISTINCT )
            .addUnnamedColumns( c.colName( tableAlias, SQLs.ENTITY_TABLE_PK_COLUMN_NAME ),
                c.colName( tableAlias, SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME ) );

        return result;
    }

    private String translateJavaRegexpToPGSQLRegexp( String javaRegexp )
    {
        // TODO
        // Yo dawg, I heard you like regular expressions, so we made a regexp about your regexp so you can match while
        // you match!
        // Meaning, probably best way to translate java regexp into pg-sql regexp is by... regexp.
        return javaRegexp;
    }

    private void processOrderBySegments( OrderBy[] orderBy, SQLVendor vendor, QuerySpecificationBuilder builder )
    {
        if( orderBy != null )
        {
            QNameInfo[] qNames = new QNameInfo[orderBy.length];

            QueryFactory q = vendor.getQueryFactory();
            LiteralFactory l = vendor.getLiteralFactory();
            ColumnsFactory c = vendor.getColumnsFactory();

            Integer tableIndex = 0;
            for( Integer idx = 0; idx < orderBy.length; ++idx )
            {
                if( orderBy[idx] != null )
                {
                    PropertyReference<?> ref = orderBy[idx].propertyReference();
                    QualifiedName qName = QualifiedName.fromClass( ref.propertyDeclaringType(), ref.propertyName() );
                    QNameInfo info = this._state.qNameInfos().get().get( qName );
                    qNames[idx] = info;
                    if( info == null )
                    {
                        throw new InternalError( "No qName info found for qName [" + qName + "]." );
                    }
                    tableIndex = this.traversePropertyPath( ref, tableIndex, vendor, builder.getFrom()
                        .getTableReferences().iterator().next(), JoinType.LEFT_OUTER );
                    Class<?> declaringType = ref.propertyDeclaringType();
                    String colName = null;
                    Integer tableIdx = null;
                    if( Identity.class.equals( declaringType ) )
                    {
                        colName = SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME;
                        tableIdx = tableIndex - 1;
                    }
                    else
                    {
                        colName = SQLs.QNAME_TABLE_VALUE_COLUMN_NAME;
                        tableIdx = tableIndex;
                    }
                    Ordering ordering = Ordering.ASCENDING;
                    if( orderBy[idx].order() == Order.DESCENDING )
                    {
                        ordering = Ordering.DESCENDING;
                    }
                    builder.getOrderBy().addSortSpecs(
                        q.sortSpec( l.l( c.colName( TABLE_NAME_PREFIX + tableIdx, colName ) ), ordering ) );
                }
            }
        }

    }

    private Integer traversePropertyPath( PropertyReference<?> reference, Integer index, SQLVendor vendor,
        TableReferenceBuilder builder, JoinType joinStyle )
    {

        Stack<QualifiedName> qNameStack = new Stack<QualifiedName>();
        Stack<PropertyReference<?>> refStack = new Stack<PropertyReference<?>>();

        while( reference != null )
        {
            qNameStack.add( QualifiedName.fromClass( reference.propertyDeclaringType(), reference.propertyName() ) );
            refStack.add( reference );
            if( reference.traversedProperty() == null && reference.traversedAssociation() != null )
            {
                index = this.traverseAssociationPath( reference.traversedAssociation(), index, vendor, builder,
                    joinStyle, true );
            }

            reference = reference.traversedProperty();
        }

        PropertyReference<?> prevRef = null;
        String schemaName = this._state.schemaName().get();
        TableReferenceFactory t = vendor.getFromFactory();
        BooleanFactory b = vendor.getBooleanFactory();
        LiteralFactory l = vendor.getLiteralFactory();
        ColumnsFactory c = vendor.getColumnsFactory();

        while( !qNameStack.isEmpty() )
        {
            QualifiedName qName = qNameStack.pop();
            PropertyReference<?> ref = refStack.pop();
            if( !qName.type().equals( Identity.class.getName() ) )
            {
                QNameInfo info = this._state.qNameInfos().get().get( qName );
                if( info == null )
                {
                    throw new InternalError( "No qName info found for qName [" + qName + "]." );
                }

                String prevTableAlias = TABLE_NAME_PREFIX + index;
                String nextTableAlias = TABLE_NAME_PREFIX + (index + 1);
                TableReferenceByName nextTable = t.table( t.tableName( schemaName, info.getTableName() ),
                    t.tableAlias( nextTableAlias ) );
                // @formatter:off
                if( prevRef == null )
                {
                    builder.addQualifiedJoin(
                        joinStyle,
                        nextTable,
                        t.jc(
                            b.booleanBuilder(
                                b.eq(
                                    l.l( c.colName( prevTableAlias, SQLs.ENTITY_TABLE_PK_COLUMN_NAME ) ),
                                    l.l( c.colName( nextTableAlias, SQLs.ENTITY_TABLE_PK_COLUMN_NAME ) )
                                    )
                                )
                            .and(
                                    b.isNull( l.l( c.colName( nextTableAlias, SQLs.QNAME_TABLE_PARENT_QNAME_COLUMN_NAME ) ) )
                                )
                            .createExpression()
                            )
                        );
                }
                else
                {
                    builder.addQualifiedJoin(
                        joinStyle,
                        nextTable,
                        t.jc(
                            b.booleanBuilder(
                                b.eq(
                                    l.l( c.colName( prevTableAlias, SQLs.ALL_QNAMES_TABLE_PK_COLUMN_NAME ) ),
                                    l.l( c.colName( nextTableAlias, SQLs.QNAME_TABLE_PARENT_QNAME_COLUMN_NAME ) ) )
                                )
                            .and(
                                b.eq(
                                    l.l( c.colName( prevTableAlias, SQLs.ENTITY_TABLE_PK_COLUMN_NAME ) ),
                                    l.l( c.colName( nextTableAlias, SQLs.ENTITY_TABLE_PK_COLUMN_NAME ) )
                                    )
                                )
                            .createExpression()
                            )
                        );
                }
                // @formatter:on
                ++index;
                prevRef = ref;
            }
        }

        return index;
    }

    private Integer traverseAssociationPath( AssociationReference reference, Integer index, SQLVendor vendor,
        TableReferenceBuilder builder, JoinType joinStyle, Boolean includeLastTable )
    {
        Stack<QualifiedName> qNameStack = new Stack<QualifiedName>();
        TableReferenceFactory t = vendor.getFromFactory();
        BooleanFactory b = vendor.getBooleanFactory();
        LiteralFactory l = vendor.getLiteralFactory();
        ColumnsFactory c = vendor.getColumnsFactory();
        String schemaName = this._state.schemaName().get();

        while( reference != null )
        {
            qNameStack
                .add( QualifiedName.fromClass( reference.associationDeclaringType(), reference.associationName() ) );
            reference = reference.traversedAssociation();
        }
        while( !qNameStack.isEmpty() )
        {
            QualifiedName qName = qNameStack.pop();
            QNameInfo info = this._state.qNameInfos().get().get( qName );
            if( info == null )
            {
                throw new InternalError( "No qName info found for qName [" + qName + "]." );
            }
            // @formatter:off
            builder.addQualifiedJoin(
                joinStyle,
                t.table( t.tableName( schemaName, info.getTableName() ), t.tableAlias( TABLE_NAME_PREFIX
                    + (index + 1) ) ),
                t.jc(
                    b.eq(
                        l.l( c.colName( TABLE_NAME_PREFIX + index, SQLs.ENTITY_TABLE_PK_COLUMN_NAME ) ),
                        l.l( c.colName( TABLE_NAME_PREFIX + (index + 1), SQLs.ENTITY_TABLE_PK_COLUMN_NAME ) ) )
                    ) );
            ++index;
            if( includeLastTable || !qNameStack.isEmpty() )
            {
                builder.addQualifiedJoin(
                    joinStyle,
                    t.table( t.tableName( schemaName, SQLs.ENTITY_TABLE_NAME ), t.tableAlias( TABLE_NAME_PREFIX + (index + 1) ) ),
                    t.jc(
                        b.eq(
                            l.l( c.colName( TABLE_NAME_PREFIX + index, SQLs.QNAME_TABLE_VALUE_COLUMN_NAME ) ),
                            l.l( c.colName( TABLE_NAME_PREFIX + (index + 1), SQLs.ENTITY_TABLE_PK_COLUMN_NAME ) )
                            )
                        )
                    );
                ++index;
            }
            // @formatter:on
        }

        return index;
    }

    private List<Integer> getEntityTypeIDs( Class<?> entityType )
    {
        List<Integer> result = new ArrayList<Integer>();
        for( Map.Entry<String, EntityTypeInfo> entry : this._state.entityTypeInfos().get().entrySet() )
        {
            if( entityType.isAssignableFrom( entry.getValue().getEntityDescriptor().type() ) )
            {
                result.add( entry.getValue().getEntityTypePK() );
            }
        }

        return result;
    }

    // TODO currently tableJoinStyle is not used
    private Integer modifyFromClauseAndWhereClauseToGetValue( final QualifiedName qName, Object value,
        final Predicate predicate, final Boolean negationActive, final Integer currentTableIndex,
        final ModifiableInt maxTableIndex, final String columnName, final String collectionPath,
        final SQLVendor vendor, final BooleanBuilder whereClause, final BooleanBuilder afterWhere,
        final TableReferenceBuilder fromClause, final GroupByBuilder groupBy, final BooleanBuilder having,
        final List<QNameJoin> qNameJoins, final List<Object> values, final List<Integer> valueSQLTypes )
    {
        final String schemaName = this._state.schemaName().get();
        Integer result = 1;

        final BooleanFactory b = vendor.getBooleanFactory();
        final LiteralFactory l = vendor.getLiteralFactory();
        final ColumnsFactory c = vendor.getColumnsFactory();
        final QueryFactory q = vendor.getQueryFactory();
        final TableReferenceFactory t = vendor.getFromFactory();

        if( value instanceof Collection<?> )
        {
            // Collection
            Integer collectionIndex = 0;
            Boolean collectionIsSet = value instanceof Set<?>;
            Boolean topLevel = collectionPath.equals( SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME );
            String collTable = TABLE_NAME_PREFIX + currentTableIndex;
            String collCol = SQLs.QNAME_TABLE_COLLECTION_PATH_COLUMN_NAME;
            LiteralExpression collColExp = l.l( c.colName( collTable, collCol ) );

            BooleanBuilder collectionCondition = b.booleanBuilder();

            if( topLevel )
            {
                // if( whereClause.length() > 0 )
                // {
                // whereClause.append( "AND" + " " );
                // }
                // whereClause.append( "(" );
                if( negationActive )
                {

                    afterWhere.and( b
                        .booleanBuilder( b.neq( collColExp, l.l( SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME ) ) )
                        .or( b.isNull( collColExp ) ).createExpression() );
                    // afterWhere.append( " AND (" + collCol + " <> '" + SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME
                    // + "' OR " + collCol + " IS NULL)" + "\n" );
                }
            }

            Integer totalItemsProcessed = 0;
            for( Object item : (Collection<?>) value )
            {
                String path = collectionPath + SQLs.QNAME_TABLE_COLLECTION_PATH_SEPARATOR
                    + (collectionIsSet ? "*{1,}" : collectionIndex);
                Boolean isCollection = (item instanceof Collection<?>);
                BooleanBuilder newWhere = b.booleanBuilder();
                if( !isCollection )
                {
                    newWhere.reset( b.matches( collColExp, l.l( path ) ) );
                    // whereClause.append( "(" + collCol + " ~ '" + path + "'" );
                }
                totalItemsProcessed = totalItemsProcessed
                    + modifyFromClauseAndWhereClauseToGetValue( qName, item, predicate, negationActive,
                        currentTableIndex, maxTableIndex, columnName, path, vendor, newWhere, afterWhere, fromClause,
                        groupBy, having, qNameJoins, values, valueSQLTypes );
                // if( !isCollection )
                // {
                // whereClause.or
                // }
                ++collectionIndex;
                // if( collectionIndex < ((Collection<?>) value).size() )
                // {
                // whereClause.append( " OR " );
                collectionCondition.or( newWhere.createExpression() );
                // }
            }
            result = totalItemsProcessed;

            if( topLevel )
            {
                if( totalItemsProcessed == 0 )
                {
                    collectionCondition.and( b.isNotNull( collColExp ) ).and(
                        b.eq( collColExp, l.d( SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME ) ) );
                    // whereClause.append( collCol + "IS NOT NULL AND " + collCol + " = "
                    // + SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME );
                }
                // whereClause.append( ")" + "\n" );
                else if( !negationActive )
                {
                    // if( groupBy.length() > 0 )
                    // {
                    // groupBy.append( ", " );
                    // }
                    // if( having.length() > 0 )
                    // {
                    // having.append( " AND " );
                    // }
                    groupBy.addGroupingElements( q.groupingElement( l.l( c.colName( TABLE_NAME_PREFIX
                        + currentTableIndex, SQLs.ENTITY_TABLE_PK_COLUMN_NAME ) ) ) );
                    having.and( b.eq(
                        l.func( "COUNT", l.l( c.colName( TABLE_NAME_PREFIX + currentTableIndex,
                            SQLs.QNAME_TABLE_VALUE_COLUMN_NAME ) ) ), l.n( totalItemsProcessed ) ) );
                    // groupBy.append( TABLE_NAME_PREFIX + currentTableIndex + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME );
                    // having.append( "COUNT(" + TABLE_NAME_PREFIX + currentTableIndex + "."
                    // + SQLs.QNAME_TABLE_VALUE_COLUMN_NAME + ") = " + totalItemsProcessed );
                }
            }

            whereClause.and( collectionCondition.createExpression() );

        }
        else if( value instanceof ValueComposite )
        {
            // Visit all properties with recursion and make joins as necessary
            ((ValueComposite) value).state().visitProperties( new StateVisitor()
            {




                public void visitProperty( QualifiedName name, Object propertyValue )
                {

                    Boolean qNameJoinDone = false;
                    Integer sourceIndex = maxTableIndex.getInt();
                    Integer targetIndex = sourceIndex + 1;
                    for( QNameJoin join : qNameJoins )
                    {
                        if( join.getSourceQName().equals( qName ) )
                        {
                            sourceIndex = join.getSourceTableIndex();
                            if( join.getTargetQName().equals( name ) )
                            {
                                // This join has already been done once
                                qNameJoinDone = true;
                                targetIndex = join.getTargetTableIndex();
                                break;
                            }
                        }
                    }

                    if( !qNameJoinDone )
                    {
                        // @formatter:off
                        QNameInfo info = _state.qNameInfos().get().get( name );
                        String prevTableName = TABLE_NAME_PREFIX + sourceIndex;
                        String nextTableName = TABLE_NAME_PREFIX + targetIndex;
                        fromClause.addQualifiedJoin(
                            JoinType.LEFT_OUTER,
                            t.table( t.tableName( schemaName, info.getTableName() ), t.tableAlias( TABLE_NAME_PREFIX + targetIndex ) ),
                            t.jc(
                                b.booleanBuilder(b.eq(
                                    l.l( c.colName( prevTableName, SQLs.ALL_QNAMES_TABLE_PK_COLUMN_NAME ) ),
                                    l.l( c.colName( nextTableName, SQLs.QNAME_TABLE_PARENT_QNAME_COLUMN_NAME ) )
                                    ) )
                                .and(b.eq(
                                    l.l( c.colName( prevTableName, SQLs.ENTITY_TABLE_PK_COLUMN_NAME ) ),
                                    l.l( c.colName( nextTableName, SQLs.ENTITY_TABLE_PK_COLUMN_NAME ) )
                                    ) ).createExpression()
                                )
                            );
                        // @formatter:on
                        // fromClause.append( "LEFT JOIN" + " " + schemaName + "." + info.getTableName() + " "
                        // + TABLE_NAME_PREFIX + targetIndex + /**/
                        // " ON (" + TABLE_NAME_PREFIX + sourceIndex + "." + SQLs.ALL_QNAMES_TABLE_PK_COLUMN_NAME
                        // + " = " + TABLE_NAME_PREFIX + targetIndex + "." + SQLs.QNAME_TABLE_PARENT_QNAME_COLUMN_NAME
                        // + " AND " + TABLE_NAME_PREFIX + sourceIndex + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME
                        // + " = " + TABLE_NAME_PREFIX + targetIndex + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + ")"
                        // + "\n" );

                        qNameJoins.add( new QNameJoin( qName, name, sourceIndex, targetIndex ) );
                        maxTableIndex.setInt( maxTableIndex.getInt() + 1 );
                    }
                    modifyFromClauseAndWhereClauseToGetValue( name, propertyValue, predicate, negationActive,
                        targetIndex, maxTableIndex, columnName, collectionPath, vendor, whereClause, afterWhere,
                        fromClause, groupBy, having, qNameJoins, values, valueSQLTypes );
                }
            } );

        }
        else
        {
            // Primitive
            ColumnReferenceByName valueCol = c.colName( TABLE_NAME_PREFIX + currentTableIndex, columnName );
            if( value == null )
            {
                whereClause.and( b.isNull( l.l( valueCol ) ) );
                // this.appendToWhereClause( whereClause,
                // valueCol + " IS " + /* (negationActive ? "NOT " : "") + */"NULL", "AND" );
            }
            else
            {
                Object dbValue = value;
                if( Enum.class.isAssignableFrom( value.getClass() ) )
                {
                    dbValue = this._state.enumPKs().get().get( value.getClass().getName() );
                }
                whereClause.and( b.and( b.isNotNull( l.l( valueCol ) ),
                    getOperator( predicate ).getExpression( b, l.l( valueCol ), l.param() ) ) );
                // this.appendToWhereClause( whereClause, "(" + valueCol + " IS NOT NULL AND " + valueCol + " "
                // + getOperator( predicate ) + " ?)" /*
                // * + ( valueMayBeNull ? " OR " + valueCol + " IS NULL" : "") +
                // * ")"
                // */, "AND" );
                values.add( dbValue );
                valueSQLTypes.add( _typeHelper.getSQLType( value ) );
                _log.info( TABLE_NAME_PREFIX + currentTableIndex + "." + columnName + " is " + dbValue );
            }
        }

        return result;
    }

    private void appendToWhereClause( StringBuilder clause, String content, String joiner )
    {
        if( clause.length() == 0 )
        {
            clause.append( content + "\n" );
        }
        else
        {
            clause.append( joiner + " " + content + "\n" );
        }
    }

}
