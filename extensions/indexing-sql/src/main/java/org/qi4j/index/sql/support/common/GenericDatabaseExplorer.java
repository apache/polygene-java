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

package org.qi4j.index.sql.support.common;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.library.sql.common.SQLUtil;
import org.qi4j.spi.query.IndexExporter;
import org.sql.generation.api.grammar.builders.query.QuerySpecificationBuilder;
import org.sql.generation.api.grammar.factories.QueryFactory;
import org.sql.generation.api.grammar.factories.TableReferenceFactory;
import org.sql.generation.api.vendor.SQLVendor;

/**
 * This is a helper class to traverse through all content in specified tables in database. Typical usecase would be by
 * {@link IndexExporter} implementation.
 *
 * @author Stanislav Muhametsin
 */
public final class GenericDatabaseExplorer
{
    public static enum IntegrityActions
    {
        CASCADE,
        NO_ACTION,
        RESTRICT,
        SET_DEFAULT,
        SET_NULL
    }

    public static enum Deferrability
    {
        INITIALLY_DEFERRED,
        INITIALLY_IMMEDIATE,
        NOT_DEFERRABLE
    }

    private static Map<Integer, IntegrityActions> _integrityActions;

    private static Map<Integer, Deferrability> _deferrabilities;

    static
    {
        _deferrabilities = new HashMap<Integer, Deferrability>();
        _deferrabilities.put( DatabaseMetaData.importedKeyInitiallyDeferred, Deferrability.INITIALLY_DEFERRED );
        _deferrabilities.put( DatabaseMetaData.importedKeyInitiallyImmediate, Deferrability.INITIALLY_IMMEDIATE );
        _deferrabilities.put( DatabaseMetaData.importedKeyNotDeferrable, Deferrability.NOT_DEFERRABLE );

        _integrityActions = new HashMap<Integer, IntegrityActions>();
        _integrityActions.put( DatabaseMetaData.importedKeyCascade, IntegrityActions.CASCADE );
        _integrityActions.put( DatabaseMetaData.importedKeyNoAction, IntegrityActions.NO_ACTION );
        _integrityActions.put( DatabaseMetaData.importedKeyRestrict, IntegrityActions.RESTRICT );
        _integrityActions.put( DatabaseMetaData.importedKeySetDefault, IntegrityActions.SET_DEFAULT );
        _integrityActions.put( DatabaseMetaData.importedKeySetNull, IntegrityActions.SET_NULL );
    }

    public static class ColumnInfo
    {

        private final String _name;

        private final Integer _sqlType;

        private final String _typeName;

        private final Integer _size;

        private final Integer _scale;

        private final String _nullable;

        private final String _defaultValue;

        private final String _remarks;

        private ColumnInfo( String name, Integer sqlType, String typeName, Integer size, Integer scale,
            String nullable, String defaultValue, String remarks )
        {
            this._name = name;
            this._sqlType = sqlType;
            this._typeName = typeName;
            this._size = size;
            this._scale = scale;
            this._nullable = nullable;
            this._defaultValue = defaultValue;
            this._remarks = remarks;
        }

        public String getName()
        {
            return this._name;
        }

        public String getTypeName()
        {
            return this._typeName;
        }

        public Integer getSize()
        {
            return this._size;
        }

        public Integer getScale()
        {
            return this._scale;
        }

        public String getNullable()
        {
            return this._nullable;
        }

        public String getDefaultValue()
        {
            return this._defaultValue;
        }

        public String getRemarks()
        {
            return this._remarks;
        }

        public Integer getSQLType()
        {
            return this._sqlType;
        }
    }

    public static class ForeignKeyInfo
    {

        private final String _pkSchemaName;

        private final String _pkTableName;

        private final String _pkTablePKColumnName;

        private final IntegrityActions _onUpdateAction;

        private final IntegrityActions _onDeleteAction;

        private final Deferrability _deferrability;

        private ForeignKeyInfo( String pkSchemaName, String pkTableName, String pkTablePKColumnName, short onUpdate,
            short onDelete, short deferrability )
        {
            this._pkSchemaName = pkSchemaName;
            this._pkTableName = pkTableName;
            this._pkTablePKColumnName = pkTablePKColumnName;
            this._onUpdateAction = _integrityActions.get( (int) onUpdate );
            this._onDeleteAction = _integrityActions.get( (int) onDelete );
            this._deferrability = _deferrabilities.get( (int) deferrability );
        }

        public String getPkSchemaName()
        {
            return this._pkSchemaName;
        }

        public String getPkTableName()
        {
            return this._pkTableName;
        }

        public String getPkTablePKColumnName()
        {
            return this._pkTablePKColumnName;
        }

        public IntegrityActions getOnUpdateAction()
        {
            return this._onUpdateAction;
        }

        public IntegrityActions getOnDeleteAction()
        {
            return this._onDeleteAction;
        }

        public Deferrability getDeferrability()
        {
            return this._deferrability;
        }

    }

    public static interface DatabaseProcessor
    {
        public void beginProcessSchemaInfo( String schemaName );

        public void endProcessSchemaInfo( String schemaName );

        public void beginProcessTableInfo( String schemaName, String tableName, String remarks );

        public void endProcessTableInfo( String schemaName, String tableName, String remarks );

        public void beginProcessColumns( String schemaName, String tableName, String tableRemarks );

        public void beginProcessColumnInfo( String schemaName, String tableName, ColumnInfo colInfo,
            ForeignKeyInfo fkInfo );

        public void endProcessColumnInfo( String schemaName, String tableName, ColumnInfo colInfo, ForeignKeyInfo fkInfo );

        public void endProcessColumns( String schemaName, String tableName, String tableRemarks );

        public void beginProcessRows( String schemaName, String tableName, String tableRemarks );

        public void beginProcessRowInfo( String schemaName, String tableName, Object[] rowContents );

        public void endProcessRowInfo( String schemaName, String tableName, Object[] rowContents );

        public void endProcessRows( String schemaName, String tableName, String tableRemarks );
    }

    public static abstract class DatabaseProcessorAdapter
        implements DatabaseProcessor
    {

        @Override
        public void beginProcessColumnInfo( String schemaName, String tableName, ColumnInfo colInfo,
            ForeignKeyInfo fkInfo )
        {
        }

        @Override
        public void beginProcessColumns( String schemaName, String tableName, String tableRemarks )
        {
        }

        @Override
        public void beginProcessRowInfo( String schemaName, String tableName, Object[] rowContents )
        {
        }

        @Override
        public void beginProcessRows( String schemaName, String tableName, String tableRemarks )
        {
        }

        @Override
        public void beginProcessSchemaInfo( String schemaName )
        {
        }

        @Override
        public void beginProcessTableInfo( String schemaName, String tableName, String remarks )
        {
        }

        @Override
        public void endProcessColumnInfo( String schemaName, String tableName, ColumnInfo colInfo, ForeignKeyInfo fkInfo )
        {
        }

        @Override
        public void endProcessColumns( String schemaName, String tableName, String tableRemarks )
        {
        }

        @Override
        public void endProcessRowInfo( String schemaName, String tableName, Object[] rowContents )
        {
        }

        @Override
        public void endProcessRows( String schemaName, String tableName, String tableRemarks )
        {
        }

        @Override
        public void endProcessSchemaInfo( String schemaName )
        {
        }

        @Override
        public void endProcessTableInfo( String schemaName, String tableName, String remarks )
        {
        }

    }

    public static void visitDatabaseTables( Connection connection, String catalogName, String schemaNamePattern,
        String tableNamePattern, DatabaseProcessor processor, SQLVendor sqlSyntaxVendor )
        throws SQLException
    {
        DatabaseMetaData metaData = connection.getMetaData();
        connection.setReadOnly( true );
        ResultSet rs = metaData.getTables( catalogName, schemaNamePattern, tableNamePattern, new String[]
        {
            "TABLE"
        } );
        try
        {
            while( rs.next() )
            {
                String schemaName = rs.getString( 2 );
                try
                {
                    processor.beginProcessSchemaInfo( schemaName );
                    String tableName = rs.getString( 3 );
                    String tableRemarks = rs.getString( 5 );
                    try
                    {
                        processor.beginProcessTableInfo( schemaName, tableName, tableRemarks );
                        List<ColumnInfo> colInfos = new ArrayList<ColumnInfo>();
                        ResultSet rsCols = metaData.getColumns( null, schemaName, tableName, null );
                        try
                        {
                            while( rsCols.next() )
                            {
                                String nullable = rsCols.getString( 18 );
                                colInfos.add( new ColumnInfo( rsCols.getString( 4 ), rsCols.getInt( 5 ), rsCols
                                    .getString( 6 ), rsCols.getInt( 7 ), rsCols.getInt( 9 ),
                                    nullable.length() > 0 ? Boolean.toString( nullable.equals( "YES" ) ) : "unknown",
                                    rsCols.getString( 13 ), rsCols.getString( 12 ) ) );
                            }
                        }
                        finally
                        {
                            rsCols.close();
                        }

                        rsCols = metaData.getImportedKeys( null, schemaName, tableName );
                        Map<String, ForeignKeyInfo> fkInfos = new HashMap<String, ForeignKeyInfo>();
                        try
                        {
                            while( rsCols.next() )
                            {
                                fkInfos.put(
                                    //
                                    rsCols.getString( 8 ), //
                                    new ForeignKeyInfo( rsCols.getString( 2 ), rsCols.getString( 3 ), rsCols
                                        .getString( 4 ), rsCols.getShort( 10 ), rsCols.getShort( 11 ), rsCols
                                        .getShort( 14 ) ) );
                            }
                        }
                        finally
                        {
                            rsCols.close();
                        }

                        try
                        {
                            processor.beginProcessColumns( schemaName, tableName, tableRemarks );
                            for( ColumnInfo colInfo : colInfos )
                            {
                                try
                                {
                                    processor.beginProcessColumnInfo( schemaName, tableName, colInfo,
                                        fkInfos.get( colInfo._name ) );
                                }
                                finally
                                {
                                    processor.endProcessColumnInfo( schemaName, tableName, colInfo,
                                        fkInfos.get( colInfo._name ) );
                                }
                            }
                        }
                        finally
                        {
                            processor.endProcessColumns( schemaName, tableName, tableRemarks );
                        }

                        QueryFactory q = sqlSyntaxVendor.getQueryFactory();
                        TableReferenceFactory t = sqlSyntaxVendor.getTableReferenceFactory();
                        QuerySpecificationBuilder builda = q.querySpecificationBuilder();
                        builda.getSelect().selectAll();
                        builda.getFrom().addTableReferences(
                            t.tableBuilder( t.table( t.tableName( schemaName, tableName ) ) ) );
                        String sql = sqlSyntaxVendor.toString( q.createQuery( builda.createExpression() ) );
                        Statement stmt = connection.createStatement();
                        ResultSet rowsRs = null;
                        try
                        {
                            rowsRs = stmt.executeQuery( sql );
                            processor.beginProcessRows( schemaName, tableName, tableRemarks );
                            while( rowsRs.next() )
                            {
                                Object[] rowContents = new Object[colInfos.size()];
                                for( Integer x = 0; x < rowContents.length; ++x )
                                {
                                    rowContents[x] = rowsRs.getObject( x + 1 );
                                }

                                try
                                {
                                    processor.beginProcessRowInfo( schemaName, tableName, rowContents );
                                }
                                finally
                                {
                                    processor.endProcessRowInfo( schemaName, tableName, rowContents );
                                }
                            }
                        }
                        finally
                        {
                            processor.endProcessRows( schemaName, tableName, tableRemarks );
                            if( rowsRs != null )
                            {
                                rowsRs.close();
                            }
                            stmt.close();
                        }
                    }
                    finally
                    {
                        processor.endProcessTableInfo( schemaName, tableName, tableRemarks );
                    }
                }
                finally
                {
                    processor.endProcessSchemaInfo( schemaName );
                }
            }
        }
        finally
        {
            SQLUtil.closeQuietly( rs );
        }
    }
}
