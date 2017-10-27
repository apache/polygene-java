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
 */
package org.apache.polygene.entitystore.sql;

import java.util.function.Consumer;
import java.util.stream.Stream;
import org.apache.polygene.api.composite.TransientBuilderFactory;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.serialization.Serialization;
import org.apache.polygene.api.service.ServiceActivation;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.spi.entitystore.EntityStoreUnitOfWork;
import org.apache.polygene.spi.entitystore.helpers.DefaultEntityState;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectQuery;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

/**
 * This class handles all the Jooq interactions.
 * <p>
 * <p>
 * <p>
 * <h1>Tables</h1>
 * <h2>Types Table</h2>
 * <ul>
 * <li>identity</li>
 * <li>table_name</li>
 * <li>created_at</li>
 * <li>modified_at</li>
 * </ul>
 * <h2>Entities Table</h2>
 * <ul>
 * <li>identity</li>
 * <li>app_version</li>
 * <li>value_id</li>
 * <li>version</li>
 * <li>type</li>
 * <li>modified_at</li>
 * <li>created_at</li>
 * </ul>
 * <h2>Mixin Tables</h2>
 * <p>
 * Each Mixin is stored in its own table. Only the following column is always present;
 * <ul>
 * <li>identity - this is not entity identity but the UUID of the value_id in the Entities Table above.</li>
 * </ul>
 * </p>
 * <p>
 * Each Property of the Mixin (as defined by QualifiedName of the Property, will reside in its own column.
 * All values in columns are (for now) serialized using a ValueSerialization service.
 * </p>
 * <p>
 * Associations also has their own columns in the table, with the EntityReference.identity() stored in them.
 * </p>
 * <p>
 * ManyAssociations and NamedAssociations are stored in a separate table, named &lt;mixintable&gt;_ASSOCS, see below.
 * </p>
 * <h2>Mixin_ASSOCS Table</h2>
 * <ul>
 * <li>identity - the value_id of the mixin value</li>
 * <li>name - the name of the ManyAssociation or NamedAssociation</li>
 * <li>position - for NamedAssociation this is the 'name' (i.e key) and for ManyAssociation this is the index into the list.</li>
 * <li>reference - EntityReference.identity of that association</li>
 * </ul>
 */
@Mixins( SqlTable.Mixin.class )
public interface SqlTable extends ServiceActivation
{
    BaseEntity fetchBaseEntity( EntityReference reference, ModuleDescriptor module );

    SelectQuery<Record> createGetEntityQuery( EntityDescriptor descriptor, EntityReference reference );

    void fetchAssociations( BaseEntity entity, EntityDescriptor descriptor, Consumer<AssociationValue> consume );

    BaseEntity createNewBaseEntity( EntityReference ref, EntityDescriptor descriptor, EntityStoreUnitOfWork unitOfWork );

    void insertEntity( DefaultEntityState state, BaseEntity baseEntity, EntityStoreUnitOfWork unitOfWork );

    void updateEntity( DefaultEntityState state, BaseEntity baseEntity, EntityStoreUnitOfWork unitOfWork );

    JooqDslContext jooqDslContext();

    void removeEntity( EntityReference entityReference, EntityDescriptor descriptor );

    Stream<BaseEntity> fetchAll( EntityDescriptor type, ModuleDescriptor module );

    class Mixin
        implements SqlTable, TableFields, ServiceActivation
    {
        @Structure
        private Application application;

        @Structure
        private TransientBuilderFactory tbf;

        @This
        private Configuration<SqlEntityStoreConfiguration> configuration;

        @Service
        private Serialization serialization;

        @Uses
        private ServiceDescriptor serviceDescriptor;

        private EntitiesTable entitiesTable;

        private TypesTable types;
        private JooqDslContext dsl;

        @Override
        public BaseEntity fetchBaseEntity( EntityReference reference, ModuleDescriptor module )
        {
            return entitiesTable.fetchEntity( reference, module );
        }

        @Override
        public Stream<BaseEntity> fetchAll( EntityDescriptor type, ModuleDescriptor module )
        {
            return entitiesTable.fetchAll( type, module );
        }

        @Override
        public SelectQuery<Record> createGetEntityQuery( EntityDescriptor descriptor, EntityReference reference )
        {
            return entitiesTable.createGetEntityQuery( descriptor, reference );
        }

        @Override
        public void fetchAssociations( BaseEntity entity, EntityDescriptor descriptor, Consumer<AssociationValue> consume )
        {
            entitiesTable.fetchAssociations( entity, descriptor, consume );
        }

        @Override
        public BaseEntity createNewBaseEntity( EntityReference ref, EntityDescriptor descriptor, EntityStoreUnitOfWork unitOfWork )
        {
            return entitiesTable.createNewBaseEntity( ref, descriptor, unitOfWork );
        }

        @Override
        public void insertEntity( DefaultEntityState state, BaseEntity baseEntity, EntityStoreUnitOfWork unitOfWork )
        {
            entitiesTable.insertEntity( state, baseEntity );
        }

        @Override
        public void updateEntity( DefaultEntityState state, BaseEntity baseEntity, EntityStoreUnitOfWork unitOfWork )
        {
            entitiesTable.modifyEntity( state, baseEntity, unitOfWork );
        }

        @Override
        public JooqDslContext jooqDslContext()
        {
            return dsl;
        }

        @Override
        public void removeEntity( EntityReference reference, EntityDescriptor descriptor )
        {
            entitiesTable.removeEntity( reference, descriptor );
        }

        @Override
        public void activateService()
            throws Exception
        {
            SqlEntityStoreConfiguration config = this.configuration.get();
            SQLDialect dialect = getSqlDialect( config );
            Settings settings = serviceDescriptor
                .metaInfo( Settings.class );

            String typesTableName = config.typesTableName().get();
            String entitiesTableName = config.entitiesTableName().get();

            dsl = tbf.newTransient( JooqDslContext.class, settings, dialect );

            types = new TypesTable( dsl, dialect, typesTableName );
            entitiesTable = new EntitiesTable( dsl, types, application.version(), entitiesTableName, serialization );

            if( config.createIfMissing().get() )
            {
                dsl.transaction( t -> {

                    dsl.createTableIfNotExists( dsl.tableNameOf( typesTableName ) )
                       .column( identityColumn )
                       .column( tableNameColumn )
                       .column( createdColumn )
                       .column( modifiedColumn )
                       .constraint( DSL.primaryKey( identityColumn ) )
                       .execute();

                    dsl.createTableIfNotExists( dsl.tableNameOf( entitiesTableName ) )
                       .column( identityColumn )
                       .column( applicationVersionColumn )
                       .column( valueIdentityColumn )
                       .column( versionColumn )
                       .column( typeNameColumn )
                       .column( modifiedColumn )
                       .column( createdColumn )
                       .constraint( DSL.primaryKey( identityColumn ) )
                       .execute();
                } );
            }
        }

        @Override
        public void passivateService()
            throws Exception
        {

        }

        private SQLDialect getSqlDialect( SqlEntityStoreConfiguration config )
        {
            SQLDialect dialect;
            String dialectString = config.dialect().get();
            if( dialectString.length() == 0 )
            {
                dialect = SQLDialect.DEFAULT;
            }
            else
            {
                try
                {
                    dialect = SQLDialect.valueOf( dialectString );
                }
                catch( IllegalArgumentException e )
                {
                    throw new IllegalArgumentException( "Invalid SQLDialect: '" + dialectString + "'" );
                }
            }
            return dialect;
        }
    }
}
