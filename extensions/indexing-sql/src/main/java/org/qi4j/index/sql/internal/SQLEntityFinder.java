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


package org.qi4j.index.sql.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.library.sql.api.SQLQuerying;
import org.qi4j.library.sql.common.SQLUtil;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;

/**
 *
 * @author Stanislav Muhametsin
 */
public class SQLEntityFinder implements EntityFinder
{
   @Service private SQLQuerying _parser;

   @This private SQLJDBCState _state;

   /**
    * Helper interface to perform some SQL query. Using this simplifies the structure of some of the methods.
    *
    * @param <ReturnType> The return type of something to be done.
    */
   private interface DoQuery<ReturnType>
   {
      ReturnType doIt() throws SQLException;
   }

   public long countEntities(String resultType, BooleanExpression whereClause) throws EntityFinderException
   {

      final List<Object> values = new ArrayList<Object>();
      final List<Integer> valueSQLTypes = new ArrayList<Integer>();
      final String query = this._parser.constructQuery(resultType, whereClause, null, null, null, values, valueSQLTypes, true);

      return this.performQuery(new DoQuery<Long>()
      {

         public Long doIt() throws SQLException
         {
            PreparedStatement ps = null;
            try
            {
               ps = createPS(query, values, valueSQLTypes);
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

   public Iterable<EntityReference> findEntities(String resultType, BooleanExpression whereClause, OrderBy[] orderBySegments, final Integer firstResult, final Integer maxResults) throws EntityFinderException
   {
      // TODO what is Qi4j's policy on negative firstResult and/or maxResults? JDBC has its own way of interpreting these values - does it match with Qi4j's way?
      Iterable<EntityReference> result = null;
      if (maxResults == null || maxResults > 0)
      {
         final List<Object> values = new ArrayList<Object>();
         final List<Integer> valueSQLTypes = new ArrayList<Integer>();
         final String query = this._parser.constructQuery(resultType, whereClause, orderBySegments, firstResult, maxResults, values, valueSQLTypes, false);

         result = this.performQuery(new DoQuery<Iterable<EntityReference>>()
         {
            public Iterable<EntityReference> doIt() throws SQLException
            {
               PreparedStatement ps = null;
               List<EntityReference> resultList = new ArrayList<EntityReference>(maxResults == null ? 100 : maxResults);
               try
               {
                  // TODO possibility to further optimize by setting fetch size (not too small not too little).
                  Integer rsType = _parser.getResultSetType( firstResult, maxResults );
                  ps = createPS(query, values, valueSQLTypes, rsType, ResultSet.CLOSE_CURSORS_AT_COMMIT);
                  ResultSet rs = ps.executeQuery();
                  if (firstResult != null && !_parser.isFirstResultSettingSupported() && rsType != ResultSet.TYPE_FORWARD_ONLY)
                  {
                      rs.absolute( firstResult );
                  }
                  Integer i = 0;
                  while (rs.next() && (maxResults == null || i < maxResults))
                  {
                     resultList.add(new EntityReference(rs.getString(1)));
                     ++i;
                  }
               } finally
               {
                  if (ps != null)
                  {
                     ps.close();
                  }
               }

               return resultList;
            }

         });

      } else
      {
         result = new ArrayList<EntityReference>(0);
      }

      return result;
   }

   public EntityReference findEntity(String resultType, BooleanExpression whereClause) throws EntityFinderException
   {
      final List<Object> values = new ArrayList<Object>();
      final List<Integer> valueSQLTypes = new ArrayList<Integer>();
      final String query = this._parser.constructQuery(resultType, whereClause, null, null, null, values, valueSQLTypes, false);

      return this.performQuery(new DoQuery<EntityReference>()
      {
         public EntityReference doIt() throws SQLException
         {
            PreparedStatement ps = null;
            EntityReference result = null;
            try
            {
               ps = createPS(query, values, valueSQLTypes);
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

   private PreparedStatement createPS(String query, List<Object> values, List<Integer> valueSQLTypes) throws SQLException
   {
      return this.createPS(query, values, valueSQLTypes, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
   }

   private PreparedStatement createPS(String query, List<Object> values, List<Integer> valueSQLTypes, Integer resultSetType, Integer resultSetHoldability) throws SQLException
   {
      PreparedStatement ps = this._state.connection().get().prepareStatement(query, resultSetType, ResultSet.CONCUR_READ_ONLY, resultSetHoldability);
      if (values.size() != valueSQLTypes.size())
      {
         throw new InternalError("There was either too little or too much sql types for values [values=" + values.size() + ", types=" + valueSQLTypes.size() + "].");
      }

      for (Integer x = 0; x < values.size(); ++x)
      {
         ps.setObject(x + 1, values.get(x), valueSQLTypes.get(x));
      }

      return ps;
   }

   // Helper method to perform SQL queries and handle things if/when something happens
   private <ReturnType> ReturnType performQuery(DoQuery<ReturnType> doQuery) throws EntityFinderException
   {
      ReturnType result = null;
      Connection connection = this._state.connection().get();
      try
      {
          connection.setReadOnly(true);

          result = doQuery.doIt();

      } catch (SQLException sqle)
      {
         throw new EntityFinderException(sqle);
      } finally
      {
         SQLUtil.rollbackQuietly( connection );
      }

      return result;
   }

}
