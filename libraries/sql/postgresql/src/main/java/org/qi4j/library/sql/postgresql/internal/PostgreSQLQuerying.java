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

    private static Map<Class<? extends Predicate>, String> _negatedSqlOperators;

    private static Map<Class<? extends Predicate>, String> _joinStyles;

    private static Map<Class<? extends Predicate>, String> _negatedJoinStyles;

    private static final String TABLE_NAME_PREFIX = "t";

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

        _negatedSqlOperators = new HashMap<Class<? extends Predicate>, String>( );
        _negatedSqlOperators.put( EqualsPredicate.class, "<>" );
        _negatedSqlOperators.put( GreaterOrEqualPredicate.class, "<" );
        _negatedSqlOperators.put( GreaterThanPredicate.class, "<=" );
        _negatedSqlOperators.put( LessOrEqualPredicate.class, ">" );
        _negatedSqlOperators.put( LessThanPredicate.class, ">=" );
        _negatedSqlOperators.put( NotEqualsPredicate.class, "=" );
        _negatedSqlOperators.put( ManyAssociationContainsPredicate.class, "<>" );
        _negatedSqlOperators.put( MatchesPredicate.class, "!~" );

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
    }

    private interface WhereClauseProcessor
    {

        public String processWhereClause( Integer firstTableIndex, Integer lastTableIndex );
    }

    @Override
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

        String processedWhere = this.processBooleanExpression( whereClause, false, values, valueSQLTypes );
        if ( processedWhere == null || processedWhere.trim( ).length( ) == 0 )
        {
            processedWhere = this._state.schemaName( ).get( ) + "." + SQLs.ENTITY_TABLE_NAME;
        }
        else
        {
            processedWhere = "(" + processedWhere + ")";
        }

        StringBuilder fromClause = new StringBuilder( );
        StringBuilder orderByClause = new StringBuilder( );
        if ( orderBySegments != null )
        {
            this.processOrderBySegments( orderBySegments, fromClause, orderByClause );
        }
        String result = //
        "SELECT " + String.format( select, TABLE_NAME_PREFIX + "0." + SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME ) + "\n" +
            //
            "FROM " + processedWhere + " AS " + TABLE_NAME_PREFIX + "0" + "\n" +
            //
            fromClause.toString( ) + "\n" +
            //
            "WHERE " + this.createTypeCondition( TABLE_NAME_PREFIX + "0", this.getConcreteEntityTypesList( resultType ) ) + "\n" +
            orderByClause.toString( ) + //
            ";" //
        ;

        Logger.getLogger( this.getClass( ).getName( ) ).info( "SQL query:\n" + result );
        return result;
    }

    private String processBooleanExpression( BooleanExpression expression, Boolean negationActive, List<Object> values, List<Integer> valueSQLTypes )
    {
        String result = "";
        if ( expression != null )
        {
            if ( expression instanceof Conjunction )
            {
                Conjunction conjunction = ( Conjunction ) expression;
                String left = this.processBooleanExpression( conjunction.leftSideExpression( ), negationActive, values, valueSQLTypes );
                String right = this.processBooleanExpression( conjunction.rightSideExpression( ), negationActive, values, valueSQLTypes );
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
                String left = this.processBooleanExpression( disjunction.leftSideExpression( ), negationActive, values, valueSQLTypes );
                String right = this.processBooleanExpression( disjunction.rightSideExpression( ), negationActive, values, valueSQLTypes );
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
                result = this.processBooleanExpression( ( ( Negation ) expression ).expression( ), !negationActive, values, valueSQLTypes );
            }
            else if ( expression instanceof MatchesPredicate )
            {
                result = this.processMatchesPredicate( ( MatchesPredicate ) expression, negationActive, values, valueSQLTypes );
            }
            else if ( expression instanceof ComparisonPredicate<?> )
            {
                result = this.processComparisonPredicate( ( ComparisonPredicate<?> ) expression, negationActive, values, valueSQLTypes );
            }
            else if ( expression instanceof ManyAssociationContainsPredicate<?> )
            {
                result = this.processManyAssociationContainsPredicate(
                    ( ManyAssociationContainsPredicate<?> ) expression,
                    negationActive,
                    values,
                    valueSQLTypes );
            }
            else if ( expression instanceof PropertyNullPredicate<?> )
            {
                result = this.processPropertyNullPredicate( ( PropertyNullPredicate<?> ) expression, negationActive );
            }
            else if ( expression instanceof AssociationNullPredicate )
            {
                result = this.processAssociationNullPredicate( ( AssociationNullPredicate ) expression, negationActive );
            }
            else if ( expression instanceof ContainsPredicate<?, ?> )
            {
                result = this.processContainsPredicate( ( ContainsPredicate<?, ?> ) expression, negationActive, values, valueSQLTypes );
            }
            else if ( expression instanceof ContainsAllPredicate<?, ?> )
            {
                result = this.processContainsAllPredicate( ( ContainsAllPredicate<?, ?> ) expression, negationActive, values, valueSQLTypes );
            }
            else
            {
                throw new UnsupportedOperationException( "Expression " + expression + " is not supported" );
            }
        }

        return result;
    }

    private String processMatchesPredicate(
        final MatchesPredicate predicate,
        final Boolean negationActive,
        final List<Object> values,
        final List<Integer> valueSQLTypes )
    {
        return this.constructQueryForPredicate( //
            predicate, //
            predicate.propertyReference( ), //
            null, //
            null, //
            negationActive, //
            new WhereClauseProcessor( )
            {

                @Override
                public String processWhereClause( Integer firstTableIndex, Integer lastTableIndex )
                {
                    String result = String.format(
                        TABLE_NAME_PREFIX + lastTableIndex + "." + SQLs.QNAME_TABLE_VALUE_COLUMN_NAME + " %s ?",
                        getOperator( predicate, negationActive ) );
                    values.add( translateJavaRegexpToPGSQLRegexp( ( ( SingleValueExpression<String> ) predicate.valueExpression( ) ).value( ) ) );
                    valueSQLTypes.add( Types.VARCHAR );
                    return result;
                }
            } //
            );
    }

    private String processComparisonPredicate(
        final ComparisonPredicate<?> predicate,
        final Boolean negationActive,
        final List<Object> values,
        final List<Integer> valueSQLTypes )
    {
        return this.constructQueryForPredicate( //
            predicate, //
            predicate.propertyReference( ), //
            null, //
            null, //
            negationActive, //
            new WhereClauseProcessor( )
            {

                @Override
                public String processWhereClause( Integer firstTableIndex, Integer lastTableIndex )
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
                    StringBuilder builder = new StringBuilder( );
                    modifyFromClauseAndWhereClauseToGetValue(
                        value,
                        predicate,
                        negationActive,
                        TABLE_NAME_PREFIX + lastTableIndex,
                        columnName,
                        "AND",
                        builder,
                        values,
                        valueSQLTypes );
                    return builder.toString( );
                }
            } //
            );
    }

    private String processManyAssociationContainsPredicate(
        final ManyAssociationContainsPredicate<?> predicate,
        final Boolean negationActive,
        final List<Object> values,
        final List<Integer> valueSQLTypes )
    {
        return this.constructQueryForPredicate( //
            predicate, //
            null, //
            predicate.associationReference( ), //
            true, //
            negationActive, //
            new WhereClauseProcessor( )
            {

                @Override
                public String processWhereClause( Integer firstTableIndex, Integer lastTableIndex )
                {
                    String result = TABLE_NAME_PREFIX + lastTableIndex + "." + SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME + " " +
                        getOperator( predicate, negationActive ) + " ? " + "\n"; //
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
                    return result;
                }
            } //
            );
    }

    private String processPropertyNullPredicate( final PropertyNullPredicate<?> predicate, final Boolean negationActive )
    {
        return this.constructQueryForPredicate( //
            predicate, //
            predicate.propertyReference( ), //
            null, //
            null, //
            negationActive, //
            new WhereClauseProcessor( )
            {

                @Override
                public String processWhereClause( Integer firstTableIndex, Integer lastTableIndex )
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

                    String result = "";
                    if ((predicate instanceof PropertyIsNullPredicate<?> && !negationActive)
                        || (predicate instanceof PropertyIsNotNullPredicate<?> && negationActive))
                    {
                        // Last table column might be null because of left joins
                        result = TABLE_NAME_PREFIX + lastTableIndex + "." + colName + " IS NULL" + "\n";
                    }
                    return result;
                }
            } //
            );
    }

    private String processAssociationNullPredicate( final AssociationNullPredicate predicate, final Boolean negationActive )
    {
        return this.constructQueryForPredicate( //
            predicate, //
            null, //
            predicate.associationReference( ), //
            false, //
            negationActive, //
            new WhereClauseProcessor( )
            {

                @Override
                public String processWhereClause( Integer firstTableIndex, Integer lastTableIndex )
                {
                    String result = "";
                    if ((predicate instanceof AssociationIsNullPredicate && !negationActive)
                        || (predicate instanceof AssociationIsNotNullPredicate && negationActive))
                    {
                        // Last table column might be null because of left joins
                        result = TABLE_NAME_PREFIX + lastTableIndex + "." + SQLs.QNAME_TABLE_VALUE_COLUMN_NAME + " IS NULL" + "\n"; //
                    }
                    return result;
                }
            } //
            );
    }

    private String processContainsPredicate(
        final ContainsPredicate<?, ? extends Collection<?>> predicate,
        final Boolean negationActive,
        final List<Object> values,
        final List<Integer> valueSQLTypes )
    {
        // Path: Top.* (star without braces), value = value
        // ASSUMING value is NOT collection (ie, find all entities, which collection property has value x as leaf item, no matter collection depth)
        return this.constructQueryForPredicate( //
            predicate, //
            predicate.propertyReference( ), //
            null, //
            null, //
            negationActive, //
            new WhereClauseProcessor( )
            {

                @Override
                public String processWhereClause( Integer firstTableIndex, Integer lastTableIndex )
                {
                    StringBuilder result = new StringBuilder(TABLE_NAME_PREFIX + lastTableIndex + "." + SQLs.QNAME_TABLE_COLLECTION_PATH_COLUMN_NAME + " ~ '" +
                        SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME + ".*'");
                    Object value = ((SingleValueExpression<?>)predicate.valueExpression( )).value( );
                    if (value instanceof Collection<?>)
                    {
                        throw new IllegalArgumentException( "ContainsPredicate may have only either primitive or value composite as value." );
                    }
                    modifyFromClauseAndWhereClauseToGetValue(
                        value,
                        predicate,
                        negationActive,
                        TABLE_NAME_PREFIX + lastTableIndex,
                        SQLs.QNAME_TABLE_VALUE_COLUMN_NAME,
                        "AND",
                        result,
                        values,
                        valueSQLTypes
                        );
                    return result.toString( );
                }
            } //
            );
    }

    private String processContainsAllPredicate(
        final ContainsAllPredicate<?, ? extends Collection<?>> predicate,
        final Boolean negationActive,
        final List<Object> values,
        final List<Integer> valueSQLTypes )
    {
        // has all leaf items in specified collection

        return this.constructQueryForPredicate( //
            predicate, //
            predicate.propertyReference( ), //
            null, //
            null, //
            negationActive, //
            new WhereClauseProcessor( )
            {

                @Override
                public String processWhereClause( Integer firstTableIndex, Integer lastTableIndex )
                {
                    StringBuilder itemsClause = new StringBuilder();
                    Collection<?> collection = (Collection<?>)((SingleValueExpression<?>)predicate.valueExpression( )).value( );
                    for (Object value : collection)
                    {
                        if (value instanceof Collection<?>)
                        {
                            throw new IllegalArgumentException( "ContainsAllPredicate may not have nested collections as value." );
                        }
                        modifyFromClauseAndWhereClauseToGetValue(
                            value,
                            predicate,
                            negationActive,
                            TABLE_NAME_PREFIX + lastTableIndex,
                            SQLs.QNAME_TABLE_VALUE_COLUMN_NAME,
                            "OR",
                            itemsClause,
                            values,
                            valueSQLTypes
                            );
                    }
                    StringBuilder result = new StringBuilder(TABLE_NAME_PREFIX + lastTableIndex + "." + SQLs.QNAME_TABLE_COLLECTION_PATH_COLUMN_NAME + " ~ '" +
                    SQLs.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME + ".*{1,}'");
                    if (itemsClause.length( ) > 0)
                    {
                        result.append( " AND (" + itemsClause.toString( ) + ")" );
                    }

                    return result.toString( );
                }
            } //
            );
    }

    private String constructQueryForPredicate(
        Predicate predicate,
        PropertyReference<?> propRef,
        AssociationReference assoRef,
        Boolean includeLastAssoPathTable,
        Boolean negationActive,
        WhereClauseProcessor whereClauseGenerator )
    {
        StringBuilder builder = new StringBuilder( );
        Integer startingIndex = 0;
        this.getSelectClauseForPredicate( predicate, TABLE_NAME_PREFIX + startingIndex, builder );
        builder.append( "FROM " + this._state.schemaName( ).get( ) + "." + SQLs.ENTITY_TABLE_NAME + " " + TABLE_NAME_PREFIX + startingIndex + "\n" );
        Integer lastTableIndex = null;
        if ( propRef == null )
        {
            lastTableIndex = this.traverseAssociationPath(
                assoRef,
                builder,
                startingIndex,
                this.getJoinStyle( predicate, negationActive ),
                includeLastAssoPathTable );
        }
        else if ( assoRef == null )
        {
            lastTableIndex = this.traversePropertyPath( propRef, builder, startingIndex, this.getJoinStyle( predicate, negationActive ) );
        }
        else
        {
            throw new InternalError( "Can not have both property reference and association reference (non-)nulls [propRef=" + propRef + ", assoRef=" +
                assoRef + ", predicate=" + predicate + "]." );
        }

        String whereClause = whereClauseGenerator.processWhereClause( startingIndex, lastTableIndex );
        if ( whereClause != null && whereClause.trim( ).length( ) > 0 )
        {
            builder.append( "WHERE " ).append( whereClause ).append( "\n" );
        }
        return builder.toString( );
    }

    private String getOperator( Predicate predicate, Boolean negationActive )
    {
        return this.findFromLookupTables( _sqlOperators, _negatedSqlOperators, predicate, negationActive );
    }

    private String getJoinStyle( Predicate predicate, Boolean negationActive )
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

    private void getSelectClauseForPredicate( Predicate predicate, String tableAlias, StringBuilder builder )
    {
        builder.append( "SELECT DISTINCT " + tableAlias + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + ", " + tableAlias + "." +
            SQLs.ENTITY_TYPES_TABLE_PK_COLUMN_NAME + ", " + tableAlias + "." + SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME + "\n" );
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
                        joinStyle + " " + schemaName + "." + info.getTableName( ) + " " + TABLE_NAME_PREFIX + ( index + 1 ) + "\n" + /**/
                        "ON (" + TABLE_NAME_PREFIX + index + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + " = " + TABLE_NAME_PREFIX + ( index + 1 ) +
                        "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + " AND " + TABLE_NAME_PREFIX + ( index + 1 ) + "." + SQLs.QNAME_TABLE_PARENT_QNAME_COLUMN_NAME + " IS NULL" + ")" + "\n" /**/
                        );
                }
                else
                {
                    builder.append( //
                        joinStyle + " " + this._state.schemaName( ).get( ) + "." + info.getTableName( ) + " " + TABLE_NAME_PREFIX + ( index + 1 ) +
                            "\n" + /**/
                            "ON " + TABLE_NAME_PREFIX + index + "." + SQLs.ALL_QNAMES_TABLE_PK_COLUMN_NAME + " = " + TABLE_NAME_PREFIX + ( index + 1 ) +
                            "." + SQLs.QNAME_TABLE_PARENT_QNAME_COLUMN_NAME + "\n" /**/
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
                joinStyle + " " + this._state.schemaName( ).get( ) + "." + info.getTableName( ) + " " + TABLE_NAME_PREFIX + ( index + 1 ) + "\n" + /**/
                "ON " + TABLE_NAME_PREFIX + index + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + " = " + TABLE_NAME_PREFIX + ( index + 1 ) + "." +
                    SQLs.ALL_QNAMES_TABLE_PK_COLUMN_NAME + "\n" /**/
                );
            ++index;
            if ( !qNameStack.isEmpty( ) || includeLastTable )
            {
                builder.append( //
                    joinStyle + " " + this._state.schemaName( ).get( ) + "." + SQLs.ENTITY_TABLE_NAME + " " + TABLE_NAME_PREFIX + ( index + 1 ) +
                        "\n" + /**/
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

    private void buildWhereClauseForContainsAllPredicate(Predicate predicate, Boolean negationActive, Object value, String path, StringBuilder builder, String conditionJoiner, String tableName)
    {
        if (value instanceof Collection<?>)
        {
            Collection<?> collection = (Collection<?>)value;
            Integer index = 0;
            for (Object o : collection)
            {
                String newPath = path + SQLs.QNAME_TABLE_COLLECTION_PATH_SEPARATOR;
                if (collection instanceof Set<?>)
                {
                    newPath = newPath + "*{1}";
                } else
                {
                    newPath = newPath + index;
                }
                this.buildWhereClauseForContainsAllPredicate( predicate, negationActive, o, newPath, builder, conditionJoiner, tableName );
                ++index;
            }
        } else
        {
            if (builder.length( ) > 0)
            {
                builder.append( conditionJoiner );
            }
            builder.append( "(" + tableName + "." + SQLs.QNAME_TABLE_COLLECTION_PATH_COLUMN_NAME + " ~ '" );
        }
    }

    private void modifyFromClauseAndWhereClauseToGetValue(Object value, Predicate predicate, Boolean negationActive, String tableName, String columnName, String joiner, StringBuilder whereClause, List<Object> values, List<Integer> valueSQLTypes)
    {
        if (value instanceof Collection<?>)
        {
            // Collection
            throw new UnsupportedOperationException( "Comparing values for collections is not yet supported." );

        } else if (value instanceof ValueComposite)
        {
            // Value composite
            ((ValueComposite)value).state( ).visitProperties( new StateVisitor( )
            {

                @Override
                public void visitProperty( QualifiedName name, Object value )
                {
                    // TODO extra joins
                    throw new UnsupportedOperationException( "Comparing values for value composites is not yet supported." );
                }
            });
        } else
        {
            // Primitive
            this.appendToWhereClause( whereClause, tableName + "." + columnName + " " + getOperator( predicate, negationActive ) + " ?", joiner);
            values.add( value );
            valueSQLTypes.add( _typeHelper.getSQLType( value ) );
        }
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

    private void modifySelectClauseForValueCompositeValue(ValueComposite value, StringBuilder fromClause, StringBuilder whereClause, Boolean shouldEqual)
    {
        value.state( ).visitProperties( new StateVisitor( )
        {

            @Override
            public void visitProperty( QualifiedName name, Object value )
            {

            }
        });
    }

}
