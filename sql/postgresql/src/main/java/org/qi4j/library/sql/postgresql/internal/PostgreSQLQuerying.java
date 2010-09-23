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

package org.qi4j.library.sql.postgresql.internal;

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
import org.qi4j.api.query.grammar.Predicate;
import org.qi4j.api.query.grammar.PropertyIsNotNullPredicate;
import org.qi4j.api.query.grammar.PropertyIsNullPredicate;
import org.qi4j.api.query.grammar.PropertyNullPredicate;
import org.qi4j.api.query.grammar.PropertyReference;
import org.qi4j.api.query.grammar.SingleValueExpression;
import org.qi4j.api.query.grammar.OrderBy.Order;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.library.sql.api.SQLQuerying;
import org.qi4j.library.sql.common.EntityTypeInfo;
import org.qi4j.library.sql.common.QNameInfo;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.spi.structure.ModuleSPI;

/**
 *
 * @author Stanislav Muhametsin
 */
public class PostgreSQLQuerying implements SQLQuerying
{

    @This
    private PostgreSQLDBState _state;

    @This
    private PostgreSQLTypeHelper _typeHelper;

    @Structure
    private UnitOfWorkFactory _uowf;

    @Structure
    private Module _module;

    private static Map<Class<? extends Predicate>, String> _sqlOperators;

    private static Map<Class<? extends Predicate>, String> _joinStyles;

    private static Map<Class<? extends Predicate>, String> _negatedJoinStyles;

    private static final String TABLE_NAME_PREFIX = "t";

    private static final Logger _log = Logger.getLogger( PostgreSQLQuerying.class.getName( ) );

    static
    {
        _sqlOperators = new HashMap<Class<? extends Predicate>, String>( );
        _sqlOperators.put( EqualsPredicate.class, "=" );
        _sqlOperators.put( GreaterOrEqualPredicate.class, ">=" );
        _sqlOperators.put( GreaterThanPredicate.class, ">" );
        _sqlOperators.put( LessOrEqualPredicate.class, "<=" );
        _sqlOperators.put( LessThanPredicate.class, "<" );
        _sqlOperators.put( NotEqualsPredicate.class, "<>" );
        _sqlOperators.put( ManyAssociationContainsPredicate.class, "=" );
        _sqlOperators.put( MatchesPredicate.class, "~" );
        _sqlOperators.put( ContainsPredicate.class, "=" );
        _sqlOperators.put( ContainsAllPredicate.class, "=" );

        _joinStyles = new HashMap<Class<? extends Predicate>, String>( );
        _joinStyles.put( EqualsPredicate.class, "JOIN" );
        _joinStyles.put( GreaterOrEqualPredicate.class, "JOIN" );
        _joinStyles.put( GreaterThanPredicate.class, "JOIN" );
        _joinStyles.put( LessOrEqualPredicate.class, "JOIN" );
        _joinStyles.put( LessThanPredicate.class, "JOIN" );
        _joinStyles.put( NotEqualsPredicate.class, "JOIN" );
        _joinStyles.put( PropertyIsNullPredicate.class, "LEFT JOIN" );
        _joinStyles.put( PropertyIsNotNullPredicate.class, "JOIN" );
        _joinStyles.put( AssociationIsNullPredicate.class, "LEFT JOIN" );
        _joinStyles.put( AssociationIsNotNullPredicate.class, "JOIN" );
        _joinStyles.put( ManyAssociationContainsPredicate.class, "JOIN" );
        _joinStyles.put( MatchesPredicate.class, "JOIN" );
        _joinStyles.put( ContainsPredicate.class, "JOIN" );
        _joinStyles.put( ContainsAllPredicate.class, "JOIN" );

        _negatedJoinStyles = new HashMap<Class<? extends Predicate>, String>( );
        _negatedJoinStyles.put( EqualsPredicate.class, "LEFT JOIN" );
        _negatedJoinStyles.put( GreaterOrEqualPredicate.class, "LEFT JOIN" );
        _negatedJoinStyles.put( GreaterThanPredicate.class, "LEFT JOIN" );
        _negatedJoinStyles.put( LessOrEqualPredicate.class, "LEFT JOIN" );
        _negatedJoinStyles.put( LessThanPredicate.class, "LEFT JOIN" );
        _negatedJoinStyles.put( NotEqualsPredicate.class, "JOIN" );
        _negatedJoinStyles.put( PropertyIsNullPredicate.class, "JOIN" );
        _negatedJoinStyles.put( PropertyIsNotNullPredicate.class, "LEFT JOIN" );
        _negatedJoinStyles.put( AssociationIsNullPredicate.class, "JOIN" );
        _negatedJoinStyles.put( AssociationIsNotNullPredicate.class, "LEFT JOIN" );
        _negatedJoinStyles.put( ManyAssociationContainsPredicate.class, "JOIN" );
        _negatedJoinStyles.put( MatchesPredicate.class, "LEFT JOIN" );
        _negatedJoinStyles.put( ContainsPredicate.class, "LEFT JOIN" );
        _negatedJoinStyles.put( ContainsAllPredicate.class, "LEFT JOIN" );
    }

    private interface WhereClauseProcessor
    {

        public void processWhereClause( StringBuilder where, StringBuilder fromClause, StringBuilder groupBy, StringBuilder having, StringBuilder afterWhere, String joinStyle, Integer firstTableIndex, Integer lastTableIndex );
    }

    private static class ModifiableInt
    {
        private int _int;

        public ModifiableInt(Integer integer)
        {
            this._int = integer;
        }

        public int getInt()
        {
            return this._int;
        }

        public void setInt(int integer)
        {
            this._int = integer;
        }

        @Override
        public String toString( )
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

        public QNameJoin(QualifiedName sourceQName, QualifiedName targetQName, Integer sourceTableIndex, Integer targetTableIndex)
        {
            this._sourceQName = sourceQName;
            this._targetQName = targetQName;
            this._sourceTableIndex = sourceTableIndex;
            this._targetTableIndex = targetTableIndex;
        }


        public QualifiedName getSourceQName( )
        {
            return this._sourceQName;
        }


        public QualifiedName getTargetQName( )
        {
            return this._targetQName;
        }


        public Integer getSourceTableIndex( )
        {
            return this._sourceTableIndex;
        }


        public Integer getTargetTableIndex( )
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

    public String constructQuery( String resultType, //
        BooleanExpression whereClause, //
        OrderBy[] orderBySegments, //
        Integer firstResult, //
        Integer maxResults, //
        List<Object> values, //
        List<Integer> valueSQLTypes, //
        Boolean countOnly //
    ) throws EntityFinderException
    {
        String select = countOnly ? "COUNT(%s)" : "%s";
        String entityTypeCondition = this.createTypeCondition( TABLE_NAME_PREFIX + "0", this.getConcreteEntityTypesList( resultType ) );
        String processedWhere = this.processBooleanExpression( whereClause, false, entityTypeCondition, values, valueSQLTypes );

        StringBuilder fromClause = new StringBuilder( );
        StringBuilder orderByClause = new StringBuilder( );
        if ( orderBySegments != null )
        {
            this.processOrderBySegments( orderBySegments, fromClause, orderByClause );
        }

        StringBuilder offset = new StringBuilder();
        if (firstResult != null && firstResult > 0)
        {
            offset.append( "OFFSET " + firstResult );
        }
        StringBuilder limit = new StringBuilder();
        if (maxResults != null && maxResults > 0)
        {
            limit.append( "LIMIT " + maxResults );
            if (orderByClause.length() == 0)
            {
                orderByClause.append( "ORDER BY " + TABLE_NAME_PREFIX + "0." + SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME );
            }
        }

        String result = //
            "SELECT " + String.format( select, TABLE_NAME_PREFIX + "0." + SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME ) + "\n" + /**/
            "FROM (" + processedWhere + ") AS " + TABLE_NAME_PREFIX + "0" + "\n" + /**/
            fromClause.toString( ) + "\n" + /**/
            orderByClause.toString( ) + "\n" + //
            limit.toString() + "\n" + //
            offset.toString() + "\n" //
        ;

        _log.info( "SQL query:\n" + result );
        return result;
    }

    private String processBooleanExpression( BooleanExpression expression, Boolean negationActive, String entityTypeCondition, List<Object> values, List<Integer> valueSQLTypes )
    {
        String result = "";
        if ( expression != null )
        {
            if ( expression instanceof Conjunction )
            {
                Conjunction conjunction = ( Conjunction ) expression;
                String left = this.processBooleanExpression( conjunction.leftSideExpression( ), negationActive, entityTypeCondition, values, valueSQLTypes );
                String right = this.processBooleanExpression( conjunction.rightSideExpression( ), negationActive, entityTypeCondition, values, valueSQLTypes );
                if ( left == "" )
                {
                    result = right;
                }
                else if ( right == "" )
                {
                    result = left;
                }
                else
                {
                    result = String.format( "(%s)\nINTERSECT\n(%s)", left, right );
                }
            }
            else if ( expression instanceof Disjunction )
            {
                Disjunction disjunction = ( Disjunction ) expression;
                String left = this.processBooleanExpression( disjunction.leftSideExpression( ), negationActive, entityTypeCondition, values, valueSQLTypes );
                String right = this.processBooleanExpression( disjunction.rightSideExpression( ), negationActive, entityTypeCondition, values, valueSQLTypes );
                if ( left == "" )
                {
                    result = right;
                }
                else if ( right == "" )
                {
                    result = left;
                }
                else
                {
                    result = String.format( "(%s)\nUNION\n(%s)", left, right );
                }
            }
            else if ( expression instanceof Negation )
            {
                result = this.processBooleanExpression( ( ( Negation ) expression ).expression( ), !negationActive, entityTypeCondition, values, valueSQLTypes );
            }
            else if ( expression instanceof MatchesPredicate )
            {
                result = this.processMatchesPredicate( ( MatchesPredicate ) expression, negationActive, entityTypeCondition, values, valueSQLTypes );
            }
            else if ( expression instanceof ComparisonPredicate<?> )
            {
                result = this.processComparisonPredicate( ( ComparisonPredicate<?> ) expression, negationActive, entityTypeCondition, values, valueSQLTypes );
            }
            else if ( expression instanceof ManyAssociationContainsPredicate<?> )
            {
                result = this.processManyAssociationContainsPredicate(
                    ( ManyAssociationContainsPredicate<?> ) expression,
                    negationActive,
                    entityTypeCondition,
                    values,
                    valueSQLTypes );
            }
            else if ( expression instanceof PropertyNullPredicate<?> )
            {
                result = this.processPropertyNullPredicate( ( PropertyNullPredicate<?> ) expression, negationActive, entityTypeCondition );
            }
            else if ( expression instanceof AssociationNullPredicate )
            {
                result = this.processAssociationNullPredicate( ( AssociationNullPredicate ) expression, negationActive, entityTypeCondition );
            }
            else if ( expression instanceof ContainsPredicate<?, ?> )
            {
                result = this.processContainsPredicate( ( ContainsPredicate<?, ?> ) expression, negationActive, entityTypeCondition, values, valueSQLTypes );
            }
            else if ( expression instanceof ContainsAllPredicate<?, ?> )
            {
                result = this.processContainsAllPredicate( ( ContainsAllPredicate<?, ?> ) expression, negationActive, entityTypeCondition, values, valueSQLTypes );
            }
            else
            {
                throw new UnsupportedOperationException( "Expression " + expression + " is not supported" );
            }
        } else
        {
            StringBuilder selectBuilda = new StringBuilder();
            this.getSelectClauseForPredicate( TABLE_NAME_PREFIX + "0", selectBuilda );
            result = selectBuilda.toString( ) + "FROM " + this._state.schemaName( ).get( ) + "." + SQLs.ENTITY_TABLE_NAME + " AS " + TABLE_NAME_PREFIX + "0" + "\n" + /**/
                "WHERE " + entityTypeCondition;
        }

        return result;
    }

    private String processMatchesPredicate(
        final MatchesPredicate predicate,
        final Boolean negationActive,
        String entityTypeCondition,
        final List<Object> values,
        final List<Integer> valueSQLTypes )
    {
        return this.constructQueryForPredicate( //
            predicate, //
            predicate.propertyReference( ), //
            null, //
            null, //
            negationActive, //
            entityTypeCondition, //
            new WhereClauseProcessor( )
            {

                public void processWhereClause( StringBuilder where, StringBuilder fromClause, StringBuilder groupBy, StringBuilder having, StringBuilder afterWhere, String joinStyle, Integer firstTableIndex, Integer lastTableIndex )
                {
                    where.append(String.format(
                        TABLE_NAME_PREFIX + lastTableIndex + "." + SQLs.QNAME_TABLE_VALUE_COLUMN_NAME + " %s ?",
                        getOperator( predicate ) ));
                    values.add( translateJavaRegexpToPGSQLRegexp( ( ( SingleValueExpression<String> ) predicate.valueExpression( ) ).value( ) ) );
                    valueSQLTypes.add( Types.VARCHAR );
                }
            } //
            );
    }

    private String processComparisonPredicate(
        final ComparisonPredicate<?> predicate,
        final Boolean negationActive,
        String entityTypeCondition,
        final List<Object> values,
        final List<Integer> valueSQLTypes )
    {
        return this.constructQueryForPredicate( //
            predicate, //
            predicate.propertyReference( ), //
            null, //
            null, //
            negationActive, //
            entityTypeCondition, //
            new WhereClauseProcessor( )
            {

                public void processWhereClause( StringBuilder where, StringBuilder fromClause, StringBuilder groupBy, StringBuilder having, StringBuilder afterWhere, String joinStyle, Integer firstTableIndex, Integer lastTableIndex )
                {
                    QualifiedName qName = QualifiedName.fromClass( predicate.propertyReference( ).propertyDeclaringType( ), predicate
                        .propertyReference( ).propertyName( ) );
                    String columnName = null;
                    if ( qName.type( ).equals( Identity.class.getName( ) ) )
                    {
                        columnName = SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME;
                    }
                    else
                    {
                        columnName = SQLs.QNAME_TABLE_VALUE_COLUMN_NAME;
                    }
                    Object value = ( ( SingleValueExpression<?> ) predicate.valueExpression( ) ).value( );
                    modifyFromClauseAndWhereClauseToGetValue(
                        qName,
                        value,
                        predicate,
                        negationActive,
                        lastTableIndex,
                        new ModifiableInt( lastTableIndex ),
                        columnName,
                        SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME,
                        where,
                        afterWhere,
                        fromClause,
                        groupBy,
                        having,
                        new ArrayList<QNameJoin>( ),
                        values,
                        valueSQLTypes
                        );
                }
            } //
            );
    }

    private String processManyAssociationContainsPredicate(
        final ManyAssociationContainsPredicate<?> predicate,
        final Boolean negationActive,
        String entityTypeCondition,
        final List<Object> values,
        final List<Integer> valueSQLTypes )
    {
        return this.constructQueryForPredicate( //
            predicate, //
            null, //
            predicate.associationReference( ), //
            true, //
            negationActive, //
            entityTypeCondition, //
            new WhereClauseProcessor( )
            {

                public void processWhereClause( StringBuilder where, StringBuilder fromClause, StringBuilder groupBy, StringBuilder having, StringBuilder afterWhere, String joinStyle, Integer firstTableIndex, Integer lastTableIndex )
                {
                    where.append(TABLE_NAME_PREFIX + lastTableIndex + "." + SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME + " " +
                        getOperator( predicate ) + " ? " + "\n"); //
                    Object value = ( ( SingleValueExpression<?> ) predicate.valueExpression( ) ).value( );
                    // TODO Is it really certain that this value is always instance of EntityComposite?
                    if ( value instanceof EntityComposite )
                    {
                        value = _uowf.currentUnitOfWork( ).get( ( EntityComposite ) value ).identity( ).get( );
                    }
                    else
                    {
                        value = value.toString( );
                    }
                    values.add( value );
                    valueSQLTypes.add( Types.VARCHAR );
                }
            } //
            );
    }

    private String processPropertyNullPredicate( final PropertyNullPredicate<?> predicate, final Boolean negationActive, String entityTypeCondition)
    {
        return this.constructQueryForPredicate( //
            predicate, //
            predicate.propertyReference( ), //
            null, //
            null, //
            negationActive, //
            entityTypeCondition, //
            new WhereClauseProcessor( )
            {

                public void processWhereClause( StringBuilder where, StringBuilder fromClause, StringBuilder groupBy, StringBuilder having, StringBuilder afterWhere, String joinStyle, Integer firstTableIndex, Integer lastTableIndex )
                {
                    QNameInfo info = _state.qNameInfos( ).get( ).get(
                        QualifiedName.fromClass( predicate.propertyReference( ).propertyDeclaringType( ), predicate.propertyReference( )
                            .propertyName( ) ) );
                    String colName = null;
                    if ( info.getCollectionDepth( ) > 0 )
                    {
                        colName = SQLs.ALL_QNAMES_TABLE_PK_COLUMN_NAME;
                    }
                    else
                    {
                        colName = SQLs.QNAME_TABLE_VALUE_COLUMN_NAME;
                    }

                    if ((predicate instanceof PropertyIsNullPredicate<?> && !negationActive)
                        || (predicate instanceof PropertyIsNotNullPredicate<?> && negationActive))
                    {
                        // Last table column might be null because of left joins
                        where.append( TABLE_NAME_PREFIX + lastTableIndex + "." + colName + " IS NULL" + "\n");
                    }
                }
            } //
            );
    }

    private String processAssociationNullPredicate( final AssociationNullPredicate predicate, final Boolean negationActive, String entityTypeCondition )
    {
        return this.constructQueryForPredicate( //
            predicate, //
            null, //
            predicate.associationReference( ), //
            false, //
            negationActive, //
            entityTypeCondition, //
            new WhereClauseProcessor( )
            {

                public void processWhereClause( StringBuilder where, StringBuilder fromClause, StringBuilder groupBy, StringBuilder having, StringBuilder afterWhere, String joinStyle, Integer firstTableIndex, Integer lastTableIndex )
                {
                    String result = "";
                    if ((predicate instanceof AssociationIsNullPredicate && !negationActive)
                        || (predicate instanceof AssociationIsNotNullPredicate && negationActive))
                    {
                        // Last table column might be null because of left joins
                        where.append(TABLE_NAME_PREFIX + lastTableIndex + "." + SQLs.QNAME_TABLE_VALUE_COLUMN_NAME + " IS NULL" + "\n"); //
                    }
                }
            } //
            );
    }

    private String processContainsPredicate(
        final ContainsPredicate<?, ? extends Collection<?>> predicate,
        final Boolean negationActive,
        String entityTypeCondition,
        final List<Object> values,
        final List<Integer> valueSQLTypes )
    {
        // Path: Top.* (star without braces), value = value
        // ASSUMING value is NOT collection (ie, find all entities, which collection property has value x as leaf item, no matter collection depth)
        String contains = this.constructQueryForPredicate( //
            predicate, //
            predicate.propertyReference( ), //
            null, //
            null, //
            false, //
            entityTypeCondition, //
            new WhereClauseProcessor( )
            {

                public void processWhereClause( StringBuilder where, StringBuilder fromClause, StringBuilder groupBy, StringBuilder having, StringBuilder afterWhere, String joinStyle, Integer firstTableIndex, Integer lastTableIndex )
                {
                    where.append(TABLE_NAME_PREFIX + lastTableIndex + "." + SQLs.QNAME_TABLE_COLLECTION_PATH_COLUMN_NAME + " ~ '" +
                        SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME + ".*{1,}' AND (");
                    Object value = ((SingleValueExpression<?>)predicate.valueExpression( )).value( );
                    if (value instanceof Collection<?>)
                    {
                        throw new IllegalArgumentException( "ContainsPredicate may have only either primitive or value composite as value." );
                    }
                    StringBuilder condition = new StringBuilder( );
                    modifyFromClauseAndWhereClauseToGetValue(
                        QualifiedName.fromClass( predicate.propertyReference( ).propertyDeclaringType( ), predicate.propertyReference( ).propertyName( ) ),
                        value,
                        predicate,
                        false,
                        lastTableIndex,
                        new ModifiableInt( lastTableIndex ),
                        SQLs.QNAME_TABLE_VALUE_COLUMN_NAME,
                        SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME,
                        condition,
                        afterWhere,
                        fromClause,
                        groupBy,
                        having,
                        new ArrayList<QNameJoin>( ),
                        values,
                        valueSQLTypes
                        );
                    where.append(condition);
                    where.append(")");
                }
            } //
            );

        if (negationActive)
        {
            StringBuilder builda = new StringBuilder( );
            this.getSelectClauseForPredicate( TABLE_NAME_PREFIX + 0, builda );
            builda.append( "FROM " + this._state.schemaName( ).get() + "." + SQLs.ENTITY_TABLE_NAME + " " + TABLE_NAME_PREFIX + 0 + "\n" +
                "WHERE " + entityTypeCondition + "\n"
                );
            contains = builda.toString( ) + "EXCEPT" + "\n" + contains;
        }

        return contains;
    }

    private String processContainsAllPredicate(
        final ContainsAllPredicate<?, ? extends Collection<?>> predicate,
        final Boolean negationActive,
        String entityTypeCondition,
        final List<Object> values,
        final List<Integer> valueSQLTypes )
    {
        // has all leaf items in specified collection

        String contains =  this.constructQueryForPredicate( //
            predicate, //
            predicate.propertyReference( ), //
            null, //
            null, //
            false, //
            entityTypeCondition, //
            new WhereClauseProcessor( )
            {

                public void processWhereClause( StringBuilder where, StringBuilder fromClause, StringBuilder groupBy, StringBuilder having, StringBuilder afterWhere, String joinStyle, Integer firstTableIndex, Integer lastTableIndex )
                {
                    Collection<?> collection = (Collection<?>)((SingleValueExpression<?>)predicate.valueExpression( )).value( );
                    List<QNameJoin> joins = new ArrayList<QNameJoin>( );
                    for (Object value : collection)
                    {
                        if (value instanceof Collection<?>)
                        {
                            throw new IllegalArgumentException( "ContainsAllPredicate may not have nested collections as value." );
                        }
                        if (where.length( ) > 0)
                        {
                            where.append(" OR ");
                        }
                        where.append( "(" + TABLE_NAME_PREFIX + lastTableIndex + "." + SQLs.QNAME_TABLE_COLLECTION_PATH_COLUMN_NAME + " ~ '" +
                            SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME + ".*{1,}' AND (");
                        StringBuilder conditionForItem = new StringBuilder( );
                        modifyFromClauseAndWhereClauseToGetValue(
                            QualifiedName.fromClass( predicate.propertyReference( ).propertyDeclaringType( ), predicate.propertyReference( ).propertyName( ) ),
                            value,
                            predicate,
                            false,
                            lastTableIndex,
                            new ModifiableInt( lastTableIndex ),
                            SQLs.QNAME_TABLE_VALUE_COLUMN_NAME,
                            SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME,
                            conditionForItem,
                            afterWhere,
                            fromClause,
                            groupBy,
                            having,
                            joins,
                            values,
                            valueSQLTypes
                            );
                        where.append(conditionForItem);
                        where.append( " ))" + "\n");
                    }

//                    if (groupBy.length( ) > 0)
//                    {
//                        groupBy.append( ", " );
//                    }
                    if (having.length( ) > 0)
                    {
                        having.append( " AND " );
                    }
//                    groupBy.append( TABLE_NAME_PREFIX + lastTableIndex + "." + SQLs.QNAME_TABLE_VALUE_COLUMN_NAME);
                    having.append("COUNT(" + TABLE_NAME_PREFIX + lastTableIndex + "." + SQLs.QNAME_TABLE_VALUE_COLUMN_NAME + ") >= " + collection.size( ));

                }
            } //
            );
        if (negationActive)
        {
            StringBuilder builda = new StringBuilder( );
            this.getSelectClauseForPredicate( TABLE_NAME_PREFIX + 0, builda );
            builda.append( "FROM " + this._state.schemaName( ).get() + "." + SQLs.ENTITY_TABLE_NAME + " " + TABLE_NAME_PREFIX + 0 + "\n" +
                "WHERE " + entityTypeCondition + "\n"
                );
            contains = builda.toString( ) + "EXCEPT" + "\n" + contains;
        }

        return contains;
    }

    private String constructQueryForPredicate(
        Predicate predicate,
        PropertyReference<?> propRef,
        AssociationReference assoRef,
        Boolean includeLastAssoPathTable,
        Boolean negationActive,
        String entityTypeCondition,
        WhereClauseProcessor whereClauseGenerator )
    {
        StringBuilder builder = new StringBuilder( );
        Integer startingIndex = 0;
        this.getSelectClauseForPredicate( TABLE_NAME_PREFIX + startingIndex, builder );
        builder.append( "FROM " + this._state.schemaName( ).get( ) + "." + SQLs.ENTITY_TABLE_NAME + " " + TABLE_NAME_PREFIX + startingIndex + "\n" );
        Integer lastTableIndex = null;
        String joinStyle = this.getTableJoinStyle( predicate, negationActive );
        if ( propRef == null )
        {
            lastTableIndex = this.traverseAssociationPath(
                assoRef,
                builder,
                startingIndex,
                joinStyle,
                includeLastAssoPathTable );
        }
        else if ( assoRef == null )
        {
            lastTableIndex = this.traversePropertyPath( propRef, builder, startingIndex, joinStyle );
        }
        else
        {
            throw new InternalError( "Can not have both property reference and association reference (non-)nulls [propRef=" + propRef + ", assoRef=" +
                assoRef + ", predicate=" + predicate + "]." );
        }
        StringBuilder groupBy = new StringBuilder( );
        StringBuilder having = new StringBuilder( );
        StringBuilder afterWhere = new StringBuilder( );
        StringBuilder where = new StringBuilder( );
        whereClauseGenerator.processWhereClause( where, builder, groupBy, having, afterWhere, joinStyle, startingIndex, lastTableIndex );
        builder.append("WHERE ").append(entityTypeCondition + "\n");
        if ( where.length( ) > 0 )
        {
            builder.append( "AND ");
            if (negationActive)
            {
                builder.append("NOT ");
            }
            builder.append("(").append( where ).append( ")" + "\n" );
            builder.append(afterWhere);
        }
        if (having.length( ) > 0)
        {
            builder.append("GROUP BY " + (groupBy.length() > 0 ? (groupBy + ", ") : "") + TABLE_NAME_PREFIX + startingIndex + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + ", " + TABLE_NAME_PREFIX + startingIndex + "." + SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME + "\n");
        }
        if (having.length( ) > 0)
        {
            builder.append("HAVING " + having + "\n");
        }
        return builder.toString( );
    }

    private String getOperator( Predicate predicate )
    {
        return this.findFromLookupTables( _sqlOperators, null, predicate, false );
    }

    private String getTableJoinStyle( Predicate predicate, Boolean negationActive )
    {
        return this.findFromLookupTables( _joinStyles, _negatedJoinStyles, predicate, negationActive );
    }

    private String findFromLookupTables(
        Map<Class<? extends Predicate>, String> normal,
        Map<Class<? extends Predicate>, String> negated,
        Predicate predicate,
        Boolean negationActive )
    {
        Class<? extends Predicate> predicateClass = predicate.getClass( );
        String result = null;
        Set<Map.Entry<Class<? extends Predicate>, String>> entries = negationActive ? negated.entrySet( ) : normal.entrySet( );
        for ( Map.Entry<Class<? extends Predicate>, String> entry : entries )
        {
            if ( entry.getKey( ).isAssignableFrom( predicateClass ) )
            {
                result = entry.getValue( );
                break;
            }
        }

        if ( result == null )
        {
            throw new UnsupportedOperationException( "Predicate [" + predicateClass.getName( ) + "] is not supported" );
        }

        return result;
    }

    private void getSelectClauseForPredicate( String tableAlias, StringBuilder builder )
    {
        builder.append( "SELECT DISTINCT " + tableAlias + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + ", " + tableAlias + "." + SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME + "\n" );
    }

    private String createTypeCondition( String tableAlias, String entityTypeIDs )
    {
        return tableAlias + "." + SQLs.ENTITY_TYPES_TABLE_PK_COLUMN_NAME + " IN (" + entityTypeIDs + ")";
    }

    private String translateJavaRegexpToPGSQLRegexp( String javaRegexp )
    {
        // TODO
        // Yo dawg, I heard you like regular expressions, so we made a regexp about your regexp so you can match while you match!
        // Meaning, probably best way to translate java regexp into pg-sql regexp is by... regexp.
        return javaRegexp;
    }

    private void processOrderBySegments( OrderBy[] orderBy, StringBuilder fromClause, StringBuilder orderByClause )
    {
        QNameInfo[] qNames = new QNameInfo[orderBy.length];
        Integer[] tableIndices = new Integer[orderBy.length];
        String[] columnNames = new String[orderBy.length];

        Integer tableIndex = 0;
        for ( Integer idx = 0 ; idx < orderBy.length ; ++idx )
        {
            if ( orderBy[idx] != null )
            {
                PropertyReference<?> ref = orderBy[idx].propertyReference( );
                QualifiedName qName = QualifiedName.fromClass( ref.propertyDeclaringType( ), ref.propertyName( ) );
                QNameInfo info = this._state.qNameInfos( ).get( ).get( qName );
                qNames[idx] = info;
                if ( info == null )
                {
                    throw new InternalError( "No qName info found for qName [" + qName + "]." );
                }
                tableIndex = this.traversePropertyPath( ref, fromClause, tableIndex, "LEFT JOIN" );
                Class<?> declaringType = ref.propertyDeclaringType( );
                if ( Identity.class.equals( declaringType ) )
                {
                    columnNames[idx] = SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME;
                    tableIndices[idx] = tableIndex - 1;
                }
                else
                {
                    columnNames[idx] = SQLs.QNAME_TABLE_VALUE_COLUMN_NAME;
                    tableIndices[idx] = tableIndex;
                }
            }
        }

        if ( fromClause.length( ) > 0 )
        {
            orderByClause.append( "ORDER BY " );
            Boolean atLeastOneOBProcessed = false;
            for ( Integer idx = 0 ; idx < orderBy.length ; ++idx )
            {
                OrderBy ob = orderBy[idx];
                if ( ob != null )
                {
                    if ( atLeastOneOBProcessed )
                    {
                        orderByClause.append( ", " );
                    }
                    tableIndex = tableIndices[idx];
                    String columnName = columnNames[idx];
                    Order order = ob.order( );
                    String orderStr = order.equals( Order.ASCENDING ) ? "ASC" : "DESC";
                    orderByClause.append( TABLE_NAME_PREFIX + tableIndex + "." + columnName + " " + orderStr );
                    atLeastOneOBProcessed = true;
                }
            }
        }

    }

    private Integer traversePropertyPath( PropertyReference<?> reference, StringBuilder builder, Integer index, String joinStyle )
    {

        Stack<QualifiedName> qNameStack = new Stack<QualifiedName>( );
        Stack<PropertyReference<?>> refStack = new Stack<PropertyReference<?>>( );

        while ( reference != null )
        {
            qNameStack.add( QualifiedName.fromClass( reference.propertyDeclaringType( ), reference.propertyName( ) ) );
            refStack.add( reference );
            if ( reference.traversedProperty( ) == null && reference.traversedAssociation( ) != null )
            {
                index = this.traverseAssociationPath( reference.traversedAssociation( ), builder, index, joinStyle, true );
            }

            reference = reference.traversedProperty( );
        }

        PropertyReference<?> prevRef = null;
        String schemaName = this._state.schemaName( ).get( );
        while ( !qNameStack.isEmpty( ) )
        {
            QualifiedName qName = qNameStack.pop( );
            PropertyReference<?> ref = refStack.pop( );
            if ( !qName.type( ).equals( Identity.class.getName( ) ) )
            {
                QNameInfo info = this._state.qNameInfos( ).get( ).get( qName );
                if ( info == null )
                {
                    throw new InternalError( "No qName info found for qName [" + qName + "]." );
                }

                if ( prevRef == null )
                {
                    builder.append( //
//                        joinStyle + " " + schemaName + "." + SQLs.PROPERTY_QNAMES_TABLE_NAME + " " + TABLE_NAME_PREFIX + (index + 1) + "\n" + /**/
//                        "ON (" + TABLE_NAME_PREFIX + index + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + " = " + TABLE_NAME_PREFIX + ( index + 1 ) +
//                        "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + ")" + "\n" + /**/
                        joinStyle + " " + schemaName + "." + info.getTableName( ) + " " + TABLE_NAME_PREFIX + ( index + 1 ) + " " + /**/
                        "ON (" + TABLE_NAME_PREFIX + index + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + " = " + TABLE_NAME_PREFIX + ( index + 1 ) +
                        "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + " AND " + TABLE_NAME_PREFIX + ( index + 1 ) + "." + SQLs.QNAME_TABLE_PARENT_QNAME_COLUMN_NAME + " IS NULL" + ")" + "\n" /**/
                        );
                }
                else
                {
                    builder.append( //
                        joinStyle + " " + this._state.schemaName( ).get( ) + "." + info.getTableName( ) + " " + TABLE_NAME_PREFIX + ( index + 1 ) + " " + /**/
                            "ON (" + TABLE_NAME_PREFIX + index + "." + SQLs.ALL_QNAMES_TABLE_PK_COLUMN_NAME + " = " + TABLE_NAME_PREFIX + ( index + 1 ) +
                            "." + SQLs.QNAME_TABLE_PARENT_QNAME_COLUMN_NAME + " AND " + TABLE_NAME_PREFIX + index + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME +
                            " = " + TABLE_NAME_PREFIX + (index + 1) + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + ")\n" /**/
                        );
                }
                ++index;
                prevRef = ref;
            }
        }

        return index;
    }

    private Integer traverseAssociationPath(
        AssociationReference reference,
        StringBuilder builder,
        Integer index,
        String joinStyle,
        Boolean includeLastTable )
    {
        Stack<QualifiedName> qNameStack = new Stack<QualifiedName>( );
        while ( reference != null )
        {
            qNameStack.add( QualifiedName.fromClass( reference.associationDeclaringType( ), reference.associationName( ) ) );
            reference = reference.traversedAssociation( );
        }
        while ( !qNameStack.isEmpty( ) )
        {
            QualifiedName qName = qNameStack.pop( );
            QNameInfo info = this._state.qNameInfos( ).get( ).get( qName );
            if ( info == null )
            {
                throw new InternalError( "No qName info found for qName [" + qName + "]." );
            }
            builder.append( //
                joinStyle + " " + this._state.schemaName( ).get( ) + "." + info.getTableName( ) + " " + TABLE_NAME_PREFIX + ( index + 1 ) + " " + /**/
                "ON " + TABLE_NAME_PREFIX + index + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + " = " + TABLE_NAME_PREFIX + ( index + 1 ) + "." +
                SQLs.ENTITY_TABLE_PK_COLUMN_NAME + "\n" /**/
                );
            ++index;
            if ( !qNameStack.isEmpty( ) || includeLastTable )
            {
                builder.append( //
                    joinStyle + " " + this._state.schemaName( ).get( ) + "." + SQLs.ENTITY_TABLE_NAME + " " + TABLE_NAME_PREFIX + ( index + 1 ) + " " + /**/
                    "ON " + TABLE_NAME_PREFIX + index + "." + SQLs.QNAME_TABLE_VALUE_COLUMN_NAME + " = " + TABLE_NAME_PREFIX + ( index + 1 ) +
                    "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + "\n" /**/
                    );
                ++index;
            }
        }

        return index;
    }

    private List<Integer> getEntityTypeIDs( String entityType ) throws ClassNotFoundException
    {
        Class<?> entityClass = ( ( ModuleSPI ) this._module ).classLoader( ).loadClass( entityType );
        List<Integer> result = new ArrayList<Integer>( );
        for ( Map.Entry<String, EntityTypeInfo> entry : this._state.entityTypeInfos( ).get( ).entrySet( ) )
        {
            if ( entityClass.isAssignableFrom( entry.getValue( ).getEntityDescriptor( ).type( ) ) )
            {
                result.add( entry.getValue( ).getEntityTypePK( ) );
            }
        }

        return result;
    }

    private String getConcreteEntityTypesList( String entityType ) throws EntityFinderException
    {
        List<Integer> typeIDs = null;
        try
        {
            typeIDs = this.getEntityTypeIDs( entityType );
        }
        catch ( ClassNotFoundException cnfe )
        {
            throw new EntityFinderException( cnfe );
        }
        StringBuilder result = new StringBuilder( );
        Iterator<Integer> iter = typeIDs.iterator( );
        while ( iter.hasNext( ) )
        {
            result.append( iter.next( ) );
            if ( iter.hasNext( ) )
            {
                result.append( ", " );
            }
        }

        return result.toString( );
    }

    // TODO currently tableJoinStyle is not used
    private Integer modifyFromClauseAndWhereClauseToGetValue(
        final QualifiedName qName,
        Object value,
        final Predicate predicate,
        final Boolean negationActive,
        final Integer currentTableIndex,
        final ModifiableInt maxTableIndex,
        final String columnName,
        final String collectionPath,
        final StringBuilder whereClause,
        final StringBuilder afterWhere,
        final StringBuilder fromClause,
        final StringBuilder groupBy,
        final StringBuilder having,
        final List<QNameJoin> qNameJoins,
        final List<Object> values,
        final List<Integer> valueSQLTypes
        )
    {
        final String schemaName = this._state.schemaName( ).get( );
        Integer result = 1;
        if (value instanceof Collection<?>)
        {
            // Collection
            Integer collectionIndex = 0;
            Boolean collectionIsSet = value instanceof Set<?>;
            Boolean topLevel = collectionPath.equals( SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME );
            String collCol = TABLE_NAME_PREFIX + currentTableIndex + "." + SQLs.QNAME_TABLE_COLLECTION_PATH_COLUMN_NAME;
            if (topLevel)
            {
                if (whereClause.length( ) > 0)
                {
                    whereClause.append("AND" + " ");
                }
                whereClause.append("(");
                if (negationActive)
                {
                    afterWhere.append( " AND (" + collCol + " <> '" + SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME + "' OR " + collCol + " IS NULL)" + "\n" );
                }
            }
            Integer totalItemsProcessed = 0;
            for (Object item : (Collection<?>)value)
            {
                String path = collectionPath + SQLs.QNAME_TABLE_COLLECTION_PATH_SEPARATOR + (collectionIsSet ? "*{1,}" : collectionIndex);
                Boolean isCollection = (item instanceof Collection<?>);
                if (!isCollection)
                {
                    whereClause.append( "(" + collCol +  " ~ '" + path + "'"); // + (negationActive ? " OR " + collCol + " IS NULL": "") + ")" + "\n");
                }
                totalItemsProcessed = totalItemsProcessed + modifyFromClauseAndWhereClauseToGetValue( qName, item, predicate, negationActive, currentTableIndex, maxTableIndex, columnName, path, whereClause, afterWhere, fromClause, groupBy, having, qNameJoins, values, valueSQLTypes );
                if (!isCollection)
                {
                    whereClause.append( ")" );
                }
                ++collectionIndex;
                if (collectionIndex < ((Collection<?>)value).size( ))
                {
                    whereClause.append (" OR ");
                }
            }
            result = totalItemsProcessed;

            if (topLevel)
            {
                if (totalItemsProcessed == 0)
                {
                    whereClause.append( collCol + "IS NOT NULL AND " + collCol + " = " + SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME);
                }
                whereClause.append( ")" + "\n" );
                if (totalItemsProcessed > 0 && !negationActive)
                {
                    if (groupBy.length( ) > 0)
                    {
                        groupBy.append(", ");
                    }
                    if (having.length( ) > 0)
                    {
                        having.append( " AND " );
                    }
                    groupBy.append( TABLE_NAME_PREFIX + currentTableIndex + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME );
                    having.append( "COUNT(" + TABLE_NAME_PREFIX + currentTableIndex + "." + SQLs.QNAME_TABLE_VALUE_COLUMN_NAME + ") = " + totalItemsProcessed );
                }
            }

        } else if (value instanceof ValueComposite)
        {
            // Visit all properties with recursion and make joins as necessary
            ((ValueComposite)value).state( ).visitProperties( new StateVisitor( )
            {

                public void visitProperty( QualifiedName name, Object propertyValue )
                {

                    Boolean qNameJoinDone = false;
                    Integer sourceIndex = maxTableIndex.getInt( );
                    Integer targetIndex = sourceIndex + 1;
                    for (QNameJoin join : qNameJoins)
                    {
                        if (join.getSourceQName( ).equals( qName ))
                        {
                            sourceIndex = join.getSourceTableIndex( );
                            if (join.getTargetQName( ).equals( name ))
                            {
                                // This join has already been done once
                                qNameJoinDone = true;
                                targetIndex = join.getTargetTableIndex( );
                                break;
                            }
                        }
                    }

                    if (!qNameJoinDone)
                    {
                        QNameInfo info = _state.qNameInfos( ).get( ).get( name );
                        fromClause.append("LEFT JOIN" + " " + schemaName + "." + info.getTableName( ) + " " + TABLE_NAME_PREFIX + targetIndex + /**/
                            " ON (" + TABLE_NAME_PREFIX + sourceIndex + "." + SQLs.ALL_QNAMES_TABLE_PK_COLUMN_NAME + " = " + TABLE_NAME_PREFIX + targetIndex +
                            "." + SQLs.QNAME_TABLE_PARENT_QNAME_COLUMN_NAME + " AND " + TABLE_NAME_PREFIX + sourceIndex + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME +
                            " = " + TABLE_NAME_PREFIX + targetIndex + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + ")" + "\n"
                            );

                        qNameJoins.add( new QNameJoin( qName, name, sourceIndex, targetIndex ) );
                        maxTableIndex.setInt( maxTableIndex.getInt( ) + 1 );
                    }
                    modifyFromClauseAndWhereClauseToGetValue( name, propertyValue, predicate, negationActive, targetIndex, maxTableIndex, columnName, collectionPath, whereClause, afterWhere, fromClause, groupBy, having, qNameJoins, values, valueSQLTypes );
                }
            });

        } else
        {
            // Primitive
            String valueCol = TABLE_NAME_PREFIX + currentTableIndex + "." + columnName;
            if (value == null)
            {
                this.appendToWhereClause( whereClause, valueCol + " IS " + /* (negationActive ? "NOT " : "") +*/ "NULL", "AND" );
            } else
            {
                Object dbValue = value;
                if (Enum.class.isAssignableFrom( value.getClass( ) ))
                {
                    dbValue = this._state.enumPKs( ).get( ).get( value.getClass( ).getName( ) );
                }
                this.appendToWhereClause( whereClause, "(" + valueCol + " IS NOT NULL AND " + valueCol + " " + getOperator( predicate ) + " ?)" /* + (valueMayBeNull ? " OR " + valueCol + " IS NULL" : "") + ")"*/, "AND");
                values.add( dbValue );
                valueSQLTypes.add( _typeHelper.getSQLType( value ) );
                _log.info( TABLE_NAME_PREFIX + currentTableIndex + "." + columnName + " is " + dbValue );
            }
        }

        return result;
    }

    private void appendToWhereClause(StringBuilder clause, String content, String joiner)
    {
        if (clause.length( ) == 0)
        {
            clause.append( content + "\n");
        } else
        {
            clause.append( joiner + " " + content + "\n");
        }
    }

}
