/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.index.sql.support.skeletons;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.sql.DataSource;
import org.apache.polygene.api.PolygeneAPI;
import org.apache.polygene.api.common.QualifiedName;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.property.StateHolder;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.api.value.ValueDescriptor;
import org.apache.polygene.index.sql.support.api.SQLIndexing;
import org.apache.polygene.index.sql.support.common.DBNames;
import org.apache.polygene.index.sql.support.common.QNameInfo;
import org.apache.polygene.index.sql.support.common.QNameInfo.QNameType;
import org.apache.polygene.index.sql.support.postgresql.PostgreSQLTypeHelper;
import org.apache.polygene.index.sql.support.skeletons.SQLSkeletonUtil.Lazy;
import org.apache.polygene.library.sql.common.SQLUtil;
import org.apache.polygene.library.sql.generator.grammar.builders.modification.ColumnSourceByValuesBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.modification.DeleteBySearchBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.modification.UpdateBySearchBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.query.QuerySpecificationBuilder;
import org.apache.polygene.library.sql.generator.grammar.factories.BooleanFactory;
import org.apache.polygene.library.sql.generator.grammar.factories.ColumnsFactory;
import org.apache.polygene.library.sql.generator.grammar.factories.LiteralFactory;
import org.apache.polygene.library.sql.generator.grammar.factories.ModificationFactory;
import org.apache.polygene.library.sql.generator.grammar.factories.QueryFactory;
import org.apache.polygene.library.sql.generator.grammar.factories.TableReferenceFactory;
import org.apache.polygene.library.sql.generator.grammar.modification.DeleteStatement;
import org.apache.polygene.library.sql.generator.grammar.modification.InsertStatement;
import org.apache.polygene.library.sql.generator.grammar.modification.UpdateSourceByExpression;
import org.apache.polygene.library.sql.generator.grammar.modification.UpdateStatement;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpression;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;
import org.apache.polygene.spi.PolygeneSPI;
import org.apache.polygene.spi.entity.EntityState;
import org.apache.polygene.spi.entity.EntityStatus;
import org.apache.polygene.spi.entitystore.EntityStoreException;

import static org.apache.polygene.index.sql.support.common.DBNames.ENTITY_TABLE_NAME;
import static org.apache.polygene.index.sql.support.common.DBNames.ENTITY_TYPES_JOIN_TABLE_NAME;

/**
 * TODO SQL-Generator now has support for auto-generated columns. Use it in indexing ( should make
 * some things quite faster and simpler ).
 */
public abstract class AbstractSQLIndexing
    implements SQLIndexing
{

    protected static final Integer AMOUNT_OF_COLUMNS_IN_ENTITY_TABLE = 5;

    @SuppressWarnings( "WeakerAccess" )
    protected static final Integer AMOUNT_OF_COLUMNS_IN_ALL_QNAMES_TABLE = 2;

    @SuppressWarnings( "WeakerAccess" )
    protected static final Integer AMOUNT_OF_COLUMNS_IN_ASSO_TABLE = 2;

    @SuppressWarnings( "WeakerAccess" )
    protected static final Integer AMOUNT_OF_COLUMNS_IN_MANY_ASSO_TABLE = 3;

    @Structure
    private Application _app;

    @Structure
    private PolygeneSPI _qi4SPI;

    @This
    private SQLDBState _state;

    @This
    private PostgreSQLTypeHelper _sqlTypeHelper;

    @Uses
    private ServiceDescriptor descriptor;

    @Service
    private DataSource _dataSource;

    @Override
    public void indexEntities( Iterable<EntityState> changedStates )
        throws SQLException
    {
        final String schemaName = this._state.schemaName().get();
        final SQLVendor vendor = this.descriptor.metaInfo( SQLVendor.class );
        Connection connectionTest = AbstractSQLStartup.CONNECTION_FOR_REINDEXING.get();
        boolean connectionFromStartupWasNull = connectionTest == null;
        boolean wasAutoCommit = false;
        boolean wasReadOnly = false;
        if( connectionFromStartupWasNull )
        {
            connectionTest = this._dataSource.getConnection();
        }
        else
        {
            wasAutoCommit = connectionTest.getAutoCommit();
            wasReadOnly = connectionTest.isReadOnly();
        }
        final Connection connection = connectionTest;

        PreparedStatement updateEntityTablePS = null;
        PreparedStatement removeEntityPS = null;
        PreparedStatement insertToPropertyQNamesPS = null;
        PreparedStatement clearEntityDataPS = null;
        Lazy<PreparedStatement, SQLException> queryEntityPKPS = new Lazy<>(
            () -> connection.prepareStatement(
                vendor.toString( createQueryEntityPkByIdentityStatement( schemaName, vendor ) ) ) );
        Lazy<PreparedStatement, SQLException> insertToEntityTableAutoGenerated = new Lazy<>(
            () -> connection.prepareStatement(
                createInsertStatementWithAutoGeneratedIDForEntitiesTable( schemaName,
                                                                          ENTITY_TABLE_NAME,
                                                                          vendor ).toString() ) );
        Lazy<PreparedStatement, SQLException> insertToEntityTypeTablePS = new Lazy<>(
            () -> connection.prepareStatement(
                createInsertEntityTypeStatement( schemaName, vendor ).toString() ) );
        Map<QualifiedName, PreparedStatement> qNameInsertPSs = new HashMap<>();

        try
        {
            connection.setAutoCommit( false );
            connection.setReadOnly( false );

            // TODO cache all queries.
            updateEntityTablePS = connection.prepareStatement(
                this.createUpdateEntityTableStatement( schemaName, vendor ).toString() );
            removeEntityPS = connection.prepareStatement(
                this.createDeleteFromEntityTableStatement( schemaName, vendor ).toString() );
            insertToPropertyQNamesPS = connection.prepareStatement(
                vendor.toString( this.createInsertStatement( schemaName,
                                                             DBNames.ALL_QNAMES_TABLE_NAME,
                                                             AMOUNT_OF_COLUMNS_IN_ALL_QNAMES_TABLE,
                                                             vendor ) ) );
            clearEntityDataPS = connection.prepareStatement(
                this.createClearEntityDataStatement( schemaName, vendor ).toString() );

            Map<Long, EntityState> statesByPK = new HashMap<>();
            Map<Long, Integer> qNamePKs = new HashMap<>();

            Iterable<EntityState> relatedStates = StreamSupport.stream( changedStates.spliterator(), false )
                                                               .filter( state -> state.entityDescriptor().queryable() )
                                                               .map( SQLCompatEntityStateWrapper.WRAP )
                                                               .collect( Collectors.toList() );

            for( EntityState eState : relatedStates )
            {
                EntityStatus status = eState.status();
                Long pk = null;
                boolean needToInsert = status.equals( EntityStatus.NEW );
                if( !needToInsert )
                {
                    if( status.equals( EntityStatus.UPDATED ) )
                    {
                        pk = this.findEntityPK( eState, queryEntityPKPS );
                        if( pk == null )
                        {
                            // Happens when reindexing
                            needToInsert = true;
                        }
                        else
                        {
                            // TODO if multiple applications with different application model use
                            // indexing, need to sync type-table.
                            this.updateEntityInfoAndProperties( connection, qNameInsertPSs,
                                                                insertToPropertyQNamesPS,
                                                                clearEntityDataPS,
                                                                updateEntityTablePS,
                                                                eState,
                                                                pk, qNamePKs );
                        }
                    }
                    else if( status.equals( EntityStatus.REMOVED ) )
                    {
                        this.removeEntity( eState, removeEntityPS );
                    }
//                    else
//                    {
//                        // TODO possibly handle LOADED state somehow
//                        // throw new
//                        // UnsupportedOperationException("Did not understand what to do with state [id = "
//                        // +
//                        // eState.reference().reference() + ", status = " + status + "].");
//                    }
                }

                if( needToInsert )
                {
                    pk = this.getPKFromAutoGeneratedIDInsert(
                        eState,
                        insertToEntityTableAutoGenerated.getValue(),
                        vendor,
                        connection );
                    insertPropertyType( insertToEntityTypeTablePS.getValue(), eState, pk );
                    insertProperties( connection, qNameInsertPSs, insertToPropertyQNamesPS, eState, pk, qNamePKs );
                }
                if( pk != null )
                {
                    statesByPK.put( pk, eState );
                }
            }

            removeEntityPS.executeBatch();
            updateEntityTablePS.executeBatch();
            clearEntityDataPS.executeBatch();
            if( insertToEntityTypeTablePS.hasValue() )
            {
                insertToEntityTypeTablePS.getValue().executeBatch();
            }

            for( Map.Entry<Long, EntityState> entry : statesByPK.entrySet() )
            {
                EntityState eState = entry.getValue();
                Long pk = entry.getKey();
                this.insertAssoAndManyAssoQNames( qNameInsertPSs, insertToPropertyQNamesPS,
                                                  eState,
                                                  qNamePKs.get( pk ), pk );
            }

            insertToPropertyQNamesPS.executeBatch();

            for( PreparedStatement ps : qNameInsertPSs.values() )
            {
                ps.executeBatch();
            }

            connection.commit();
        }
        catch( SQLException sqle )
        {
            sqle = SQLUtil.withAllSQLExceptions( sqle );
            SQLUtil.rollbackQuietly( connection, sqle );
            throw sqle;
        }
        finally
        {
            try
            {
                if( queryEntityPKPS.hasValue() )
                {
                    SQLUtil.closeQuietly( queryEntityPKPS.getValue() );
                }
                if( insertToEntityTableAutoGenerated.hasValue() )
                {
                    SQLUtil.closeQuietly( insertToEntityTableAutoGenerated.getValue() );
                }
                SQLUtil.closeQuietly( updateEntityTablePS );
                SQLUtil.closeQuietly( removeEntityPS );
                SQLUtil.closeQuietly( insertToPropertyQNamesPS );
                SQLUtil.closeQuietly( clearEntityDataPS );
                qNameInsertPSs.values().forEach( SQLUtil::closeQuietly );
            }
            finally
            {
                if( connectionFromStartupWasNull )
                {
                    SQLUtil.closeQuietly( connection );
                }
                else
                {
                    connection.setReadOnly( wasReadOnly );
                    connection.setAutoCommit( wasAutoCommit );
                }
            }
        }
    }

    @SuppressWarnings( "WeakerAccess" )
    protected InsertStatement createInsertStatement( String schemaName, String tableName,
                                                     Integer amountOfColumns,
                                                     SQLVendor vendor
                                                   )
    {
        ModificationFactory m = vendor.getModificationFactory();
        LiteralFactory l = vendor.getLiteralFactory();
        TableReferenceFactory t = vendor.getTableReferenceFactory();

        ColumnSourceByValuesBuilder columnBuilder = m.columnSourceByValues();
        for( Integer x = 0; x < amountOfColumns; ++x )
        {
            columnBuilder.addValues( l.param() );
        }

        return m.insert().setTableName( t.tableName( schemaName, tableName ) )
                .setColumnSource( columnBuilder.createExpression() ).createExpression();
    }

    protected abstract InsertStatement createInsertStatementWithAutoGeneratedIDForEntitiesTable(
        String schemaName, String tableName, SQLVendor vendor );

    @SuppressWarnings( "SameParameterValue" )
    protected void addEntityInfoToInsertToEntityTablePS( EntityState state, PreparedStatement ps, int startingIndex )
        throws SQLException
    {
        ps.setString( startingIndex, state.entityReference().identity().toString() );
        ps.setTimestamp( startingIndex + 1, Timestamp.from( state.lastModified() ) );
        ps.setString( startingIndex + 2, state.version() );
        ps.setString( startingIndex + 3, this._app.version() );
    }

    @SuppressWarnings( "WeakerAccess" )
    protected Long findEntityPK( EntityState state, Lazy<PreparedStatement, SQLException> queryPKPS )
        throws SQLException
    {
        // TODO build cache: Polygene Identity -> PK
        Long entityPK = null;
        PreparedStatement ps = queryPKPS.getValue();
        ps.setString( 1, state.entityReference().identity().toString() );
        ResultSet rs = null;
        try
        {
            rs = ps.executeQuery();

            if( rs.next() )
            {
                entityPK = rs.getLong( 1 );
            }
        }
        finally
        {
            SQLUtil.closeQuietly( rs );
        }

        return entityPK;
    }

    protected abstract long getPKFromAutoGeneratedIDInsert(
        EntityState state, PreparedStatement autoGeneratedIDStatement, SQLVendor vendor,
        Connection connection
                                                          )
        throws SQLException;

    @SuppressWarnings( "WeakerAccess" )
    protected UpdateStatement createUpdateEntityTableStatement( String schemaName, SQLVendor vendor )
    {
        ModificationFactory m = vendor.getModificationFactory();
        BooleanFactory b = vendor.getBooleanFactory();
        LiteralFactory l = vendor.getLiteralFactory();
        ColumnsFactory c = vendor.getColumnsFactory();
        TableReferenceFactory t = vendor.getTableReferenceFactory();

        // "UPDATE " + "%s" + "." + ENTITY_TABLE_NAME + "\n" + //
        // "SET " + ENTITY_TABLE_IDENTITY_COLUMN_NAME + " = ?, " + //
        // ENTITY_TABLE_MODIFIED_COLUMN_NAME + " = ?, " + //
        // ENTITY_TABLE_VERSION_COLUMN_NAME + " = ?, " + //
        // ENTITY_TABLE_APPLICATION_VERSION_COLUMN_NAME + " = ?" + "\n" + //
        // "WHERE " + ENTITY_TABLE_PK_COLUMN_NAME + " = ?" + "\n" + //
        // ";" //
        UpdateSourceByExpression paramSource = m.updateSourceByExp( l.param() );
        UpdateBySearchBuilder builder = m.updateBySearch();
        builder
            .setTargetTable(
                m.createTargetTable( t.tableName( schemaName, DBNames.ENTITY_TABLE_NAME ) ) )
            .addSetClauses( m.setClause( DBNames.ENTITY_TABLE_IDENTITY_COLUMN_NAME, paramSource ),
                            m.setClause( DBNames.ENTITY_TABLE_MODIFIED_COLUMN_NAME, paramSource ),
                            m.setClause( DBNames.ENTITY_TABLE_VERSION_COLUMN_NAME, paramSource ),
                            m.setClause( DBNames.ENTITY_TABLE_APPLICATION_VERSION_COLUMN_NAME, paramSource ) )
            .getWhereBuilder()
            .reset( b.eq( c.colName( DBNames.ENTITY_TABLE_PK_COLUMN_NAME ), l.param() ) );

        return builder.createExpression();
    }

    @SuppressWarnings( "WeakerAccess" )
    protected QueryExpression createQueryEntityPkByIdentityStatement( String schemaName, SQLVendor vendor )
    {
        BooleanFactory b = vendor.getBooleanFactory();
        LiteralFactory l = vendor.getLiteralFactory();
        ColumnsFactory c = vendor.getColumnsFactory();
        TableReferenceFactory t = vendor.getTableReferenceFactory();
        QueryFactory q = vendor.getQueryFactory();

        // "SELECT " + ENTITY_TABLE_PK_COLUMN_NAME + "\n" + //
        // "FROM " + "%s" + "." + ENTITY_TABLE_NAME + "\n" + //
        // "WHERE " + ENTITY_TABLE_IDENTITY_COLUMN_NAME + " = ?" + "\n" + //
        // ";" //
        QuerySpecificationBuilder query = q.querySpecificationBuilder();
        query.getSelect().addUnnamedColumns( c.colName( DBNames.ENTITY_TABLE_PK_COLUMN_NAME ) );
        query.getFrom().addTableReferences(
            t.tableBuilder( t.table( t.tableName( schemaName, DBNames.ENTITY_TABLE_NAME ) ) ) );
        query.getWhere().reset(
            b.eq( c.colName( DBNames.ENTITY_TABLE_IDENTITY_COLUMN_NAME ), l.param() ) );

        return q.createQuery( query.createExpression() );
    }

    @SuppressWarnings( "WeakerAccess" )
    protected DeleteStatement createDeleteFromEntityTableStatement( String schemaName, SQLVendor vendor )
    {
        return this.createDeleteFromTableStatement( schemaName, DBNames.ENTITY_TABLE_NAME,
                                                    DBNames.ENTITY_TABLE_IDENTITY_COLUMN_NAME, vendor );
    }

    @SuppressWarnings( "WeakerAccess" )
    protected DeleteStatement createClearEntityDataStatement( String schemaName, SQLVendor vendor )
    {
        return this.createDeleteFromTableStatement( schemaName, DBNames.ALL_QNAMES_TABLE_NAME,
                                                    DBNames.ENTITY_TABLE_PK_COLUMN_NAME, vendor );
    }

    @SuppressWarnings( "WeakerAccess" )
    protected DeleteStatement createDeleteFromTableStatement( String schemaName, String tableName,
                                                              String columnName,
                                                              SQLVendor vendor
                                                            )
    {
        ModificationFactory m = vendor.getModificationFactory();
        BooleanFactory b = vendor.getBooleanFactory();
        LiteralFactory l = vendor.getLiteralFactory();
        ColumnsFactory c = vendor.getColumnsFactory();
        TableReferenceFactory t = vendor.getTableReferenceFactory();

        // "DELETE FROM " + "%s" + "." + "%s" + "\n" + //
        // "WHERE " + "%s" + " = ? " + "\n" + //
        // ";" //
        DeleteBySearchBuilder delete = m.deleteBySearch();
        delete.setTargetTable( m.createTargetTable( t.tableName( schemaName, tableName ) ) )
              .getWhere()
              .reset( b.eq( c.colName( columnName ), l.param() ) );

        return delete.createExpression();
    }

    @SuppressWarnings( "WeakerAccess" )
    protected InsertStatement createPropertyInsert( QNameInfo qNameInfo, SQLVendor vendor )
    {
        String tableName = qNameInfo.getTableName();
        ModificationFactory m = vendor.getModificationFactory();
        TableReferenceFactory t = vendor.getTableReferenceFactory();
        LiteralFactory l = vendor.getLiteralFactory();

        ColumnSourceByValuesBuilder columnBuilder = m.columnSourceByValues()
                                                     .addValues( l.param(), l.param(), l.param() );
        if( qNameInfo.getCollectionDepth() > 0 )
        {
            columnBuilder.addValues( l.func( "text2ltree", l.param() ) );
        }
        columnBuilder.addValues( l.param() );

        return m.insert().setTableName( t.tableName( this._state.schemaName().get(), tableName ) )
                .setColumnSource( columnBuilder.createExpression() ).createExpression();
    }

    @SuppressWarnings( "WeakerAccess" )
    protected InsertStatement createAssoInsert( QNameInfo qNameInfo, SQLVendor vendor,
                                                Integer amountOfParams
                                              )
    {
        ModificationFactory m = vendor.getModificationFactory();
        LiteralFactory l = vendor.getLiteralFactory();
        ColumnsFactory c = vendor.getColumnsFactory();
        QueryFactory q = vendor.getQueryFactory();
        TableReferenceFactory t = vendor.getTableReferenceFactory();
        BooleanFactory b = vendor.getBooleanFactory();
        String schemaName = this._state.schemaName().get();

        // "INSERT INTO " + "%s" + "." + "%s" + "\n" + //
        // "SELECT " + "?, " + "?, " + ENTITY_TABLE_PK_COLUMN_NAME + "\n" + // <-- here is 4 params
        // when many-asso
        // "FROM " + "%s" + "." + ENTITY_TABLE_NAME + "\n" + //
        // "WHERE " + ENTITY_TABLE_IDENTITY_COLUMN_NAME + " = " + "?";
        QuerySpecificationBuilder qBuilder = q.querySpecificationBuilder();
        for( Integer x = 0; x < amountOfParams; ++x )
        {
            qBuilder.getSelect().addUnnamedColumns( c.colExp( l.param() ) );
        }
        qBuilder.getSelect().addUnnamedColumns( c.colName( DBNames.ENTITY_TABLE_PK_COLUMN_NAME ) );
        qBuilder.getFrom().addTableReferences(
            t.tableBuilder( t.table( t.tableName( schemaName, DBNames.ENTITY_TABLE_NAME ) ) ) );
        qBuilder.getWhere().reset(
            b.eq( c.colName( DBNames.ENTITY_TABLE_IDENTITY_COLUMN_NAME ), l.param() ) );

        return m
            .insert()
            .setTableName( t.tableName( schemaName, qNameInfo.getTableName() ) )
            .setColumnSource( m.columnSourceByQuery( q.createQuery( qBuilder.createExpression() ) ) )
            .createExpression();
    }

    @SuppressWarnings( "WeakerAccess" )
    protected InsertStatement createInsertEntityTypeStatement( String schemaName, SQLVendor vendor )
    {
        return this.createInsertStatement( schemaName, ENTITY_TYPES_JOIN_TABLE_NAME, 2, vendor );
    }

    private void syncQNamesInsertPSs( Connection connection,
                                      Map<QualifiedName, PreparedStatement> qNameInsertPSs,
                                      Set<QualifiedName> qNames
                                    )
        throws SQLException
    {
        Set<QualifiedName> copy = new HashSet<>( qNames );
        copy.removeAll( qNameInsertPSs.keySet() );
        for( QualifiedName qName : copy )
        {
            QNameInfo info = this._state.qNameInfos().get().get( qName );
            if( info == null )
            {
                throw new SqlIndexingException( "Could not find database information about qualified name [" + qName + "]" );
            }

            QNameType type = info.getQNameType();
            if( type.equals( QNameType.PROPERTY ) )
            {
                qNameInsertPSs.put( qName, this.createInsertPropertyPS( connection, info ) );
            }
            else if( type.equals( QNameType.ASSOCIATION ) )
            {
                qNameInsertPSs.put( qName, this.createInsertAssociationPS( connection, info ) );
            }
            else if( type.equals( QNameType.MANY_ASSOCIATION ) )
            {
                qNameInsertPSs.put( qName, this.createInsertManyAssociationPS( connection, info ) );
            }
            else
            {
                throw new SqlIndexingException( "Did not know what to do with QName of type " + type + "." );
            }
        }
    }

    private PreparedStatement createInsertPropertyPS( Connection connection, QNameInfo qNameInfo )
        throws SQLException
    {
        SQLVendor vendor = this.descriptor.metaInfo( SQLVendor.class );
        return connection.prepareStatement( vendor.toString( this.createPropertyInsert( qNameInfo,
                                                                                        vendor ) ) );
    }

    private PreparedStatement
    createInsertAssociationPS( Connection connection, QNameInfo qNameInfo )
        throws SQLException
    {
        SQLVendor vendor = this.descriptor.metaInfo( SQLVendor.class );
        return connection.prepareStatement( vendor.toString( this.createAssoInsert( qNameInfo,
                                                                                    vendor,
                                                                                    AMOUNT_OF_COLUMNS_IN_ASSO_TABLE ) ) );
    }

    private PreparedStatement createInsertManyAssociationPS( Connection connection,
                                                             QNameInfo qNameInfo
                                                           )
        throws SQLException
    {
        SQLVendor vendor = this.descriptor.metaInfo( SQLVendor.class );
        return connection.prepareStatement( vendor.toString( this.createAssoInsert( qNameInfo,
                                                                                    vendor,
                                                                                    AMOUNT_OF_COLUMNS_IN_MANY_ASSO_TABLE ) ) );
    }

    private void clearAllEntitysQNames( PreparedStatement clearPropertiesPS, Long pk )
        throws SQLException
    {
        clearPropertiesPS.setLong( 1, pk );
        clearPropertiesPS.addBatch();
    }

    private Integer insertPropertyQNames( Connection connection,
                                          Map<QualifiedName, PreparedStatement> qNameInsertPSs,
                                          PreparedStatement insertAllQNamesPS, EntityState state, Long entityPK
                                        )
        throws SQLException
    {
        Set<QualifiedName> qNames = this._state.entityUsedQNames().get().get( state.entityDescriptor() );
        this.syncQNamesInsertPSs( connection, qNameInsertPSs, qNames );
        final Integer propertyPK[] = { 0 };
        state.entityDescriptor().state().properties()
             .filter( pDesc -> SQLSkeletonUtil.isQueryable( pDesc.accessor() ) )
             .forEach( pDesc -> {
                 try
                 {
                     propertyPK[ 0 ] = this.insertProperty(
                         qNameInsertPSs,
                         insertAllQNamesPS,
                         propertyPK[ 0 ],
                         entityPK,
                         pDesc.qualifiedName(),
                         state.propertyValueOf( pDesc.qualifiedName() ),
                         null //
                                                          );
                 }
                 catch( SQLException e )
                 {
                     throw new EntityStoreException( "Underlying exception when inserting property " + pDesc,
                                                     SQLUtil.withAllSQLExceptions( e ) );
                 }
             } );
        return propertyPK[ 0 ];
    }

    private void insertAssoAndManyAssoQNames( Map<QualifiedName, PreparedStatement> qNameInsertPSs,
                                              PreparedStatement insertToAllQNamesPS,
                                              EntityState state,
                                              final Integer localPK,
                                              Long entityPK
                                            )
        throws SQLException
    {
        final Integer[] qNamePK = { localPK };
        state.entityDescriptor().state().associations()
             .filter( aDesc -> SQLSkeletonUtil.isQueryable( aDesc.accessor() ) )
             .forEach( aDesc -> {
                 try
                 {
                     QualifiedName qName = aDesc.qualifiedName();
                     PreparedStatement ps = qNameInsertPSs.get( qName );
                     EntityReference ref = state.associationValueOf( qName );
                     if( ref != null )
                     {
                         insertToAllQNamesPS.setInt( 1, qNamePK[ 0 ] );
                         insertToAllQNamesPS.setLong( 2, entityPK );
                         insertToAllQNamesPS.addBatch();

                         ps.setInt( 1, qNamePK[ 0 ] );
                         ps.setLong( 2, entityPK );
                         ps.setString( 3, ref.identity().toString() );
                         ps.addBatch();

                         qNamePK[ 0 ] += 1;
                     }
                 }
                 catch( SQLException e )
                 {
                     throw new EntityStoreException( "Underlying exception when inserting association " + aDesc,
                                                     SQLUtil.withAllSQLExceptions( e ) );
                 }
             } );

        state.entityDescriptor().state().manyAssociations()
             .filter( mDesc -> SQLSkeletonUtil.isQueryable( mDesc.accessor() ) )
             .forEach( mDesc -> {
                 try
                 {
                     QualifiedName qName = mDesc.qualifiedName();
                     PreparedStatement ps = qNameInsertPSs.get( qName );
                     Integer index = 0;
                     for( EntityReference ref : state.manyAssociationValueOf( qName ) )
                     {
                         if( ref != null )
                         {
                             insertToAllQNamesPS.setInt( 1, qNamePK[ 0 ] );
                             insertToAllQNamesPS.setLong( 2, entityPK );
                             insertToAllQNamesPS.addBatch();

                             ps.setInt( 1, qNamePK[ 0 ] );
                             ps.setLong( 2, entityPK );
                             ps.setInt( 3, index );
                             ps.setString( 4, ref.identity().toString() );
                             ps.addBatch();
                             qNamePK[ 0 ] += 1;
                         }
                         ++index;
                     }
                 }
                 catch( SQLException e )
                 {
                     throw new EntityStoreException( "Underlying exception when inserting manyassociation " + mDesc,
                                                     SQLUtil.withAllSQLExceptions( e ) );
                 }
             } );
    }

    private Integer insertProperty(
        Map<QualifiedName, PreparedStatement> qNameInsertPSs,
        PreparedStatement insertAllQNamesPS,
        Integer propertyPK,
        Long entityPK,
        QualifiedName qName,
        Object property,
        Integer parentQNameID
                                  )
        throws SQLException
    {
        Integer result = propertyPK;
        if( property != null )
        {
            if( !qName.type().equals( HasIdentity.class.getName() ) )
            {
                QNameInfo info = this._state.qNameInfos().get().get( qName );
                if( info.getCollectionDepth() > 0 )
                {
                    result = this.storeCollectionProperty( qNameInsertPSs, insertAllQNamesPS,
                                                           propertyPK, entityPK,
                                                           qName, (Collection<?>) property, parentQNameID );
                }
                else if( info.isFinalTypePrimitive() )
                {
                    result = this.storePrimitiveProperty( qNameInsertPSs, insertAllQNamesPS, propertyPK,
                                                          entityPK,
                                                          qName, property, parentQNameID );
                }
                else
                {
                    result = this.storeValueCompositeProperty( qNameInsertPSs, insertAllQNamesPS,
                                                               propertyPK, entityPK,
                                                               qName, property, parentQNameID );
                }
            }
        }
        return result;
    }

    private Integer storeCollectionProperty(
        Map<QualifiedName, PreparedStatement> qNameInsertPSs,
        PreparedStatement insertAllQNamesPS,
        Integer propertyPK,
        Long entityPK,
        QualifiedName qName,
        Collection<?> property,
        Integer parentQNameID
                                           )
        throws SQLException
    {
        QNameInfo info = this._state.qNameInfos().get().get( qName );
        PreparedStatement ps = qNameInsertPSs.get( qName );
        propertyPK = this.storeCollectionInfo( insertAllQNamesPS, propertyPK, entityPK, parentQNameID, ps,
                                               info );

        propertyPK = this.storeCollectionItems( qNameInsertPSs, property, insertAllQNamesPS,
                                                DBNames.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME, ps,
                                                propertyPK, entityPK,
                                                parentQNameID, info.getFinalType(), info.isFinalTypePrimitive() );
        return propertyPK;
    }

    private Integer storeCollectionInfo( PreparedStatement insertAllQNamesPS, Integer propertyPK,
                                         Long entityPK,
                                         Integer parentQNameID, PreparedStatement ps, QNameInfo info
                                       )
        throws SQLException
    {
        insertAllQNamesPS.setInt( 1, propertyPK );
        insertAllQNamesPS.setLong( 2, entityPK );
        insertAllQNamesPS.addBatch();

        ps.setInt( 1, propertyPK );
        ps.setLong( 2, entityPK );
        ps.setObject( 3, parentQNameID, Types.BIGINT );

        //noinspection SuspiciousNameCombination
        ps.setString( 4, DBNames.QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME );
        if( info.isFinalTypePrimitive() )
        {
            this.storePrimitiveUsingPS( ps, 5, null, info.getFinalType() );
        }
        else
        {
            this.storeVCClassIDUsingPS( ps, 5, null );
        }
        ps.addBatch();

        return propertyPK + 1;
    }

    private Integer storeCollectionItems( Map<QualifiedName, PreparedStatement> qNameInsertPSs,
                                          Collection<?> collection,
                                          PreparedStatement insertAllQNamesPS,
                                          String path,
                                          PreparedStatement ps,
                                          Integer propertyPK,
                                          Long entityPK,
                                          Integer parentPK,
                                          Type finalType,
                                          Boolean isFinalTypePrimitive
                                        )
        throws SQLException
    {
        Integer index = 0;
        for( Object o : collection )
        {
            String itemPath = path + DBNames.QNAME_TABLE_COLLECTION_PATH_SEPARATOR + index;
            if( o instanceof Collection<?> )
            {
                propertyPK = this.storeCollectionItems(
                    qNameInsertPSs, (Collection<?>) o,
                    insertAllQNamesPS, itemPath,
                    ps, propertyPK, entityPK, parentPK, finalType,
                    isFinalTypePrimitive );
            }
            else
            {
                propertyPK = this.storeCollectionItem(
                    qNameInsertPSs, ps, insertAllQNamesPS, propertyPK,
                    entityPK,
                    parentPK, itemPath, o, isFinalTypePrimitive, finalType );
                ps.addBatch();
            }
            ++index;
        }
        return propertyPK;
    }

    private Integer storeCollectionItem(
        Map<QualifiedName, PreparedStatement> qNameInsertPSs, PreparedStatement ps,
        PreparedStatement insertAllQNamesPS,
        Integer propertyPK,
        Long entityPK,
        Integer parentPK,
        String path,
        Object item,
        Boolean isFinalTypePrimitive,
        Type finalType
                                       )
        throws SQLException
    {
        insertAllQNamesPS.setInt( 1, propertyPK );
        insertAllQNamesPS.setLong( 2, entityPK );
        insertAllQNamesPS.addBatch();

        ps.setInt( 1, propertyPK );
        ps.setLong( 2, entityPK );
        ps.setObject( 3, parentPK, Types.INTEGER );
        ps.setString( 4, path );
        if( isFinalTypePrimitive )
        {
            this.storePrimitiveUsingPS( ps, 5, item, finalType );
            ++propertyPK;
        }
        else
        {
            this.storeVCClassIDUsingPS( ps, 5, item );
            propertyPK = this.storePropertiesOfVC( qNameInsertPSs, insertAllQNamesPS, propertyPK, entityPK, item );
        }

        return propertyPK;
    }

    private Integer storePrimitiveProperty(
        Map<QualifiedName, PreparedStatement> qNameInsertPSs,
        PreparedStatement insertAllQNamesPS,
        Integer propertyPK,
        Long entityPK,
        QualifiedName qName,
        Object property,
        Integer parentQNameID
                                          )
        throws SQLException
    {
        QNameInfo info = this._state.qNameInfos().get().get( qName );
        insertAllQNamesPS.setInt( 1, propertyPK );
        insertAllQNamesPS.setLong( 2, entityPK );
        insertAllQNamesPS.addBatch();

        PreparedStatement ps = qNameInsertPSs.get( qName );
        ps.setInt( 1, propertyPK );
        ps.setLong( 2, entityPK );
        ps.setObject( 3, parentQNameID, Types.INTEGER );
        Type type = info.getFinalType();
        this.storePrimitiveUsingPS( ps, 4, property, type );
        ps.addBatch();

        return propertyPK + 1;
    }

    private Integer storeValueCompositeProperty(
        Map<QualifiedName, PreparedStatement> qNameInsertPSs,
        PreparedStatement insertAllQNamesPS,
        Integer propertyPK,
        Long entityPK,
        QualifiedName qName,
        Object property,
        Integer parentQNameID
                                               )
        throws SQLException
    {

        PreparedStatement ps = qNameInsertPSs.get( qName );
        insertAllQNamesPS.setInt( 1, propertyPK );
        insertAllQNamesPS.setLong( 2, entityPK );
        insertAllQNamesPS.addBatch();

        ps.setInt( 1, propertyPK );
        ps.setLong( 2, entityPK );
        ps.setObject( 3, parentQNameID, Types.INTEGER );
        this.storeVCClassIDUsingPS( ps, 4, property );
        ps.addBatch();

        return this.storePropertiesOfVC( qNameInsertPSs, insertAllQNamesPS, propertyPK, entityPK,
                                         property );
    }

    private Integer storePropertiesOfVC( Map<QualifiedName, PreparedStatement> qNameInsertPSs,
                                         PreparedStatement insertAllQNamesPS,
                                         Integer localPK,
                                         Long entityPK,
                                         Object property
                                       )
        throws SQLException
    {
        ValueDescriptor vDesc = this._qi4SPI.valueDescriptorFor( property );
        StateHolder state = PolygeneAPI.FUNCTION_COMPOSITE_INSTANCE_OF.apply( (ValueComposite) property ).state();
        Integer propertyPK[] = { localPK + 1 };
        vDesc.state().properties().forEach( pDesc -> {

            try
            {
                propertyPK[ 0 ] = this.insertProperty(
                    qNameInsertPSs,
                    insertAllQNamesPS,
                    propertyPK[ 0 ],
                    entityPK,
                    pDesc.qualifiedName(),
                    state.propertyFor( pDesc.accessor() ).get(),
                    localPK
                                                     );
            }
            catch( SQLException e )
            {
                throw new EntityStoreException( "Underlying exception when inserting property " + pDesc + " in value " + vDesc,
                                                SQLUtil.withAllSQLExceptions( e ) );
            }
        } );

        return propertyPK[ 0 ];
    }

    private void storePrimitiveUsingPS( PreparedStatement ps, Integer nextFreeIndex,
                                        Object primitive,
                                        Type primitiveType
                                      )
        throws SQLException
    {
        if( primitiveType instanceof ParameterizedType )
        {
            primitiveType = ( (ParameterizedType) primitiveType ).getRawType();
        }

        if( primitiveType instanceof Class<?>
            && Enum.class.isAssignableFrom( (Class<?>) primitiveType ) )
        {
            ps.setInt(
                nextFreeIndex,
                this._state.enumPKs().get().get(
                    QualifiedName.fromClass( (Class<?>) primitiveType, primitive.toString() ).toString() )
                     );
        }
        else
        {
            this._sqlTypeHelper.addPrimitiveToPS( ps, nextFreeIndex, primitive, primitiveType );
        }
    }

    private void storeVCClassIDUsingPS( PreparedStatement ps, Integer nextFreeIndex, Object vc )
        throws SQLException
    {
        if( vc == null )
        {
            ps.setNull( nextFreeIndex, Types.INTEGER );
        }
        else
        {
            ValueDescriptor vDesc = this._qi4SPI.valueDescriptorFor( vc );
            Integer classID = this._state.usedClassesPKs().get().get( vDesc );
            ps.setInt( nextFreeIndex, classID );
        }
    }

    private void updateEntityInfoAndProperties( Connection connection,
                                                Map<QualifiedName, PreparedStatement> qNameInsertPSs,
                                                PreparedStatement insertAllQNamesPS,
                                                PreparedStatement clearPropertiesPS,
                                                PreparedStatement ps,
                                                EntityState state,
                                                Long entityPK,
                                                Map<Long, Integer> qNamePKs
                                              )
        throws SQLException
    {

        this.clearAllEntitysQNames( clearPropertiesPS, entityPK );

        // Update state
        ps.setString( 1, state.entityReference().identity().toString() );
        ps.setTimestamp( 2, Timestamp.from( state.lastModified() ) );
        ps.setString( 3, state.version() );
        ps.setString( 4, this._app.version() );
        ps.setLong( 5, entityPK );
        ps.addBatch();

        Integer nextUsableQNamePK = this.insertPropertyQNames( connection, qNameInsertPSs, insertAllQNamesPS,
                                                               state, entityPK );
        qNamePKs.put( entityPK, nextUsableQNamePK );
    }

    private void insertProperties( Connection connection,
                                   Map<QualifiedName, PreparedStatement> qNameInsertPSs,
                                   PreparedStatement insertAllQNamesPS,
                                   EntityState state,
                                   Long entityPK,
                                   Map<Long, Integer> qNamePKs
                                 )
        throws SQLException
    {
        Integer nextQnamePK = this.insertPropertyQNames( connection,
                                                         qNameInsertPSs, insertAllQNamesPS,
                                                         state, entityPK );
        qNamePKs.put( entityPK, nextQnamePK );
    }

    private void removeEntity( EntityState state, PreparedStatement ps )
        throws SQLException
    {
        ps.setString( 1, state.entityReference().identity().toString() );
        ps.addBatch();
    }

    private void insertPropertyType( PreparedStatement insertPropertyTypePS, EntityState state, Long entityPK )
        throws SQLException
    {
        state.entityDescriptor().types().forEach( clazz -> {
            Integer typePK = this._state.entityTypePKs().get().get( clazz.getName() );
            if( typePK == null )
            {
                throw new SqlIndexingException( "Tried to get entity : " + clazz
                                                + ", but only aware of the following entities: "
                                                + this._state.entityTypePKs().get().keySet() );
            }
            try
            {
                insertPropertyTypePS.setLong( 1, entityPK );
                insertPropertyTypePS.setInt( 2, typePK );
                insertPropertyTypePS.addBatch();
            }
            catch( SQLException e )
            {
                throw new SqlIndexingException( "Underlying Exception when inserting " + entityPK,
                                                SQLUtil.withAllSQLExceptions( e ) );
            }
        } );
    }
}
