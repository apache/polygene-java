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

package org.qi4j.index.sql.support.skeletons;

import org.qi4j.api.Qi4j;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.grammar.*;
import org.qi4j.api.query.grammar.OrderBy.Order;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.index.sql.support.api.SQLQuerying;
import org.qi4j.index.sql.support.common.DBNames;
import org.qi4j.index.sql.support.common.EntityTypeInfo;
import org.qi4j.index.sql.support.common.QNameInfo;
import org.qi4j.index.sql.support.postgresql.PostgreSQLTypeHelper;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.query.EntityFinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql.generation.api.grammar.builders.booleans.BooleanBuilder;
import org.sql.generation.api.grammar.builders.booleans.InBuilder;
import org.sql.generation.api.grammar.builders.query.GroupByBuilder;
import org.sql.generation.api.grammar.builders.query.QueryBuilder;
import org.sql.generation.api.grammar.builders.query.QuerySpecificationBuilder;
import org.sql.generation.api.grammar.builders.query.TableReferenceBuilder;
import org.sql.generation.api.grammar.common.NonBooleanExpression;
import org.sql.generation.api.grammar.common.SQLFunctions;
import org.sql.generation.api.grammar.common.SetQuantifier;
import org.sql.generation.api.grammar.factories.*;
import org.sql.generation.api.grammar.query.*;
import org.sql.generation.api.grammar.query.QuerySpecification;
import org.sql.generation.api.grammar.query.joins.JoinType;
import org.sql.generation.api.vendor.SQLVendor;

import java.lang.reflect.Member;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.*;

import static org.qi4j.functional.Iterables.first;

/**
 * 
 * @author Stanislav Muhametsin
 */
public abstract class AbstractSQLQuerying
    implements SQLQuerying, Activatable
{

    @This
    private SQLDBState _state;

    @This
    private PostgreSQLTypeHelper _typeHelper;

    @Structure
    private Module module;

    @Structure
    private Qi4jSPI spi;

    public static interface SQLBooleanCreator
    {
        public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
            NonBooleanExpression left, NonBooleanExpression right );
    }

    private static Map<Class<? extends Specification>, SQLBooleanCreator> _sqlOperators;

    private static Map<Class<? extends Specification>, JoinType> _joinStyles;

    private static Map<Class<? extends Specification>, JoinType> _negatedJoinStyles;

    private static final String TABLE_NAME_PREFIX = "t";

    private static final Logger _log = LoggerFactory.getLogger( AbstractSQLQuerying.class.getName() );

    static
    {
        _sqlOperators = new HashMap<Class<? extends Specification>, SQLBooleanCreator>();
        _sqlOperators.put( EqSpecification.class, new SQLBooleanCreator()
        {

            public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
                NonBooleanExpression left, NonBooleanExpression right )
            {
                return factory.eq( left, right );
            }
        } );
        _sqlOperators.put( GeSpecification.class, new SQLBooleanCreator()
        {

            public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
                NonBooleanExpression left, NonBooleanExpression right )
            {
                return factory.geq( left, right );
            }
        } );
        _sqlOperators.put( GtSpecification.class, new SQLBooleanCreator()
        {

            public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
                NonBooleanExpression left, NonBooleanExpression right )
            {
                return factory.gt( left, right );
            }
        } );
        _sqlOperators.put( LeSpecification.class, new SQLBooleanCreator()
        {

            public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
                NonBooleanExpression left, NonBooleanExpression right )
            {
                return factory.leq( left, right );
            }
        } );
        _sqlOperators.put( LtSpecification.class, new SQLBooleanCreator()
        {

            public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
                NonBooleanExpression left, NonBooleanExpression right )
            {
                return factory.lt( left, right );
            }
        } );
        _sqlOperators.put( ManyAssociationContainsSpecification.class, new SQLBooleanCreator()
        {

            public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
                NonBooleanExpression left, NonBooleanExpression right )
            {
                return factory.eq( left, right );
            }
        } );
        _sqlOperators.put( MatchesSpecification.class, new SQLBooleanCreator()
        {

            public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
                NonBooleanExpression left, NonBooleanExpression right )
            {
                return factory.regexp( left, right );
            }
        } );
        _sqlOperators.put( ContainsSpecification.class, new SQLBooleanCreator()
        {

            public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
                NonBooleanExpression left, NonBooleanExpression right )
            {
                return factory.eq( left, right );
            }
        } );
        _sqlOperators.put( ContainsAllSpecification.class, new SQLBooleanCreator()
        {

            public org.sql.generation.api.grammar.booleans.BooleanExpression getExpression( BooleanFactory factory,
                NonBooleanExpression left, NonBooleanExpression right )
            {
                return factory.eq( left, right );
            }
        } );

        _joinStyles = new HashMap<Class<? extends Specification>, JoinType>();
        _joinStyles.put( EqSpecification.class, JoinType.INNER );
        _joinStyles.put( GeSpecification.class, JoinType.INNER );
        _joinStyles.put( GtSpecification.class, JoinType.INNER );
        _joinStyles.put( LeSpecification.class, JoinType.INNER );
        _joinStyles.put( LtSpecification.class, JoinType.INNER );
        _joinStyles.put( PropertyNullSpecification.class, JoinType.LEFT_OUTER );
        _joinStyles.put( PropertyNotNullSpecification.class, JoinType.INNER );
        _joinStyles.put( AssociationNullSpecification.class, JoinType.LEFT_OUTER );
        _joinStyles.put( AssociationNotNullSpecification.class, JoinType.INNER );
        _joinStyles.put( ManyAssociationContainsSpecification.class, JoinType.INNER );
        _joinStyles.put( MatchesSpecification.class, JoinType.INNER );
        _joinStyles.put( ContainsSpecification.class, JoinType.INNER );
        _joinStyles.put( ContainsAllSpecification.class, JoinType.INNER );

        _negatedJoinStyles = new HashMap<Class<? extends Specification>, JoinType>();
        _negatedJoinStyles.put( EqSpecification.class, JoinType.LEFT_OUTER );
        _negatedJoinStyles.put( GeSpecification.class, JoinType.LEFT_OUTER );
        _negatedJoinStyles.put( GtSpecification.class, JoinType.LEFT_OUTER );
        _negatedJoinStyles.put( LeSpecification.class, JoinType.LEFT_OUTER );
        _negatedJoinStyles.put( LtSpecification.class, JoinType.LEFT_OUTER );
        _negatedJoinStyles.put( PropertyNullSpecification.class, JoinType.INNER );
        _negatedJoinStyles.put( PropertyNotNullSpecification.class, JoinType.LEFT_OUTER );
        _negatedJoinStyles.put( AssociationNullSpecification.class, JoinType.INNER );
        _negatedJoinStyles.put( AssociationNotNullSpecification.class, JoinType.LEFT_OUTER );
        _negatedJoinStyles.put( ManyAssociationContainsSpecification.class, JoinType.INNER );
        _negatedJoinStyles.put( MatchesSpecification.class, JoinType.LEFT_OUTER );
        _negatedJoinStyles.put( ContainsSpecification.class, JoinType.LEFT_OUTER );
        _negatedJoinStyles.put( ContainsAllSpecification.class, JoinType.LEFT_OUTER );
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

    public Integer getResultSetType( Integer firstResult, Integer maxResults )
    {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    public Boolean isFirstResultSettingSupported()
    {
        return true;
    }

    private SQLVendor _vendor;

    @Uses
    private ServiceDescriptor descriptor;

    public void activate()
        throws Exception
    {
        this._vendor = this.descriptor.metaInfo( SQLVendor.class );
    }

    public void passivate()
        throws Exception
    {

    }

    public String constructQuery( Class<?> resultType, //
        Specification<Composite> whereClause, //
        OrderBy[] orderBySegments, //
        Integer firstResult, //
        Integer maxResults, //
        List<Object> values, //
        List<Integer> valueSQLTypes, //
        Boolean countOnly //
    )
        throws EntityFinderException
    {
        SQLVendor vendor = this._vendor;

        QueryFactory q = vendor.getQueryFactory();
        TableReferenceFactory t = vendor.getTableReferenceFactory();
        LiteralFactory l = vendor.getLiteralFactory();
        ColumnsFactory c = vendor.getColumnsFactory();

        ColumnReference mainColumn = c.colName( TABLE_NAME_PREFIX + "0", DBNames.ENTITY_TABLE_IDENTITY_COLUMN_NAME );
        if( countOnly )
        {
            mainColumn = c.colExp( l.func( SQLFunctions.COUNT, mainColumn ) );
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

        String result = vendor.toString( finalMainQuery );

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
        InBuilder in = b.inBuilder( c.colName( TABLE_NAME_PREFIX + "0", DBNames.ENTITY_TYPES_TABLE_PK_COLUMN_NAME ) );
        for( Integer i : typeIDs )
        {
            in.addValues( l.n( i ) );
        }

        return in.createExpression();
    }

    protected abstract QueryExpression finalizeQuery( //
        SQLVendor sqlVendor, QuerySpecificationBuilder specBuilder, //
        Class<?> resultType, //
        Specification<Composite> whereClause, //
        OrderBy[] orderBySegments, //
        Integer firstResult, //
        Integer maxResults, //
        List<Object> values, //
        List<Integer> valueSQLTypes, //
        Boolean countOnly );

    protected QueryBuilder processBooleanExpression( //
        Specification<Composite> expression,//
        Boolean negationActive,//
        SQLVendor vendor,//
        org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition, //
        List<Object> values, List<Integer> valueSQLTypes )
    {
        QueryBuilder result = null;
        if( expression != null )
        {
            if( expression instanceof AndSpecification )
            {
                AndSpecification conjunction = (AndSpecification) expression;
                for( Specification<Composite> entitySpecification : conjunction.getOperands() )
                {
                    if (result == null)
                        result = processBooleanExpression( entitySpecification, negationActive, vendor,
                                    entityTypeCondition, values, valueSQLTypes );
                    else
                        result = result.intersect( processBooleanExpression( entitySpecification, negationActive, vendor,
                                    entityTypeCondition, values, valueSQLTypes ).createExpression() );
                }
            }
            else if( expression instanceof OrSpecification )
            {
                OrSpecification conjunction = (OrSpecification) expression;
                for( Specification<Composite> entitySpecification : conjunction.getOperands() )
                {
                    if (result == null)
                        result = processBooleanExpression( entitySpecification, negationActive, vendor,
                                    entityTypeCondition, values, valueSQLTypes );
                    else
                        result = result.union( processBooleanExpression( entitySpecification, negationActive, vendor,
                                entityTypeCondition, values, valueSQLTypes ).createExpression() );
                }
            }
            else if( expression instanceof NotSpecification )
            {
                result = this.processBooleanExpression( ((NotSpecification) expression).getOperand(), !negationActive, vendor,
                        entityTypeCondition, values, valueSQLTypes );
            }
            else if( expression instanceof MatchesSpecification )
            {
                result = this.processMatchesPredicate( (MatchesSpecification) expression, negationActive, vendor,
                        entityTypeCondition, values, valueSQLTypes );
            }
            else if( expression instanceof ComparisonSpecification<?> )
            {
                result = this.processComparisonPredicate( (ComparisonSpecification<?>) expression, negationActive, vendor,
                        entityTypeCondition, values, valueSQLTypes );
            }
            else if( expression instanceof ManyAssociationContainsSpecification<?> )
            {
                result = this.processManyAssociationContainsPredicate(
                    (ManyAssociationContainsSpecification<?>) expression, negationActive, vendor, entityTypeCondition,
                    values, valueSQLTypes );
            }
            else if( expression instanceof PropertyNullSpecification<?> )
            {
                result = this.processPropertyNullPredicate( (PropertyNullSpecification<?>) expression, negationActive,
                    vendor, entityTypeCondition );
            }
            else if( expression instanceof AssociationNullSpecification )
            {
                result = this.processAssociationNullPredicate( (AssociationNullSpecification) expression, negationActive,
                    vendor, entityTypeCondition );
            }
            else if( expression instanceof ContainsSpecification<?> )
            {
                result = this.processContainsPredicate( (ContainsSpecification<?>) expression, negationActive, vendor,
                    entityTypeCondition, values, valueSQLTypes );
            }
            else if( expression instanceof ContainsAllSpecification<?> )
            {
                result = this.processContainsAllPredicate( (ContainsAllSpecification<?>) expression, negationActive,
                    vendor, entityTypeCondition, values, valueSQLTypes );
            }
            else
            {
                throw new UnsupportedOperationException( "Expression " + expression + " of type " + expression.getClass() + " is not supported" );
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
        TableReferenceFactory t = vendor.getTableReferenceFactory();

        String tableAlias = TABLE_NAME_PREFIX + "0";
        QuerySpecificationBuilder query = this.getBuilderForPredicate( vendor, tableAlias );
        query.getFrom().addTableReferences(
            t.tableBuilder( t.table( t.tableName( this._state.schemaName().get(), DBNames.ENTITY_TABLE_NAME ),
                t.tableAlias( tableAlias ) ) ) );
        query.getWhere().reset( entityTypeCondition );

        return query;
    }

    protected QueryBuilder processMatchesPredicate( final MatchesSpecification predicate, final Boolean negationActive,
        final SQLVendor vendor, org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition,
        final List<Object> values, final List<Integer> valueSQLTypes )
    {
        return this.singleQuery( //
            predicate, //
            predicate.getProperty(), //
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
                        vendor.getBooleanFactory().regexp(
                            c.colName( TABLE_NAME_PREFIX + lastTableIndex, DBNames.QNAME_TABLE_VALUE_COLUMN_NAME ),
                            l.param() ) );

                    values.add( translateJavaRegexpToPGSQLRegexp( predicate.getRegexp() ));
                    valueSQLTypes.add( Types.VARCHAR );
                }
            } //
            );
    }

    protected QueryBuilder processComparisonPredicate( final ComparisonSpecification<?> predicate,
        final Boolean negationActive, final SQLVendor vendor,
        org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition, final List<Object> values,
        final List<Integer> valueSQLTypes )
    {
        return this.singleQuery( //
            predicate, //
            predicate.getProperty(), //
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
                    QualifiedName qName = QualifiedName.fromAccessor( predicate.getProperty().getAccessor() );
                    String columnName = null;
                    if( qName.type().equals( Identity.class.getName() ) )
                    {
                        columnName = DBNames.ENTITY_TABLE_IDENTITY_COLUMN_NAME;
                    }
                    else
                    {
                        columnName = DBNames.QNAME_TABLE_VALUE_COLUMN_NAME;
                    }
                    Object value = predicate.getValue();
                    modifyFromClauseAndWhereClauseToGetValue( qName, value, predicate, negationActive, lastTableIndex,
                        new ModifiableInt( lastTableIndex ), columnName,
                        DBNames.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME, vendor, builder.getWhere(), afterWhere,
                        builder.getFrom().getTableReferences().iterator().next(), builder.getGroupBy(),
                        builder.getHaving(), new ArrayList<QNameJoin>(), values, valueSQLTypes );
                }

            } //
            );
    }

    protected QueryBuilder processManyAssociationContainsPredicate(
        final ManyAssociationContainsSpecification<?> predicate, final Boolean negationActive, final SQLVendor vendor,
        org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition, final List<Object> values,
        final List<Integer> valueSQLTypes )
    {
        return this.singleQuery( //
            predicate, //
            null, //
            null, //
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
                        getOperator( predicate ).getExpression( b,
                            c.colName( TABLE_NAME_PREFIX + lastTableIndex, DBNames.ENTITY_TABLE_IDENTITY_COLUMN_NAME ),
                            l.param() ) );

                    Object value = predicate.getValue();
                    // TODO Is it really certain that this value is always instance of EntityComposite?
                    if( value instanceof EntityComposite )
                    {
                        value = module.currentUnitOfWork().get( (EntityComposite) value ).identity().get();
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

    protected QueryBuilder processPropertyNullPredicate( final PropertyNullSpecification<?> predicate,
        final Boolean negationActive, final SQLVendor vendor,
        org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition )
    {
        return this.singleQuery( //
            predicate, //
            predicate.getProperty(), //
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
                    if( !negationActive)
                    {
                        ColumnsFactory c = vendor.getColumnsFactory();
                        BooleanFactory b = vendor.getBooleanFactory();

                        QNameInfo info = _state
                            .qNameInfos()
                            .get()
                            .get(
                                QualifiedName.fromAccessor( predicate.getProperty().getAccessor() ) );
                        String colName = null;
                        if( info.getCollectionDepth() > 0 )
                        {
                            colName = DBNames.ALL_QNAMES_TABLE_PK_COLUMN_NAME;
                        }
                        else
                        {
                            colName = DBNames.QNAME_TABLE_VALUE_COLUMN_NAME;
                        }
                        // Last table column might be null because of left joins
                        builder.getWhere().reset( b.isNull( c.colName( TABLE_NAME_PREFIX + lastTableIndex, colName ) ) );
                    }
                }

            } //
        );
    }

    protected QueryBuilder processAssociationNullPredicate( final AssociationNullSpecification predicate,
        final Boolean negationActive, final SQLVendor vendor,
        org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition )
    {
        return this.singleQuery( //
            predicate, //
            null, //
            predicate.getAssociation(), //
            false, //
            negationActive, //
            vendor, //
            entityTypeCondition, //
            new WhereClauseProcessor()
            {

                public void processWhereClause( QuerySpecificationBuilder builder, BooleanBuilder afterWhere,
                    JoinType joinStyle, Integer firstTableIndex, Integer lastTableIndex )
                {
                    if( !negationActive)
                    {
                        ColumnsFactory c = vendor.getColumnsFactory();
                        BooleanFactory b = vendor.getBooleanFactory();

                        // Last table column might be null because of left joins
                        builder.getWhere().reset(
                            b.isNull( c.colName( TABLE_NAME_PREFIX + lastTableIndex,
                                DBNames.QNAME_TABLE_VALUE_COLUMN_NAME ) ) );
                    }
                }

            } //
            );
    }

    protected QueryBuilder processContainsPredicate( final ContainsSpecification<?> predicate,
        final Boolean negationActive, final SQLVendor vendor,
        org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition, final List<Object> values,
        final List<Integer> valueSQLTypes )
    {
        // Path: Top.* (star without braces), value = value
        // ASSUMING value is NOT collection (ie, find all entities, which collection property has value x as leaf item,
        // no matter collection depth)
        QuerySpecification contains = this.constructQueryForPredicate( //
            predicate, //
            predicate.getCollectionProperty(), //
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
                        b.regexp( c.colName( TABLE_NAME_PREFIX + lastTableIndex,
                            DBNames.QNAME_TABLE_COLLECTION_PATH_COLUMN_NAME ), l
                            .s( DBNames.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME + ".*{1,}" ) ) );

                    Object value = predicate.getValue();
                    if( value instanceof Collection<?> )
                    {
                        throw new IllegalArgumentException(
                            "ContainsPredicate may have only either primitive or value composite as value." );
                    }
                    BooleanBuilder condition = b.booleanBuilder();
                    modifyFromClauseAndWhereClauseToGetValue( QualifiedName.fromAccessor( predicate.getCollectionProperty().getAccessor() ), value, predicate,
                        false, lastTableIndex, new ModifiableInt( lastTableIndex ),
                        DBNames.QNAME_TABLE_VALUE_COLUMN_NAME, DBNames.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME,
                        vendor, condition, afterWhere, builder.getFrom().getTableReferences().iterator().next(),
                        builder.getGroupBy(), builder.getHaving(), new ArrayList<QNameJoin>(), values, valueSQLTypes );
                    builder.getWhere().and( condition.createExpression() );
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

    protected QueryBuilder processContainsAllPredicate(
        final ContainsAllSpecification<?> predicate, final Boolean negationActive,
        final SQLVendor vendor, org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition,
        final List<Object> values, final List<Integer> valueSQLTypes )
    {
        // has all leaf items in specified collection

        QuerySpecification contains = this.constructQueryForPredicate( //
            predicate, //
            predicate.getCollectionProperty(), //
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

                    Iterable<?> collection = predicate.getValueCollection();
                    List<QNameJoin> joins = new ArrayList<QNameJoin>();
                    for( Object value : collection )
                    {
                        if( value instanceof Collection<?> )
                        {
                            throw new IllegalArgumentException(
                                "ContainsAllPredicate may not have nested collections as value." );
                        }

                        BooleanBuilder conditionForItem = b.booleanBuilder( b.regexp( c.colName( TABLE_NAME_PREFIX
                            + lastTableIndex, DBNames.QNAME_TABLE_COLLECTION_PATH_COLUMN_NAME ), l
                            .s( DBNames.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME + ".*{1,}" ) ) );
                        modifyFromClauseAndWhereClauseToGetValue(
                            QualifiedName.fromAccessor( predicate.getCollectionProperty().getAccessor() ), value, predicate, false, lastTableIndex,
                            new ModifiableInt( lastTableIndex ), DBNames.QNAME_TABLE_VALUE_COLUMN_NAME,
                            DBNames.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME, vendor, conditionForItem, afterWhere,
                            builder.getFrom().getTableReferences().iterator().next(), builder.getGroupBy(), builder
                                .getHaving(), joins, values, valueSQLTypes );
                        builder.getWhere().or( conditionForItem.createExpression() );

                    }

                    builder.getHaving()
                        .and(
                            b.geq(
                                l.func( "COUNT", c.colName( TABLE_NAME_PREFIX + lastTableIndex,
                                    DBNames.QNAME_TABLE_VALUE_COLUMN_NAME ) ), l.n( Iterables.count( collection ) ) ) );
                }

            } //
            );

        return this.finalizeContainsQuery( vendor, contains, entityTypeCondition, negationActive );
    }

    protected QueryBuilder singleQuery( //
        Specification<Composite> predicate, //
        PropertyFunction<?> propRef, //
        AssociationFunction<?> assoRef, //
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

    protected QuerySpecification constructQueryForPredicate( //
        Specification<Composite> predicate, //
        PropertyFunction<?> propRef, //
        AssociationFunction<?> assoRef, //
        Boolean includeLastAssoPathTable, //
        Boolean negationActive, //
        SQLVendor vendor, //
        org.sql.generation.api.grammar.booleans.BooleanExpression entityTypeCondition, //
        WhereClauseProcessor whereClauseGenerator//
    )
    {
        Integer startingIndex = 0;
        TableReferenceFactory t = vendor.getTableReferenceFactory();

        QuerySpecificationBuilder builder = this.getBuilderForPredicate( vendor, TABLE_NAME_PREFIX + startingIndex );
        TableReferenceBuilder from = t.tableBuilder( t.table(
            t.tableName( this._state.schemaName().get(), DBNames.ENTITY_TABLE_NAME ),
            t.tableAlias( TABLE_NAME_PREFIX + startingIndex ) ) );

        Integer lastTableIndex = null;
        JoinType joinStyle = this.getTableJoinStyle( predicate, negationActive );
        if( propRef == null )
        {
            lastTableIndex = this.traverseAssociationPath( assoRef, startingIndex, startingIndex + 1, vendor, from,
                joinStyle, includeLastAssoPathTable );
        }
        else if( assoRef == null )
        {
            lastTableIndex = this.traversePropertyPath( propRef, startingIndex, startingIndex + 1, vendor, from,
                joinStyle );
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

        builder.trimGroupBy();

        return builder.createExpression();
    }

    protected SQLBooleanCreator getOperator( Specification<Composite> predicate )
    {
        return this.findFromLookupTables( _sqlOperators, null, predicate, false );
    }

    protected JoinType getTableJoinStyle( Specification<Composite> predicate, Boolean negationActive )
    {
        return this.findFromLookupTables( _joinStyles, _negatedJoinStyles, predicate, negationActive );
    }

    protected <ReturnType> ReturnType findFromLookupTables( Map<Class<? extends Specification>, ReturnType> normal,
        Map<Class<? extends Specification>, ReturnType> negated, Specification<Composite> predicate, Boolean negationActive )
    {
        Class<? extends Specification> predicateClass = predicate.getClass();
        ReturnType result = null;
        Set<Map.Entry<Class<? extends Specification>, ReturnType>> entries = negationActive ? negated.entrySet() : normal
            .entrySet();
        for( Map.Entry<Class<? extends Specification>, ReturnType> entry : entries )
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

    protected QuerySpecificationBuilder getBuilderForPredicate( SQLVendor vendor, String tableAlias )
    {
        QueryFactory q = vendor.getQueryFactory();
        ColumnsFactory c = vendor.getColumnsFactory();
        QuerySpecificationBuilder result = q.querySpecificationBuilder();
        result
            .getSelect()
            .setSetQuantifier( SetQuantifier.DISTINCT )
            .addUnnamedColumns( c.colName( tableAlias, DBNames.ENTITY_TABLE_PK_COLUMN_NAME ),
                c.colName( tableAlias, DBNames.ENTITY_TABLE_IDENTITY_COLUMN_NAME ) );

        return result;
    }

    protected String translateJavaRegexpToPGSQLRegexp( String javaRegexp )
    {
        // TODO
        // Yo dawg, I heard you like regular expressions, so we made a regexp about your regexp so you can match while
        // you match!
        // Meaning, probably best way to translate java regexp into pg-sql regexp is by... regexp.
        return javaRegexp;
    }

    protected void processOrderBySegments( OrderBy[] orderBy, SQLVendor vendor, QuerySpecificationBuilder builder )
    {
        if( orderBy != null )
        {
            QNameInfo[] qNames = new QNameInfo[orderBy.length];

            QueryFactory q = vendor.getQueryFactory();
            ColumnsFactory c = vendor.getColumnsFactory();

            Integer tableIndex = 0;
            for( Integer idx = 0; idx < orderBy.length; ++idx )
            {
                if( orderBy[idx] != null )
                {
                    PropertyFunction<?> ref = orderBy[idx].getPropertyFunction();
                    QualifiedName qName = QualifiedName.fromAccessor( ref.getAccessor() );
                    QNameInfo info = this._state.qNameInfos().get().get( qName );
                    qNames[idx] = info;
                    if( info == null )
                    {
                        throw new InternalError( "No qName info found for qName [" + qName + "]." );
                    }
                    tableIndex = this.traversePropertyPath( ref, 0, tableIndex + 1, vendor, builder.getFrom()
                        .getTableReferences().iterator().next(), JoinType.LEFT_OUTER );
                    Class<?> declaringType = ((Member)ref.getAccessor()).getDeclaringClass();
                    String colName = null;
                    Integer tableIdx = null;
                    if( Identity.class.equals( declaringType ) )
                    {
                        colName = DBNames.ENTITY_TABLE_IDENTITY_COLUMN_NAME;
                        tableIdx = tableIndex - 1;
                    }
                    else
                    {
                        colName = DBNames.QNAME_TABLE_VALUE_COLUMN_NAME;
                        tableIdx = tableIndex;
                    }
                    Ordering ordering = Ordering.ASCENDING;
                    if( orderBy[idx].order() == Order.DESCENDING )
                    {
                        ordering = Ordering.DESCENDING;
                    }
                    builder.getOrderBy().addSortSpecs(
                        q.sortSpec( c.colName( TABLE_NAME_PREFIX + tableIdx, colName ), ordering ) );
                }
            }
        }

    }

    protected Integer traversePropertyPath( PropertyFunction<?> reference, Integer lastTableIndex,
        Integer nextAvailableIndex, SQLVendor vendor, TableReferenceBuilder builder, JoinType joinStyle )
    {

        Stack<QualifiedName> qNameStack = new Stack<QualifiedName>();
        Stack<PropertyFunction<?>> refStack = new Stack<PropertyFunction<?>>();

        while( reference != null )
        {
            qNameStack.add( QualifiedName.fromAccessor( reference.getAccessor() ) );
            refStack.add( reference );
            if( reference.getTraversedProperty() == null && reference.getTraversedAssociation() != null )
            {
                Integer lastAssoTableIndex = this.traverseAssociationPath( reference.getTraversedAssociation(),
                    lastTableIndex, nextAvailableIndex, vendor, builder, joinStyle, true );
                if( lastAssoTableIndex > lastTableIndex )
                {
                    lastTableIndex = lastAssoTableIndex;
                    nextAvailableIndex = lastTableIndex + 1;
                }
            }

            reference = reference.getTraversedProperty();
        }

        PropertyFunction<?> prevRef = null;
        String schemaName = this._state.schemaName().get();
        TableReferenceFactory t = vendor.getTableReferenceFactory();
        BooleanFactory b = vendor.getBooleanFactory();
        ColumnsFactory c = vendor.getColumnsFactory();

        while( !qNameStack.isEmpty() )
        {
            QualifiedName qName = qNameStack.pop();
            PropertyFunction<?> ref = refStack.pop();
            if( !qName.type().equals( Identity.class.getName() ) )
            {
                QNameInfo info = this._state.qNameInfos().get().get( qName );
                if( info == null )
                {
                    throw new InternalError( "No qName info found for qName [" + qName + "]." );
                }

                String prevTableAlias = TABLE_NAME_PREFIX + lastTableIndex;
                String nextTableAlias = TABLE_NAME_PREFIX + nextAvailableIndex;
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
                                    c.colName( prevTableAlias, DBNames.ENTITY_TABLE_PK_COLUMN_NAME ),
                                    c.colName( nextTableAlias, DBNames.ENTITY_TABLE_PK_COLUMN_NAME )
                                    )
                                )
                            .and(
                                    b.isNull( c.colName( nextTableAlias, DBNames.QNAME_TABLE_PARENT_QNAME_COLUMN_NAME ) )
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
                                    c.colName( prevTableAlias, DBNames.ALL_QNAMES_TABLE_PK_COLUMN_NAME ),
                                    c.colName( nextTableAlias, DBNames.QNAME_TABLE_PARENT_QNAME_COLUMN_NAME ) )
                                )
                            .and(
                                b.eq(
                                    c.colName( prevTableAlias, DBNames.ENTITY_TABLE_PK_COLUMN_NAME ),
                                    c.colName( nextTableAlias, DBNames.ENTITY_TABLE_PK_COLUMN_NAME )
                                    )
                                )
                            .createExpression()
                            )
                        );
                }
                // @formatter:on
                lastTableIndex = nextAvailableIndex;
                ++nextAvailableIndex;
                prevRef = ref;
            }
        }

        return lastTableIndex;
    }

    protected Integer traverseAssociationPath( AssociationFunction<?> reference, Integer lastTableIndex,
        Integer nextAvailableIndex, SQLVendor vendor, TableReferenceBuilder builder, JoinType joinStyle,
        Boolean includeLastTable )
    {
        Stack<QualifiedName> qNameStack = new Stack<QualifiedName>();
        TableReferenceFactory t = vendor.getTableReferenceFactory();
        BooleanFactory b = vendor.getBooleanFactory();
        ColumnsFactory c = vendor.getColumnsFactory();
        String schemaName = this._state.schemaName().get();

        while( reference != null )
        {
            qNameStack
                .add( QualifiedName.fromAccessor( reference.getAccessor() ) );
            reference = reference.getTraversedAssociation();
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
                    + nextAvailableIndex ) ),
                t.jc(
                    b.eq(
                        c.colName( TABLE_NAME_PREFIX + lastTableIndex, DBNames.ENTITY_TABLE_PK_COLUMN_NAME ),
                        c.colName( TABLE_NAME_PREFIX + nextAvailableIndex, DBNames.ENTITY_TABLE_PK_COLUMN_NAME ) )
                    ) );
            lastTableIndex = nextAvailableIndex;
            ++nextAvailableIndex;
            if( includeLastTable || !qNameStack.isEmpty() )
            {
                builder.addQualifiedJoin(
                    joinStyle,
                    t.table( t.tableName( schemaName, DBNames.ENTITY_TABLE_NAME ), t.tableAlias( TABLE_NAME_PREFIX + nextAvailableIndex ) ),
                    t.jc(
                        b.eq(
                            c.colName( TABLE_NAME_PREFIX + lastTableIndex, DBNames.QNAME_TABLE_VALUE_COLUMN_NAME ),
                            c.colName( TABLE_NAME_PREFIX + nextAvailableIndex, DBNames.ENTITY_TABLE_PK_COLUMN_NAME )
                            )
                        )
                    );
                lastTableIndex = nextAvailableIndex;
                ++nextAvailableIndex;
            }
            // @formatter:on
        }

        return lastTableIndex;
    }

    protected List<Integer> getEntityTypeIDs( Class<?> entityType )
    {
        List<Integer> result = new ArrayList<Integer>();
        for( Map.Entry<String, EntityTypeInfo> entry : this._state.entityTypeInfos().get().entrySet() )
        {
            if( entityType.isAssignableFrom( first( entry.getValue().getEntityDescriptor().types() ) ) )
            {
                result.add( entry.getValue().getEntityTypePK() );
            }
        }

        return result;
    }

    // TODO refactor this monster of a method to something more understandable
    protected Integer modifyFromClauseAndWhereClauseToGetValue( final QualifiedName qName, Object value,
        final Specification<Composite> predicate, final Boolean negationActive, final Integer currentTableIndex,
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
        final TableReferenceFactory t = vendor.getTableReferenceFactory();

        if( value instanceof Collection<?> )
        {
            // Collection
            Integer collectionIndex = 0;
            Boolean collectionIsSet = value instanceof Set<?>;
            Boolean topLevel = collectionPath.equals( DBNames.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME );
            String collTable = TABLE_NAME_PREFIX + currentTableIndex;
            String collCol = DBNames.QNAME_TABLE_COLLECTION_PATH_COLUMN_NAME;
            ColumnReferenceByName collColExp = c.colName( collTable, collCol );

            BooleanBuilder collectionCondition = b.booleanBuilder();

            if( topLevel && negationActive )
            {
                afterWhere.and( b
                    .booleanBuilder( b.neq( collColExp, l.s( DBNames.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME ) ) )
                    .or( b.isNull( collColExp ) ).createExpression() );
            }

            Integer totalItemsProcessed = 0;
            for( Object item : (Collection<?>) value )
            {
                String path = collectionPath + DBNames.QNAME_TABLE_COLLECTION_PATH_SEPARATOR
                    + (collectionIsSet ? "*{1,}" : collectionIndex);
                Boolean isCollection = (item instanceof Collection<?>);
                BooleanBuilder newWhere = b.booleanBuilder();
                if( !isCollection )
                {
                    newWhere.reset( b.regexp( collColExp, l.s( path ) ) );
                }
                totalItemsProcessed = totalItemsProcessed
                    + modifyFromClauseAndWhereClauseToGetValue( qName, item, predicate, negationActive,
                        currentTableIndex, maxTableIndex, columnName, path, vendor, newWhere, afterWhere, fromClause,
                        groupBy, having, qNameJoins, values, valueSQLTypes );

                ++collectionIndex;
                collectionCondition.or( newWhere.createExpression() );
            }
            result = totalItemsProcessed;

            if( topLevel )
            {
                if( totalItemsProcessed == 0 )
                {
                    collectionCondition.and( b.isNotNull( collColExp ) ).and(
                        b.eq( collColExp, l.l( DBNames.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME ) ) );
                }
                else if( !negationActive )
                {
                    groupBy.addGroupingElements( q.groupingElement( c.colName( TABLE_NAME_PREFIX + currentTableIndex,
                        DBNames.ENTITY_TABLE_PK_COLUMN_NAME ) ) );
                    having
                        .and( b.eq(
                            l.func( SQLFunctions.COUNT, c.colName( TABLE_NAME_PREFIX + currentTableIndex,
                                DBNames.QNAME_TABLE_VALUE_COLUMN_NAME ) ), l.n( totalItemsProcessed ) ) );

                }
            }

            whereClause.and( collectionCondition.createExpression() );

        }
        else if( value instanceof ValueComposite )
        {
            // Visit all properties with recursion and make joins as necessary
            // @formatter:off
            for ( Property property : Qi4j.INSTANCE_FUNCTION.map( (ValueComposite) value ).state().properties())
            {
                    Boolean qNameJoinDone = false;
                    Integer sourceIndex = maxTableIndex.getInt();
                    Integer targetIndex = sourceIndex + 1;
                    for( QNameJoin join : qNameJoins )
                    {
                        if( join.getSourceQName().equals( qName ) )
                        {
                            sourceIndex = join.getSourceTableIndex();
                            if( join.getTargetQName().equals( spi.getPropertyDescriptor( property).qualifiedName() ) )
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
                        QNameInfo info = _state.qNameInfos().get().get( spi.getPropertyDescriptor( property).qualifiedName() );
                        String prevTableName = TABLE_NAME_PREFIX + sourceIndex;
                        String nextTableName = TABLE_NAME_PREFIX + targetIndex;
                        fromClause.addQualifiedJoin(
                            JoinType.LEFT_OUTER,
                            t.table( t.tableName( schemaName, info.getTableName() ), t.tableAlias( TABLE_NAME_PREFIX + targetIndex ) ),
                            t.jc(
                                b.booleanBuilder(b.eq(
                                    c.colName( prevTableName, DBNames.ALL_QNAMES_TABLE_PK_COLUMN_NAME ),
                                    c.colName( nextTableName, DBNames.QNAME_TABLE_PARENT_QNAME_COLUMN_NAME )
                                    ) )
                                .and(b.eq(
                                    c.colName( prevTableName, DBNames.ENTITY_TABLE_PK_COLUMN_NAME ),
                                    c.colName( nextTableName, DBNames.ENTITY_TABLE_PK_COLUMN_NAME )
                                    ) ).createExpression()
                                )
                            );
                        // @formatter:on

                        qNameJoins.add( new QNameJoin( qName, spi.getPropertyDescriptor( property).qualifiedName(), sourceIndex, targetIndex ) );
                        maxTableIndex.setInt( maxTableIndex.getInt() + 1 );
                    }
                    modifyFromClauseAndWhereClauseToGetValue( spi.getPropertyDescriptor( property).qualifiedName(), property.get(), predicate, negationActive,
                        targetIndex, maxTableIndex, columnName, collectionPath, vendor, whereClause, afterWhere,
                        fromClause, groupBy, having, qNameJoins, values, valueSQLTypes );
            }

            // @formatter:on

        }
        else
        {
            // Primitive
            ColumnReferenceByName valueCol = c.colName( TABLE_NAME_PREFIX + currentTableIndex, columnName );
            if( value == null )
            {
                whereClause.and( b.isNull( valueCol ) );
            }
            else
            {
                Object dbValue = value;
                if( Enum.class.isAssignableFrom( value.getClass() ) )
                {
                    dbValue = this._state.enumPKs().get().get( value.getClass().getName() );
                }
                whereClause.and( b.and( b.isNotNull( valueCol ),
                    this.getOperator( predicate ).getExpression( b, valueCol, l.param() ) ) );
                values.add( dbValue );
                valueSQLTypes.add( _typeHelper.getSQLType( value ) );
                _log.info( TABLE_NAME_PREFIX + currentTableIndex + "." + columnName + " is " + dbValue );
            }
        }

        return result;
    }

}
