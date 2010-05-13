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
   
   private static final String SEPARATOR = "-----------------------------------------------";
   
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
   
   private static class ColumnInfo
   {
      private final String _name;
      
      private final Integer _sqlType;
      
      private final Integer _size;
      
      private final Integer _scale;
      
      private final String _nullable;
      
      private final String _defaultValue;
      
      public ColumnInfo(String name, Integer sqlType, Integer size, Integer scale, String nullable, String defaultValue)
      {
         this._name = name;
         this._sqlType = sqlType;
         this._size = size;
         this._scale = scale;
         this._nullable = nullable;
         this._defaultValue = defaultValue;
      }
   }
   
   private interface WriteProcessor
   {
      public void beginProcessSchemaInfo(String schemaName);
      public void endProcessSchemaInfo(String schemaName);
      
      public void beginProcessTableInfo(String schemaName, String tableName);
      public void endProcessTableInfo(String schemaName, String tableName);
      
      public void beginProcessColumns(String schemaName, String tableName);
      public void beginProcessColumnInfo(String schemaName, String tableName, ColumnInfo colInfo);
      public void endProcessColumnInfo(String schemaName, String tableName, ColumnInfo colInfo);
      public void endProcessColumns(String schemaName, String tableName);
      
      public void beginProcessRows(String schemaName, String tableName);
      public void beginProcessRowInfo(String schemaName, String tableName, Object[] rowContents);
      public void endProcessRowInfo(String schemaName, String tableName, Object[] rowContents);
      public void endProcessRows(String schemaName, String tableName);
   }
   
   @Override
   public void exportFormalToWriter(final PrintWriter out) throws IOException, UnsupportedOperationException
   {
      try
      {
         this.export(new WriteProcessor()
         {
            @Override
            public void endProcessColumns(String schemaName, String tableName)
            {
               out.write("</columns>" + "\n");
            }
            
            @Override
            public void endProcessRows(String schemaName, String tableName)
            {
               out.write("</rows>" + "\n");
            }
            
            @Override
            public void endProcessTableInfo(String schemaName, String tableName)
            {
               out.write("</table>" + "\n");
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
            public void endProcessColumnInfo(String schemaName, String tableName, ColumnInfo colInfo)
            {
            }
            
            @Override
            public void beginProcessTableInfo(String schemaName, String tableName)
            {
               out.write("<table name=\"" + tableName + "\">" + "\n");
            }
            
            @Override
            public void beginProcessColumns(String schemaName, String tableName)
            {
               out.write("<columns>" + "\n");
            }
            
            @Override
            public void beginProcessColumnInfo(String schemaName, String tableName, ColumnInfo colInfo)
            {
               out.write("<column name=\"" + colInfo._name + "\" colType=\"" + colInfo._sqlType + "\" colSize=\"" + colInfo._size + "\" colScale=\"" + colInfo._scale + "\" nullable=\"" + colInfo._nullable + "\" default=\"" + colInfo._defaultValue + "\" />" + "\n");
            }
            
            @Override
            public void beginProcessSchemaInfo(String schemaName)
            {
               out.write("<schema name=\"" + schemaName + "\">" + "\n");
            }
            
            @Override
            public void beginProcessRows(String schemaName, String tableName)
            {
               out.write("<rows>" + "\n");
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
            public void endProcessTableInfo(String schemaName, String tableName)
            {
               out.print("\n\n\n");
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
            public void endProcessColumnInfo(String schemaName, String tableName, ColumnInfo colInfo)
            {
               // TODO Auto-generated method stub
               
            }
            
            @Override
            public void endProcessColumns(String schemaName, String tableName)
            {
               out.print(SEPARATOR + "\n" + SEPARATOR + "\n");
            }
            
            @Override
            public void endProcessRows(String schemaName, String tableName)
            {
               out.print(SEPARATOR + "\n" + SEPARATOR + "\n");
            }
            
            private String parseSQLType(ColumnInfo colInfo)
            {
               Integer sqlType = colInfo._sqlType;
               String basicType = _typeStrings.get(sqlType);
               if (sqlType == Types.DECIMAL || sqlType == Types.NUMERIC)
               {
                  basicType = basicType + "(" + colInfo._size + ", " + colInfo._scale + ")";
               } else if (sqlType == Types.VARCHAR || sqlType == Types.VARBINARY)
               {
                  basicType = basicType + "(" + colInfo._size + ")";
               }
               return basicType;
            }
            
            @Override
            public void beginProcessColumnInfo(String schemaName, String tableName, ColumnInfo colInfo)
            {
               
               out.print(colInfo._name + ":" + this.parseSQLType(colInfo) + "[nullable:" + colInfo._nullable + "; default: " + colInfo._defaultValue + "]" + "\n");
            }
            
            @Override
            public void beginProcessTableInfo(String schemaName, String tableName)
            {
               out.print("Table: " + tableName + "\n");
            }
            
            @Override
            public void beginProcessSchemaInfo(String schemaName)
            {
               out.print( //
                     "\n\n" + "Schema: " + schemaName + "\n" + //
                     SEPARATOR + "\n\n\n" //
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
            
            @Override
            public void beginProcessColumns(String schemaName, String tableName)
            {
               out.print(SEPARATOR + "\n" + SEPARATOR + "\n");
            }
            
            @Override
            public void beginProcessRows(String schemaName, String tableName)
            {
               
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

            
            try
            {
               processor.beginProcessTableInfo(schemaName, tableName);
               List<ColumnInfo> colInfos = new ArrayList<ColumnInfo>();
               ResultSet rsCols = metaData.getColumns(null, schemaName, tableName, null);
               try
               {
                  while (rsCols.next())
                  {
                     String nullable = rsCols.getString(18);
                     colInfos.add(new ColumnInfo(
                           rsCols.getString(4),
                           rsCols.getInt(5),
                           rsCols.getInt(7),
                           rsCols.getInt(9),
                           nullable.length() > 0 ? Boolean.toString(nullable.equals("YES")) : "unknown",
                           rsCols.getString(13)
                           ));
                  }
               }
               finally
               {
                  rsCols.close();
               }
               try
               {
                  processor.beginProcessColumns(schemaName, tableName);
                  for (ColumnInfo colInfo : colInfos)
                  {
                     try
                     {
                        processor.beginProcessColumnInfo(schemaName, tableName, colInfo);
                     } finally
                     {
                        processor.endProcessColumnInfo(schemaName, tableName, colInfo);
                     }
                  }
               } finally
               {
                  processor.endProcessColumns(schemaName, tableName);
               }
               
               String sql = "SELECT * FROM " + schemaName + "." + tableName;
               Statement stmt = connection.createStatement();
               ResultSet rowsRs = null;
               try
               {
                  rowsRs = stmt.executeQuery(sql);
                  processor.beginProcessRows(schemaName, tableName);
                  while (rowsRs.next())
                  {
                     Object[] rowContents = new Object[colInfos.size()];
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
                  processor.endProcessRows(schemaName, tableName);
                  if (rowsRs != null)
                  {
                     rowsRs.close();
                  }
                  stmt.close();
               }
            }
            finally
            {
               processor.endProcessTableInfo(schemaName, tableName);
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
