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

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.qi4j.api.injection.scope.This;
import org.qi4j.spi.query.IndexExporter;

/**
 *
 * @author Stanislav Muhametsin
 */
public class PostgreSQLIndexExporter implements IndexExporter
{
   
   @This private PostgreSQLDBState _state;
   
   private static Map<Integer, String> _typeStrings;
   
   static
   {
      _typeStrings = new HashMap<Integer, String>();
      _typeStrings.put(Types.BIGINT, "BIGINT");
      _typeStrings.put(Types.BOOLEAN, "BOOLEAN");
      _typeStrings.put(Types.CHAR, "CHAR");
      _typeStrings.put(Types.DATE, "DATE");
      _typeStrings.put(Types.DECIMAL, "DECIMAL");
      _typeStrings.put(Types.DOUBLE, "DOUBLE");
      _typeStrings.put(Types.FLOAT, "FLOAT");
      _typeStrings.put(Types.INTEGER, "INTEGER");
      _typeStrings.put(Types.NULL, "NULL");
      _typeStrings.put(Types.NUMERIC, "NUMERIC");
      _typeStrings.put(Types.REAL, "REAL");
      _typeStrings.put(Types.SMALLINT, "SMALLINT");
      _typeStrings.put(Types.TIME, "TIME");
      _typeStrings.put(Types.TIMESTAMP, "TIMESTAMP");
      _typeStrings.put(Types.VARCHAR, "VARCHAR");
   }
   
   private interface WriteProcessor
   {
      public void beginProcessSchemaInfo(String schemaName);
      public void endProcessSchemaInfo(String schemaName);
      
      public void beginProcessTableInfo(String schemaName, String tableName, List<String> colNames, List<Integer> rowSQLTypes);
      public void endProcessTableInfo(String schemaName, String tableName, List<String> colNames, List<Integer> rowSQLTypes);
      
      public void beginProcessRowInfo(String schemaName, String tableName, Object[] rowContents);
      public void endProcessRowInfo(String schemaName, String tableName, Object[] rowContents);
   }
   
   @Override
   public void exportFormalToWriter(final PrintWriter out) throws IOException, UnsupportedOperationException
   {
      try
      {
         this.export(new WriteProcessor()
         {
            
            @Override
            public void endProcessTableInfo(String schemaName, String tableName, List<String> colNames, List<Integer> rowSQLTypes)
            {
               out.write( //
                     "</rows>" + "\n" + //
                           "</table" + "\n");
            }
            
            @Override
            public void endProcessSchemaInfo(String schemaName)
            {
               out.write("</schema>" + "\n");
            }
            
            @Override
            public void endProcessRowInfo(String schemaName, String tableName, Object[] rowContents)
            {
               out.write("</row>" + "\n");
            }
            
            @Override
            public void beginProcessTableInfo(String schemaName, String tableName, List<String> colNames, List<Integer> rowSQLTypes)
            {
               out.write( //
                     "<table name=\"" + tableName + "\">" + "\n" + //
                           "<columns>" + "\n" //
                     );
               for (Integer x = 0; x < colNames.size(); ++x)
               {
                  out.write( //
                        "<column name=\"" + colNames.get(x) + "\" colType=\"" + rowSQLTypes.get(x) + "\" colTypeName=\"" + _typeStrings.get(rowSQLTypes.get(x)) + "\" />" + "\n" //
                        );
               }
               out.write( //
                     "</columns>" + "\n" + //
                           "<rows>" + "\n" //
                     );
            }
            
            @Override
            public void beginProcessSchemaInfo(String schemaName)
            {
               out.write("<schema name=\"" + schemaName + "\">\n");
            }
            
            @Override
            public void beginProcessRowInfo(String schemaName, String tableName, Object[] rowContents)
            {
               out.write("<row>" + "\n");
               for (Integer x = 0; x < rowContents.length; ++x)
               {
                  out.write("<value index=\"" + x + "\" >" + rowContents[x] + "</value>" + "\n");
               }
            }
         });
      }
      catch (SQLException sqle)
      {
         // TODO Just wrap around for now...
         throw new IOException(sqle);
      }
   }
   
   @Override
   public void exportReadableToStream(final PrintStream out) throws IOException, UnsupportedOperationException
   {
      try
      {
         this.export(new WriteProcessor()
         {
            
            @Override
            public void endProcessTableInfo(String schemaName, String tableName, List<String> colNames, List<Integer> rowSQLTypes)
            {
               out.print("-----------------------------------------------" + "\n\n\n");
            }
            
            @Override
            public void endProcessSchemaInfo(String schemaName)
            {
               out.print("\n\n");
            }
            
            @Override
            public void endProcessRowInfo(String schemaName, String tableName, Object[] rowContents)
            {
               
            }
            
            @Override
            public void beginProcessTableInfo(String schemaName, String tableName, List<String> colNames, List<Integer> rowSQLTypes)
            {
               out.print("Table: " + tableName + "; Columns: ");
               for (Integer x = 0; x < colNames.size(); ++x)
               {
                  out.print(colNames.get(x) + ":" + _typeStrings.get(rowSQLTypes.get(x)));
                  if (x + 1 < colNames.size())
                  {
                     out.print(", ");
                  }
                  
               }
               out.print("\n" + "-----------------------------------------------" + "\n");
               out.print("Contents:" + "\n" + "-----------------------------------------------" + "\n");
            }
            
            @Override
            public void beginProcessSchemaInfo(String schemaName)
            {
               out.print( //
                     "Schema: " + schemaName + "\n" + //
                     "-----------------------------------------------" + "\n\n\n" //
                     );
            }
            
            @Override
            public void beginProcessRowInfo(String schemaName, String tableName, Object[] rowContents)
            {
               for (Integer x = 0; x < rowContents.length; ++x)
               {
                  Object value = rowContents[x];
                  out.print(value == null ? "NULL" : ("\"" + value + "\""));
                  if (x + 1 < rowContents.length)
                  {
                     out.print(" ; ");
                  }
               }
               out.print("\n");
            }
         });
      } catch (SQLException sqle)
      {
         // TODO Just wrap around for now...
         throw new IOException(sqle);
      }
   }
   
   private void export(WriteProcessor processor) throws SQLException
   {
      Connection connection = this._state.connection().get();
      connection.setReadOnly(true);
      DatabaseMetaData metaData = connection.getMetaData();
      String schemaName = this._state.schemaName().get();
      ResultSet rs = metaData.getTables(null, schemaName, null, new String[] { "TABLE" });
      try
      {
         processor.beginProcessSchemaInfo(schemaName);
         while (rs.next())
         {
            String tableName = rs.getString(3);
            List<String> colNames = new ArrayList<String>();
            List<Integer> colTypes = new ArrayList<Integer>();
            ResultSet rsCols = metaData.getColumns(null, schemaName, tableName, null);
            try
            {
               while (rsCols.next())
               {
                  colNames.add(rsCols.getString(4));
                  colTypes.add(rsCols.getInt(5));
               }
            }
            finally
            {
               rsCols.close();
            }
            
            colNames = Collections.unmodifiableList(colNames);
            colTypes = Collections.unmodifiableList(colTypes);
            
            try
            {
               processor.beginProcessTableInfo(schemaName, tableName, colNames, colTypes);
               String sql = "SELECT * FROM " + schemaName + "." + tableName;
               Statement stmt = connection.createStatement();
               ResultSet rowsRs = null;
               try
               {
                  rowsRs = stmt.executeQuery(sql);
                  while (rowsRs.next())
                  {
                     Object[] rowContents = new Object[colNames.size()];
                     for (Integer x = 0; x < rowContents.length; ++x)
                     {
                        rowContents[x] = rowsRs.getObject(x + 1);
                     }
                     
                     try
                     {
                        processor.beginProcessRowInfo(schemaName, tableName, rowContents);
                     } finally
                     {
                        processor.endProcessRowInfo(schemaName, tableName, rowContents);
                     }
                  }
               } finally
               {
                  if (rowsRs != null)
                  {
                     rowsRs.close();
                  }
                  stmt.close();
               }
            }
            finally
            {
               processor.endProcessTableInfo(schemaName, tableName, colNames, colTypes);
            }
         }
      }
      finally
      {
         try
         {
            rs.close();
            connection.rollback();
         } finally
         {
            processor.endProcessSchemaInfo(schemaName);
         }
      }
   }
}
