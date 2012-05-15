/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
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

package org.qi4j.index.sql.support.postgresql;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.index.sql.support.common.GenericDatabaseExplorer;
import org.qi4j.index.sql.support.common.GenericDatabaseExplorer.ColumnInfo;
import org.qi4j.index.sql.support.common.GenericDatabaseExplorer.DatabaseProcessor;
import org.qi4j.index.sql.support.common.GenericDatabaseExplorer.ForeignKeyInfo;
import org.qi4j.index.sql.support.skeletons.SQLDBState;
import org.qi4j.library.sql.common.SQLUtil;
import org.qi4j.spi.query.IndexExporter;

import org.sql.generation.api.vendor.SQLVendor;

public class PostgreSQLIndexExporter
    implements IndexExporter, Activatable
{

    @This
    private SQLDBState _state;

    @Uses
    private ServiceDescriptor descriptor;

    private SQLVendor _vendor;

    @Service
    private DataSource _dataSource;

    public void activate()
        throws Exception
    {
        this._vendor = this.descriptor.metaInfo( SQLVendor.class );
    }

    public void passivate()
        throws Exception
    {
    }

    private static final String SEPARATOR = "-----------------------------------------------";

    private static final Map<Integer, String> _typeStrings;

    static
    {
        _typeStrings = new HashMap<Integer, String>();
        _typeStrings.put( Types.BIGINT, "BIGINT" );
        _typeStrings.put( Types.BOOLEAN, "BOOLEAN" );
        _typeStrings.put( Types.CHAR, "CHAR" );
        _typeStrings.put( Types.DATE, "DATE" );
        _typeStrings.put( Types.DECIMAL, "DECIMAL" );
        _typeStrings.put( Types.DOUBLE, "DOUBLE" );
        _typeStrings.put( Types.FLOAT, "FLOAT" );
        _typeStrings.put( Types.INTEGER, "INTEGER" );
        _typeStrings.put( Types.NULL, "NULL" );
        _typeStrings.put( Types.NUMERIC, "NUMERIC" );
        _typeStrings.put( Types.REAL, "REAL" );
        _typeStrings.put( Types.SMALLINT, "SMALLINT" );
        _typeStrings.put( Types.TIME, "TIME" );
        _typeStrings.put( Types.TIMESTAMP, "TIMESTAMP" );
        _typeStrings.put( Types.VARCHAR, "VARCHAR" );
        _typeStrings.put( Types.VARBINARY, "VARBINARY" );
    }

    public void exportFormalToWriter( final PrintWriter out )
        throws IOException,
        UnsupportedOperationException
    {
        Connection connection = null;
        try
        {
            connection = this._dataSource.getConnection();
            GenericDatabaseExplorer.visitDatabaseTables( connection, null,
                this._state.schemaName().get(), null, new DatabaseProcessor()
                {

                    public void endProcessColumns( String schemaName, String tableName, String tableRemarks )
                    {
                        out.write( "</columns>" + "\n" );
                    }

                    public void endProcessRows( String schemaName, String tableName, String tableRemarks )
                    {
                        out.write( "</rows>" + "\n" );
                    }

                    public void endProcessTableInfo( String schemaName, String tableName, String tableRemarks )
                    {
                        out.write( "</table>" + "\n" );
                    }

                    public void endProcessSchemaInfo( String schemaName )
                    {
                        out.write( "</schema>" + "\n" );
                    }

                    public void endProcessRowInfo( String schemaName, String tableName, Object[] rowContents )
                    {
                        out.write( "</row>" + "\n" );
                    }

                    public void endProcessColumnInfo( String schemaName, String tableName, ColumnInfo colInfo,
                        ForeignKeyInfo fkInfo )
                    {
                    }

                    public void beginProcessTableInfo( String schemaName, String tableName, String tableRemarks )
                    {
                        out.write( "<table name=\"" + tableName + "\" remarks=\"" + tableRemarks + "\">" + "\n" );
                    }

                    public void beginProcessColumns( String schemaName, String tableName, String tableRemarks )
                    {
                        out.write( "<columns>" + "\n" );
                    }

                    public void beginProcessColumnInfo( String schemaName, String tableName, ColumnInfo colInfo,
                        ForeignKeyInfo fkInfo )
                    {
                        String defaultValue = colInfo.getDefaultValue();
                        if( defaultValue.startsWith( "'" ) )
                        {
                            defaultValue = defaultValue.substring( 1, defaultValue.length() - 1 );
                        }
                        out.write( "<column name=\"" + colInfo.getName() + "\" colType=\"" + colInfo.getTypeName()
                            + "\" colSize=\"" + colInfo.getSize() + "\" colScale=\""
                            + //
                            colInfo.getScale() + "\" nullable=\"" + colInfo.getNullable() + "\" default=\""
                            + defaultValue + "\" " //
                        );
                        if( fkInfo != null )
                        {
                            out.write( "refSchemaName=\"" + fkInfo.getPkSchemaName() + "\" refTableName=\""
                                + fkInfo.getPkTableName() + "\" refPKColumnName=\""
                                + fkInfo.getPkTablePKColumnName()
                                + //
                                "\" onUpdate=\"" + fkInfo.getOnUpdateAction() + "\" onDelete=\""
                                + fkInfo.getOnDeleteAction() + "\" deferrability=\"" + fkInfo.getDeferrability()
                                + "\" " //
                            );
                        }

                        out.write( "/>" + "\n" );
                    }

                    public void beginProcessSchemaInfo( String schemaName )
                    {
                        out.write( "<schema name=\"" + schemaName + "\">" + "\n" );
                    }

                    public void beginProcessRows( String schemaName, String tableName, String tableRemarks )
                    {
                        out.write( "<rows>" + "\n" );
                    }

                    public void beginProcessRowInfo( String schemaName, String tableName, Object[] rowContents )
                    {
                        out.write( "<row>" + "\n" );
                        for( Integer x = 0; x < rowContents.length; ++x )
                        {
                            out.write( "<value index=\"" + x + "\" >" + rowContents[x] + "</value>" + "\n" );
                        }
                    }
                }, this._vendor );
        }
        catch( SQLException sqle )
        {
            // TODO Just wrap around for now...
            throw new IOException( sqle );
        } finally {
            SQLUtil.closeQuietly( connection );
        }
    }

    public void exportReadableToStream( final PrintStream out )
        throws IOException,
        UnsupportedOperationException
    {
        Connection connection = null;
        try
        {
            connection = this._dataSource.getConnection();
            GenericDatabaseExplorer.visitDatabaseTables( connection, null,
                this._state.schemaName().get(), null, new DatabaseProcessor()
                {

                    public void endProcessTableInfo( String schemaName, String tableName, String tableRemarks )
                    {
                        out.print( "\n\n\n" );
                    }

                    public void endProcessSchemaInfo( String schemaName )
                    {
                        out.print( "\n\n" );
                    }

                    public void endProcessRowInfo( String schemaName, String tableName, Object[] rowContents )
                    {

                    }

                    public void endProcessColumnInfo( String schemaName, String tableName, ColumnInfo colInfo,
                        ForeignKeyInfo fkInfo )
                    {

                    }

                    public void endProcessColumns( String schemaName, String tableName, String tableRemarks )
                    {
                        out.print( SEPARATOR + "\n" + SEPARATOR + "\n" );
                    }

                    public void endProcessRows( String schemaName, String tableName, String tableRemarks )
                    {
                        out.print( SEPARATOR + "\n" + SEPARATOR + "\n" );
                    }

                    private String parseSQLType( ColumnInfo colInfo )
                    {
                        String result = colInfo.getTypeName();
                        Integer sqlType = colInfo.getSQLType();
                        if( _typeStrings.containsKey( sqlType ) )
                        {
                            result = _typeStrings.get( sqlType );
                            if( sqlType == Types.DECIMAL || sqlType == Types.NUMERIC )
                            {
                                result = result + "(" + colInfo.getSize() + ", " + colInfo.getScale() + ")";
                            }
                            else if( sqlType == Types.VARCHAR || sqlType == Types.VARBINARY )
                            {
                                result = result + "(" + colInfo.getSize() + ")";
                            }
                        }
                        return result;
                    }

                    public void beginProcessColumnInfo( String schemaName, String tableName, ColumnInfo colInfo,
                        ForeignKeyInfo fkInfo )
                    {
                        out.print( colInfo.getName() + ":" + this.parseSQLType( colInfo ) + "[nullable:"
                            + colInfo.getNullable() + "; default: " + colInfo.getDefaultValue() + "]" );
                        if( fkInfo != null )
                        {
                            out.print( " -> " + fkInfo.getPkSchemaName() + "." + fkInfo.getPkTableName() + "("
                                + fkInfo.getPkTablePKColumnName() + ")[ON UPDATE " + fkInfo.getOnUpdateAction()
                                + ", ON DELETE " + fkInfo.getOnDeleteAction() + ", " + fkInfo.getDeferrability() + "]" );
                        }
                        out.print( "\n" );
                    }

                    public void beginProcessTableInfo( String schemaName, String tableName, String tableRemarks )
                    {
                        out.print( "Table: " + schemaName + "." + tableName
                            + (tableRemarks == null ? "" : " (" + tableRemarks + ")") + "\n" );
                    }

                    public void beginProcessSchemaInfo( String schemaName )
                    {
                        out.print( //
                        "\n\n" + "Schema: " + schemaName + "\n" + //
                            SEPARATOR + "\n\n\n" //
                        );
                    }

                    public void beginProcessRowInfo( String schemaName, String tableName, Object[] rowContents )
                    {
                        for( Integer x = 0; x < rowContents.length; ++x )
                        {
                            Object value = rowContents[x];
                            out.print( value == null ? "NULL" : ("\"" + value + "\"") );
                            if( x + 1 < rowContents.length )
                            {
                                out.print( " ; " );
                            }
                        }
                        out.print( "\n" );
                    }

                    public void beginProcessColumns( String schemaName, String tableName, String tableRemarks )
                    {
                        out.print( SEPARATOR + "\n" + SEPARATOR + "\n" );
                    }

                    public void beginProcessRows( String schemaName, String tableName, String tableRemarks )
                    {

                    }
                }, this._vendor );
        }
        catch( SQLException sqle )
        {
            // TODO Just wrap around for now...
            throw new IOException( sqle );
        } finally {
            SQLUtil.closeQuietly( connection );
        }
    }
}
