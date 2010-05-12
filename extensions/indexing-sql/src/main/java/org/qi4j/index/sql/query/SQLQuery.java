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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.index.sql.common.SQLIndexingState;
import org.qi4j.index.sql.common.SQLTypeHelper;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;

/**
 * This interface is responsible for finding entities based on Qi4j query conditions from relational database. Currently this interface
 * just extends the {@link EntityFinder} interface, but in case there will be some SQL-indexing specific methods associated with finding entities,
 * this interface will be the place to add them.
 *
 * @author Stanislav Muhametsin
 */
@Mixins({
   SQLQuery.SQLQueryMixin.class,
   SQLQueryParser.SQLQueryParserImpl.class
})
public interface SQLQuery extends EntityFinder
{
   
   public class SQLQueryMixin implements SQLQuery
   {
      
      @This private SQLQueryParser _parser;
      
      @This private SQLIndexingState _state;
      
      @This private SQLTypeHelper _sqlTypeHelper;
      
      /**
       * Helper interface to perform some SQL query. Using this simplifies the structure of some of the methods.
       *
       * @param <ReturnType> The return type of something to be done.
       */
      private interface DoQuery<ReturnType>
      {
         ReturnType doIt() throws SQLException;
      }
      
      @Override
      public long countEntities(String resultType, BooleanExpression whereClause) throws EntityFinderException
      {

         final List<Object> values = new ArrayList<Object>();
         final String query = this._parser.getQuery(resultType, whereClause, null, null, null, values, true);
         
         return this.performQuery(new DoQuery<Long>()
         {
            
            @Override
            public Long doIt() throws SQLException
            {
               PreparedStatement ps = null;
               try
               {
                  ps = createPS(query, values);
                  ResultSet rs = ps.executeQuery();
                  rs.next();
                  return rs.getLong(1);
               } finally
               {
                  if (ps != null)
                  {
                     ps.close();
                  }
               }
            }
         });
      }
      
      @Override
      public Iterable<EntityReference> findEntities(String resultType, BooleanExpression whereClause, OrderBy[] orderBySegments, final Integer firstResult, final Integer maxResults) throws EntityFinderException
      {
         // TODO what is Qi4j's policy on negative firstResult and/or maxResults? JDBC has its own way of interpreting these values - does it match with Qi4j's way?
         Iterable<EntityReference> result = null;
         if (maxResults == null || maxResults > 0)
         {
            final List<Object> values = new ArrayList<Object>();
            final String query = this._parser.getQuery(resultType, whereClause, orderBySegments, firstResult, maxResults, values, false);
         
            result = this.performQuery(new DoQuery<Iterable<EntityReference>>()
            {
               @Override
               public Iterable<EntityReference> doIt() throws SQLException
               {
                  PreparedStatement ps = null;
                  List<EntityReference> result = new ArrayList<EntityReference>(maxResults == null ? 100 : maxResults);
                  try
                  {
                     // TODO possibility to further optimize by setting fetch size (not too small not too little).
                     ps = createPS(query, values, firstResult == null ? ResultSet.TYPE_FORWARD_ONLY : ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CLOSE_CURSORS_AT_COMMIT);
                     ResultSet rs = ps.executeQuery();
                     if (firstResult != null)
                     {
                        rs.absolute(firstResult);
                     }
                     Integer i = 0;
                     while (rs.next() && (maxResults == null || i < maxResults))
                     {
                        result.add(new EntityReference(rs.getString(1)));
                        ++i;
                     }
                  } finally
                  {
                     if (ps != null)
                     {
                        ps.close();
                     }
                  }
                  
                  return result;
               }
            
            });
            
         } else
         {
            result = new ArrayList<EntityReference>(0);
         }
         
         return result;
      }
      
      @Override
      public EntityReference findEntity(String resultType, BooleanExpression whereClause) throws EntityFinderException
      {
         final List<Object> values = new ArrayList<Object>();
         final String query = this._parser.getQuery(resultType, whereClause, null, null, null, values, false);
         
         return this.performQuery(new DoQuery<EntityReference>()
         {
            @Override
            public EntityReference doIt() throws SQLException
            {
               PreparedStatement ps = null;
               EntityReference result = null;
               try
               {
                  ps = createPS(query, values);
                  ps.setFetchSize(1);
                  ps.setMaxRows(1);
                  ResultSet rs = ps.executeQuery();
                  if (rs.next())
                  {
                     result = new EntityReference(rs.getString(1));
                  }
               } finally
               {
                  if (ps != null)
                  {
                     ps.close();
                  }
               }
               
               return result;
            }
         });
      }
      
      private PreparedStatement createPS(String query, List<Object> values) throws SQLException
      {
         return this.createPS(query, values, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
      }
      
      private PreparedStatement createPS(String query, List<Object> values, Integer resultSetType, Integer resultSetHoldability) throws SQLException
      {
         PreparedStatement ps = this._state.connection().get().prepareStatement(query, resultSetType, ResultSet.CONCUR_READ_ONLY, resultSetHoldability);
         Integer index = 1;
         for (Object value : values)
         {
            this._sqlTypeHelper.addPrimitiveToPS(ps, index, value, value.getClass());
            ++index;
         }
         
         return ps;
      }
      
      // Helper method to perform SQL queries and handle things if/when something happens
      private <ReturnType> ReturnType performQuery(DoQuery<ReturnType> doQuery) throws EntityFinderException
      {
         Boolean wasReadOnly = null;
         ReturnType result = null;
         try
         {
            wasReadOnly = this._state.connection().get().isReadOnly();
            this._state.connection().get().setReadOnly(true);

            result = doQuery.doIt();
            
         } catch (SQLException sqle)
         {
            throw new EntityFinderException(sqle);
         } finally
         {
            try
            {
               this._state.connection().get().rollback();
               if (wasReadOnly != null)
               {
                  this._state.connection().get().setReadOnly(wasReadOnly);
               }
            } catch (SQLException sqle)
            {
               throw new EntityFinderException(sqle);
            }
         }
         
         return result;
      }

   }
}
