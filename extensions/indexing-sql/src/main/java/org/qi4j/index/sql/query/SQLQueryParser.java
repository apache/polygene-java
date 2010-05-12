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


package org.qi4j.index.sql.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
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
import org.qi4j.api.query.grammar.ValueExpression;
import org.qi4j.api.query.grammar.OrderBy.Order;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.index.sql.common.EntityTypeInfo;
import org.qi4j.index.sql.common.QNameInfo;
import org.qi4j.index.sql.common.SQLIndexingState;
import org.qi4j.index.sql.startup.SQLs;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.spi.query.EntityFinderException;

/**
 * This is the interface responsible of transforming a Qi4j query into a SQL query.
 * 
 * Currently, the main principles are these: 1. For every association traverse in referenced property/association, there is
 * at least one {@code (LEFT) JOIN}, usually two. There is one join ONLY at final join of {@link ManyAssociationContainsPredicate} association reference.
 * 2. For every property traverse in referenced property/association, there is at at most one {@code (LEFT) JOIN}. There is zero joins ONLY when property
 * reference is to {@link Identity#identity()}. These join numbers also include all association and property traverses in all {@link OrderBy} elements.
 * 
 * Even though the amount of joins may seem big, this query parser always produces exactly one SQL query for exactly one Qi4j query. So, it scales ok
 * in that sense.
 * 
 * For predicate logic, {@link Conjunction} transforms into SQL {@code INTERSECT} and {@link Disjunction} transforms into SQL {@code UNION}. {@link Negation}
 * usually simply changes the SQL operator in {@code WHERE} clause.
 * 
 * Currently, the transformation from Qi4j queries to SQL queries is NOT implemented for following predicates: {@link MatchesPredicate}, {@link ContainsPredicate}, and {@link ContainsAllPredicate}.
 * 
 * @author Stanislav Muhametsin
 */
public interface SQLQueryParser
{
   String getQuery( //
         String resultType, //
         @Optional BooleanExpression whereClause, //
         @Optional OrderBy[] orderBySegments, //
         @Optional Integer firstResult, //
         @Optional Integer maxResults, //
         List<Object> values, //
         Boolean countOnly //
   ) throws EntityFinderException;
   
   public class SQLQueryParserImpl implements SQLQueryParser
   {
      
      @This private SQLIndexingState _state;
      
      @Structure private UnitOfWorkFactory _uowf;
      
      private static Map<Class<? extends Predicate>, String> _sqlOperators;
      
      private static Map<Class<? extends Predicate>, String> _negatedSqlOperators;
      
      private static Map<Class<? extends Predicate>, String> _joinStyles;
      
      private static Map<Class<? extends Predicate>, String> _negatedJoinStyles;
      
      private static final String TABLE_NAME_PREFIX = "t";
      
      static
      {
         _sqlOperators = new HashMap<Class<? extends Predicate>, String>();
         _sqlOperators.put(EqualsPredicate.class, "=");
         _sqlOperators.put(GreaterOrEqualPredicate.class, ">=");
         _sqlOperators.put(GreaterThanPredicate.class, ">");
         _sqlOperators.put(LessOrEqualPredicate.class, "<=");
         _sqlOperators.put(LessThanPredicate.class, "<");
         _sqlOperators.put(NotEqualsPredicate.class, "<>");
         _sqlOperators.put(PropertyIsNullPredicate.class, "IS");
         _sqlOperators.put(PropertyIsNotNullPredicate.class, "IS NOT");
         _sqlOperators.put(AssociationIsNullPredicate.class, "IS");
         _sqlOperators.put(AssociationIsNotNullPredicate.class, "IS NOT");
         _sqlOperators.put(ManyAssociationContainsPredicate.class, "=");
         
         _negatedSqlOperators = new HashMap<Class<? extends Predicate>, String>();
         _negatedSqlOperators.put(EqualsPredicate.class, "<>");
         _negatedSqlOperators.put(GreaterOrEqualPredicate.class, "<");
         _negatedSqlOperators.put(GreaterThanPredicate.class, "<=");
         _negatedSqlOperators.put(LessOrEqualPredicate.class, ">");
         _negatedSqlOperators.put(LessThanPredicate.class, ">=");
         _negatedSqlOperators.put(NotEqualsPredicate.class, "=");
         _negatedSqlOperators.put(PropertyIsNullPredicate.class, "IS NOT");
         _negatedSqlOperators.put(PropertyIsNotNullPredicate.class, "IS");
         _negatedSqlOperators.put(AssociationIsNullPredicate.class, "IS NOT");
         _negatedSqlOperators.put(AssociationIsNotNullPredicate.class, "IS");
         _negatedSqlOperators.put(ManyAssociationContainsPredicate.class, "<>");
         
         _joinStyles = new HashMap<Class<? extends Predicate>, String>();
         _joinStyles.put(EqualsPredicate.class, "JOIN");
         _joinStyles.put(GreaterOrEqualPredicate.class, "JOIN");
         _joinStyles.put(GreaterThanPredicate.class, "JOIN");
         _joinStyles.put(LessOrEqualPredicate.class, "JOIN");
         _joinStyles.put(LessThanPredicate.class, "JOIN");
         _joinStyles.put(NotEqualsPredicate.class, "JOIN");
         _joinStyles.put(PropertyIsNullPredicate.class, "LEFT JOIN");
         _joinStyles.put(PropertyIsNotNullPredicate.class, "JOIN");
         _joinStyles.put(AssociationIsNullPredicate.class, "LEFT JOIN");
         _joinStyles.put(AssociationIsNotNullPredicate.class, "JOIN");
         _joinStyles.put(ManyAssociationContainsPredicate.class, "JOIN");
         
         _negatedJoinStyles = new HashMap<Class<? extends Predicate>, String>();
         _negatedJoinStyles.put(EqualsPredicate.class, "JOIN");
         _negatedJoinStyles.put(GreaterOrEqualPredicate.class, "JOIN");
         _negatedJoinStyles.put(GreaterThanPredicate.class, "JOIN");
         _negatedJoinStyles.put(LessOrEqualPredicate.class, "JOIN");
         _negatedJoinStyles.put(LessThanPredicate.class, "JOIN");
         _negatedJoinStyles.put(NotEqualsPredicate.class, "JOIN");
         _negatedJoinStyles.put(PropertyIsNullPredicate.class, "JOIN");
         _negatedJoinStyles.put(PropertyIsNotNullPredicate.class, "LEFT JOIN");
         _negatedJoinStyles.put(AssociationIsNullPredicate.class, "JOIN");
         _negatedJoinStyles.put(AssociationIsNotNullPredicate.class, "LEFT JOIN");
         _negatedJoinStyles.put(ManyAssociationContainsPredicate.class, "JOIN");
      }
      
      @Override
      public String getQuery(
            String resultType, //
            BooleanExpression whereClause, //
            OrderBy[] orderBySegments, //
            Integer firstResult, //
            Integer maxResults, //
            List<Object> values, //
            Boolean countOnly //
            ) throws EntityFinderException
      {
         String select = countOnly ? "COUNT(%s)" : "%s"; 

         String entityTypeIDs = this.getConcreteEntityTypesList(resultType);
         String processedWhere = this.processWhereClause(whereClause, false, values, entityTypeIDs);
         Boolean acceptAll = false;
         if (processedWhere == null || processedWhere.trim().length() == 0)
         {
            processedWhere = this._state.schemaName().get() + "." + SQLs.ENTITY_TABLE_NAME;
            acceptAll = true;
         } else
         {
            processedWhere = "(" + processedWhere + ")";
         }
         
         StringBuilder fromClause = new StringBuilder();
         StringBuilder orderByClause = new StringBuilder();
         if (orderBySegments != null)
         {
            this.processOrderBySegments(orderBySegments, fromClause, orderByClause);
         }
         String result = //
         "SELECT " + String.format(select, TABLE_NAME_PREFIX + "0." + SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME) + "\n" + //
         "FROM " + processedWhere + " AS " + TABLE_NAME_PREFIX + "0" + "\n" + //
         fromClause.toString() + "\n" + //
         (acceptAll ? "WHERE " + this.createTypeCondition(TABLE_NAME_PREFIX + "0", entityTypeIDs) + "\n" : "") + //
         orderByClause.toString() + //
         ";" //
         ;
         
         Logger.getLogger(this.getClass().getName()).info("SQL query:\n" + result);
         return result;
      }
      
      private String processWhereClause(BooleanExpression expression, Boolean negationActive, List<Object> values, String entityTypeIDs)
      {
         String result = "";
         if (expression != null)
         {
            if (expression instanceof Conjunction)
            {
               Conjunction conjunction = (Conjunction)expression;
               String left = this.processWhereClause(conjunction.leftSideExpression(), negationActive, values, entityTypeIDs);
               String right = this.processWhereClause(conjunction.rightSideExpression(), negationActive, values, entityTypeIDs);
               if (left == "")
               {
                  result = right;
               } else if (right == "")
               {
                  result = left;
               } else
               {
                  result = String.format("(%s)\nINTERSECT\n(%s)", left, right);
               }
            } else if (expression instanceof Disjunction)
            {
               Disjunction disjunction = (Disjunction)expression;
               String left = this.processWhereClause(disjunction.leftSideExpression(), negationActive, values, entityTypeIDs);
               String right = this.processWhereClause(disjunction.rightSideExpression(), negationActive, values, entityTypeIDs);
               if (left == "")
               {
                  result = right;
               } else if (right == "")
               {
                  result = left;
               } else
               {
                  result = String.format("(%s)\nUNION\n(%s)", left, right);
               }
            } else if (expression instanceof Negation)
            {
               result = this.processWhereClause(((Negation)expression).expression(), !negationActive, values, entityTypeIDs);
            } else if (expression instanceof MatchesPredicate)
            {
               result = this.processMatchesPredicate((MatchesPredicate)expression, negationActive, values, entityTypeIDs);
            } else if (expression instanceof ComparisonPredicate<?>)
            {
               result = this.processComparisonPredicate((ComparisonPredicate<?>)expression, negationActive, values, entityTypeIDs);
            } else if (expression instanceof ManyAssociationContainsPredicate<?>)
            {
               result = this.processManyAssociationContainsPredicate((ManyAssociationContainsPredicate<?>)expression, negationActive, values, entityTypeIDs);
            } else if (expression instanceof PropertyNullPredicate<?>)
            {
               result = this.processPropertyNullPredicate((PropertyNullPredicate<?>)expression, negationActive, values, entityTypeIDs);
            } else if (expression instanceof AssociationNullPredicate)
            {
               result = this.processAssociationNullPredicate((AssociationNullPredicate)expression, negationActive, values, entityTypeIDs);
            } else if (expression instanceof ContainsPredicate<?, ?>)
            {
               result = this.processContainsPredicate((ContainsPredicate<?, ?>)expression, negationActive, values, entityTypeIDs);
            } else if (expression instanceof ContainsAllPredicate<?, ?>)
            {
               result = this.processContainsAllPredicate((ContainsAllPredicate<?, ?>)expression, negationActive, values, entityTypeIDs);
            } else
            {
               throw new UnsupportedOperationException("Expression " + expression + " is not supported");
            }
         }
         
         return result;
      }
      
      private String processMatchesPredicate(MatchesPredicate predicate, Boolean negationActive, List<Object> values, String entityTypeIDs)
      {
         // Not sure how this is supported by SQL yet
         throw new UnsupportedOperationException("Regexp matching is not yet (?) supported by SQL query parser.");
      }
      
      private String processComparisonPredicate(ComparisonPredicate<?> predicate, Boolean negationActive, List<Object> values, String entityTypeIDs)
      {
         StringBuilder builder = new StringBuilder();
         Integer startingIndex = 0;
         this.getSelectClauseForPredicate(predicate, TABLE_NAME_PREFIX + startingIndex, builder);
         builder.append("FROM " + this._state.schemaName().get() + "." + SQLs.ENTITY_TABLE_NAME + " " + TABLE_NAME_PREFIX + startingIndex + "\n");
         Integer lastTableIndex = this.traversePropertyPath(predicate.propertyReference(), builder, startingIndex, this.getJoinStyle(predicate, negationActive));
         this.processWhereClauseForPropertyComparisonPredicate(predicate, predicate.propertyReference(), negationActive, lastTableIndex, builder);
         builder.append("AND " + this.createTypeCondition(TABLE_NAME_PREFIX + startingIndex, entityTypeIDs));
         // TODO comparison of collection properties??
         values.add(((SingleValueExpression<?>)predicate.valueExpression()).value());
         return builder.toString();
      }
      
      private String processManyAssociationContainsPredicate(ManyAssociationContainsPredicate<?> predicate, Boolean negationActive, List<Object> values, String entityTypeIDs)
      {
         StringBuilder builder = new StringBuilder();
         Integer startingIndex = 0;
         this.getSelectClauseForPredicate(predicate, TABLE_NAME_PREFIX + startingIndex, builder);
         builder.append("FROM " + this._state.schemaName().get() + "." + SQLs.ENTITY_TABLE_NAME + " " + TABLE_NAME_PREFIX + startingIndex + "\n");
         Integer lastTableIndex = this.traverseAssociationPath(predicate.associationReference(), builder, startingIndex, this.getJoinStyle(predicate, negationActive), true);
         builder.append("WHERE " + this.createTypeCondition(TABLE_NAME_PREFIX + startingIndex, entityTypeIDs) + "\n" + //
               "AND " + TABLE_NAME_PREFIX + lastTableIndex + "." + SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME + " " + this.getOperator(predicate, negationActive) + " ? " + "\n" //
               );
         Object value = ((SingleValueExpression<?>)predicate.valueExpression()).value();
         // TODO can I be certain that this value is always instance of EntityComposite?
         if (value instanceof EntityComposite)
         {
            System.out.println("WAS !!!!!!! EntityComposite");
            value = this._uowf.currentUnitOfWork().get((EntityComposite)value).identity().get();
         } else if (value instanceof EntityInstance)
         {
            System.out.println("WAS !!!! EntityInstance");
            value = ((EntityInstance)value).identity().identity();
         } else if (value instanceof EntityReference)
         {
            System.out.println("WAS !!!!!! EntityReference");
            value = ((EntityReference)value).identity();
         } else
         {
            System.out.println("WAS !!!!!!!!!!! UNKNOWN!!! [" + value.getClass() + "]");
            value = value.toString();
         }
         values.add(value);
         return builder.toString();
      }
      
      private String processPropertyNullPredicate(PropertyNullPredicate<?> predicate, Boolean negationActive, List<Object> values, String entityTypeIDs)
      {
         StringBuilder builder = new StringBuilder();
         Integer startingIndex = 0;
         this.getSelectClauseForPredicate(predicate, TABLE_NAME_PREFIX + startingIndex, builder);
         builder.append("FROM " + this._state.schemaName().get() + "." + SQLs.ENTITY_TABLE_NAME + " " + TABLE_NAME_PREFIX + startingIndex + "\n");
         Integer lastTableIndex = this.traversePropertyPath(predicate.propertyReference(), builder, startingIndex, this.getJoinStyle(predicate, negationActive));
         builder.append("WHERE " + this.createTypeCondition(TABLE_NAME_PREFIX + startingIndex, entityTypeIDs) + "\n" + //
               "AND " + TABLE_NAME_PREFIX + lastTableIndex + "." + SQLs.QNAME_TABLE_VALUE_COLUMN_NAME + " " + this.getOperator(predicate, negationActive) + " NULL" + "\n" //
               );
         this.processWhereclauseForPropertyNullPredicate(predicate, negationActive, predicate.propertyReference(), builder, TABLE_NAME_PREFIX + lastTableIndex);
         return builder.toString();
      }
      
      private String processAssociationNullPredicate(AssociationNullPredicate predicate, Boolean negationActive, List<Object> values, String entityTypeIDs)
      {
         StringBuilder builder = new StringBuilder();
         Integer startingIndex = 0;
         this.getSelectClauseForPredicate(predicate, TABLE_NAME_PREFIX + startingIndex, builder);
         builder.append("FROM " + this._state.schemaName().get() + "." + SQLs.ENTITY_TABLE_NAME + " " + TABLE_NAME_PREFIX + startingIndex + "\n");
         Integer lastTableIndex = this.traverseAssociationPath(predicate.associationReference(), builder, startingIndex, this.getJoinStyle(predicate, negationActive), false);
         builder.append("WHERE " + this.createTypeCondition(TABLE_NAME_PREFIX + startingIndex, entityTypeIDs) + "\n" + //
               "AND " + TABLE_NAME_PREFIX + lastTableIndex + "." + SQLs.QNAME_TABLE_VALUE_COLUMN_NAME + " " + this.getOperator(predicate, negationActive) + " NULL" + "\n" //
               );
         return builder.toString();
      }
      
      private String processContainsPredicate(ContainsPredicate<?, ? extends Collection<?>> predicate, Boolean negationActive, List<Object> values, String entityTypeIDs)
      {
         throw new UnsupportedOperationException("Predicate " + predicate + " is not supported");
      }
      
      private String processContainsAllPredicate(ContainsAllPredicate<?, ? extends Collection<?>> predicate, Boolean negationActive, List<Object> values, String entityTypeIDs)
      {
         throw new UnsupportedOperationException("Predicate " + predicate + " is not supported");
      }
      
      private String getOperator(Predicate predicate, Boolean negationActive )
      {
         return this.findFromLookupTables(_sqlOperators, _negatedSqlOperators, predicate, negationActive);
      }
      
      private String getJoinStyle(Predicate predicate, Boolean negationActive)
      {
         return this.findFromLookupTables(_joinStyles, _negatedJoinStyles, predicate, negationActive);
      }
      
      private String findFromLookupTables(Map<Class<? extends Predicate>, String> normal, Map<Class<? extends Predicate>, String> negated, Predicate predicate, Boolean negationActive)
      {
         Class<? extends Predicate> predicateClass = predicate.getClass();
         String result = null;
         Set<Map.Entry<Class<? extends Predicate>, String>> entries = negationActive ? negated.entrySet() : normal.entrySet();
         for( Map.Entry<Class<? extends Predicate>, String> entry : entries )
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
      
      private void getSelectClauseForPredicate(Predicate predicate, String tableAlias, StringBuilder builder)
      {
         builder.append("SELECT " + tableAlias + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + ", " + tableAlias + "." + SQLs.ENTITY_TYPES_TABLE_PK_COLUMN_NAME + ", " + tableAlias + "." + SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME + "\n");
      }
      
      private void processWhereClauseForPropertyComparisonPredicate(Predicate predicate, PropertyReference<?> reference, Boolean negationActive, Integer tableIndex, StringBuilder builder)
      {
         QualifiedName qName = QualifiedName.fromClass(reference.propertyDeclaringType(), reference.propertyName());
         String str = null;
         if (!qName.type().equals(Identity.class.getName()))
         {
            str = "WHERE " + TABLE_NAME_PREFIX + tableIndex + "." + SQLs.QNAME_TABLE_VALUE_COLUMN_NAME + " %s ?" + "\n";
         } else
         {
            str = "WHERE " + TABLE_NAME_PREFIX + tableIndex + "." + SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME + " %s ?" + "\n";
         }
         builder.append(
               String.format(str, this.getOperator(predicate, negationActive))
              );
      }
      
      private void processWhereclauseForPropertyNullPredicate(Predicate predicate, Boolean negationActive, PropertyReference<?> reference, StringBuilder builder, String tableName)
      {
         QualifiedName qName = QualifiedName.fromClass(reference.propertyDeclaringType(), reference.propertyName());
         Integer collectionDepth = this._state.qNameInfos().get().get(qName).getCollectionDepth();
         for (Integer x = 0; x < collectionDepth; ++x)
         {
            builder.append("AND " + tableName + "." + SQLs.QNAME_TABLE_COLLECTION_INDEX_COLUMN_NAME_PREFIX + x + " " + this.getOperator(predicate, negationActive) + " NULL" + "\n");
         }
      }
     
      private String createTypeCondition(String tableAlias, String entityTypeIDs)
      {
         return tableAlias + "." + SQLs.ENTITY_TYPES_TABLE_PK_COLUMN_NAME + " IN (" + entityTypeIDs + ")";
      }
      
      private void processOrderBySegments(OrderBy[] orderBy, StringBuilder fromClause, StringBuilder orderByClause)
      {
         QNameInfo[] qNames = new QNameInfo[orderBy.length];
         Integer[] tableIndices = new Integer[orderBy.length];
         String[] columnNames = new String[orderBy.length];
         
         Integer tableIndex = 0;
         for (Integer idx = 0; idx < orderBy.length; ++idx)
         {
            if (orderBy[idx] != null)
            {
               PropertyReference<?> ref = orderBy[idx].propertyReference();
               QualifiedName qName = QualifiedName.fromClass(ref.propertyDeclaringType(), ref.propertyName()); 
               QNameInfo info = this._state.qNameInfos().get().get(qName);
               qNames[idx] = info;
               if (info == null)
               {
                  throw new InternalError("No qName info found for qName [" + qName + "].");
               }
               tableIndex = this.traversePropertyPath(ref, fromClause, tableIndex, "LEFT JOIN");
               Class<?> declaringType = ref.propertyDeclaringType();
               if (Identity.class.equals(declaringType))
               {
                  columnNames[idx] = SQLs.ENTITY_TABLE_IDENTITY_COLUMN_NAME;
                  tableIndices[idx] = tableIndex - 1;
               } else
               {
                  columnNames[idx] = SQLs.QNAME_TABLE_VALUE_COLUMN_NAME;
                  tableIndices[idx] = tableIndex;
               }
            }
         }
         
         if (fromClause.length() > 0)
         {
            orderByClause.append("ORDER BY ");
            Boolean atLeastOneOBProcessed = false;
            for (Integer idx = 0; idx < orderBy.length; ++idx)
            {
               OrderBy ob = orderBy[idx];
               if (ob != null)
               {
                  if (atLeastOneOBProcessed)
                  {
                     orderByClause.append(", ");
                  }
                  tableIndex = tableIndices[idx];
                  String columnName = columnNames[idx];
                  Order order = ob.order();
                  String orderStr = order.equals(Order.ASCENDING) ? "ASC" : "DESC";
                  orderByClause.append(TABLE_NAME_PREFIX + tableIndex + "." + columnName + " " + orderStr);
                  atLeastOneOBProcessed = true;
               }
            }
         }

      }
      
      private Integer traversePropertyPath(PropertyReference<?> reference, StringBuilder builder, Integer index, String joinStyle)
      {
         
         Stack<QualifiedName> qNameStack = new Stack<QualifiedName>();
         Stack<PropertyReference<?>> refStack = new Stack<PropertyReference<?>>();

         while (reference != null)
         {
            qNameStack.add(QualifiedName.fromClass(reference.propertyDeclaringType(), reference.propertyName()));
            refStack.add(reference);
            if (reference.traversedProperty() == null && reference.traversedAssociation() != null)
            {
               index = this.traverseAssociationPath(reference.traversedAssociation(), builder, index, joinStyle, true);
            }
            
            reference = reference.traversedProperty();
         }
         
         PropertyReference<?> prevRef = null;
         while (!qNameStack.isEmpty())
         {
            QualifiedName qName = qNameStack.pop();
            PropertyReference<?> ref = refStack.pop();
            if (!qName.type().equals(Identity.class.getName()))
            {
               QNameInfo info = this._state.qNameInfos().get().get(qName);
               if (info == null)
               {
                  throw new InternalError("No qName info found for qName [" + qName + "].");
               }
               
               if (prevRef == null)
               {
                  builder.append( //
                        joinStyle + " " + this._state.schemaName().get() + "." + info.getTableName() + " " + TABLE_NAME_PREFIX + (index + 1) + "\n" + //
                        "ON " + TABLE_NAME_PREFIX + index + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + " = " + TABLE_NAME_PREFIX + (index + 1) + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + "\n" //
                        );
               } else
               {
                  builder.append( //
                        joinStyle + " " + this._state.schemaName().get() + "." + info.getTableName() + " " + TABLE_NAME_PREFIX + (index + 1) + "\n" + //
                        "ON " + TABLE_NAME_PREFIX + index + "." + SQLs.QNAME_TABLE_PK_COLUMN_NAME + " = " + TABLE_NAME_PREFIX + (index + 1) + "." + SQLs.QNAME_TABLE_PARENT_QNAME_COLUMN_NAME + "\n"//
                        );
               }
               ++index;
               prevRef = ref;
            }
         }
         
         return index;
      }
      
      private Integer traverseAssociationPath(AssociationReference reference, StringBuilder builder, Integer index, String joinStyle, Boolean includeLastTable)
      {
         Stack<QualifiedName> qNameStack = new Stack<QualifiedName>();
         while (reference != null)
         {
            qNameStack.add(QualifiedName.fromClass(reference.associationDeclaringType(), reference.associationName()));
            reference = reference.traversedAssociation();
         }
         while (!qNameStack.isEmpty())
         {
            QualifiedName qName = qNameStack.pop();
            QNameInfo info = this._state.qNameInfos().get().get(qName);
            if (info == null)
            {
               throw new InternalError("No qName info found for qName [" + qName + "].");
            }
            builder.append( //
                  joinStyle + " " + this._state.schemaName().get() + "." + info.getTableName() + " " + TABLE_NAME_PREFIX + (index + 1) + "\n" + //
                  "ON " + TABLE_NAME_PREFIX + index + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + " = " + TABLE_NAME_PREFIX + (index + 1) + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + "\n" //
                  );
            ++index;
            if (!qNameStack.isEmpty() || includeLastTable)
            {
               builder.append( //
                     joinStyle + " " + this._state.schemaName().get() + "." + SQLs.ENTITY_TABLE_NAME + " " + TABLE_NAME_PREFIX + (index + 1) + "\n" + //
                     "ON " + TABLE_NAME_PREFIX + index + "." + SQLs.QNAME_TABLE_VALUE_COLUMN_NAME + " = " + TABLE_NAME_PREFIX + (index + 1) + "." + SQLs.ENTITY_TABLE_PK_COLUMN_NAME + "\n" //
                     );
               ++index;
            }
         }
         
         return index;
      }
      
      private List<Integer> getEntityTypeIDs(String entityType) throws ClassNotFoundException
      {
         Class<?> entityClass = Class.forName(entityType);
         List<Integer> result = new ArrayList<Integer>();
         for (Map.Entry<String, EntityTypeInfo> entry : this._state.entityTypeInfos().get().entrySet())
         {
            if (entityClass.isAssignableFrom(entry.getValue().getEntityDescriptor().type()))
            {
               result.add(entry.getValue().getEntityTypePK());
            }
         }
         
         return result;
      }
      
      private String getConcreteEntityTypesList(String entityType) throws EntityFinderException
      {
         List<Integer> typeIDs = null;
         try
         {
            typeIDs = this.getEntityTypeIDs(entityType);
         } catch (ClassNotFoundException cnfe)
         {
            throw new EntityFinderException(cnfe);
         }
         StringBuilder result = new StringBuilder();
         Iterator<Integer> iter = typeIDs.iterator();
         while(iter.hasNext())
         {
            result.append(iter.next());
            if (iter.hasNext())
            {
               result.append(", ");
            }
         }
         
         return result.toString();
      }
   }

}
