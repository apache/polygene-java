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
package org.apache.polygene.entitystore.jooq;

import java.util.function.Consumer;
import javax.sql.DataSource;
import org.apache.polygene.api.PolygeneAPI;
import org.apache.polygene.api.composite.TransientBuilderFactory;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.object.ObjectFactory;
import org.apache.polygene.api.service.ServiceActivation;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.spi.entitystore.EntityStoreUnitOfWork;
import org.apache.polygene.spi.entitystore.helpers.DefaultEntityState;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Schema;
import org.jooq.SelectQuery;
import org.jooq.conf.RenderNameStyle;
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

    void fetchAssociations( Record record, Consumer<AssociationValue> consume );

    void createNewBaseEntity( EntityReference ref, EntityDescriptor descriptor, EntityStoreUnitOfWork unitOfWork );

    void insertEntity( DefaultEntityState state );

    JooqDslContext jooqDslContext();

    class Mixin
        implements SqlTable, TableFields, ServiceActivation
    {
        @Structure
        private Application application;

        @Structure
        private PolygeneAPI api;

        @Structure
        private TransientBuilderFactory tbf;

        @Structure
        private ObjectFactory objectFactory;

        @This
        private Configuration<JooqEntityStoreConfiguration> configuration;

        @Service
        private DataSource datasource;

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
        public SelectQuery<Record> createGetEntityQuery( EntityDescriptor descriptor, EntityReference reference )
        {
            return entitiesTable.createGetEntityQuery( descriptor, reference );
        }

        @Override
        public void fetchAssociations( Record record, Consumer<AssociationValue> consume )
        {
            entitiesTable.fetchAssociations( record, consume );
        }

        @Override
        public void createNewBaseEntity( EntityReference ref, EntityDescriptor descriptor, EntityStoreUnitOfWork unitOfWork )
        {
            entitiesTable.createNewBaseEntity( ref, descriptor, unitOfWork );
        }

        @Override
        public void insertEntity( DefaultEntityState state )
        {
            entitiesTable.insertEntity( state );
        }

        @Override
        public JooqDslContext jooqDslContext()
        {
            return dsl;
        }

        @Override
        public void activateService()
            throws Exception
        {
            JooqEntityStoreConfiguration config = this.configuration.get();
            SQLDialect dialect = getSqlDialect( config );

            Settings settings = serviceDescriptor
                .metaInfo( Settings.class )
                .withRenderNameStyle( RenderNameStyle.QUOTED );
            dsl = tbf.newTransient( JooqDslContext.class, settings, dialect );

            String schemaName = config.schemaName().get();
            String typesTableName = config.typesTableName().get();
            String entitiesTableName = config.entitiesTableName().get();
            Schema schema = DSL.schema( DSL.name( schemaName ) );
            types = new TypesTable( dsl, schema, dialect, typesTableName );
            entitiesTable = new EntitiesTable( dsl, schema, types, application.version(), entitiesTableName );

            // Eventually create schema
            if( config.createIfMissing().get() )
            {
                if( !dialect.equals( SQLDialect.SQLITE )
                    && dsl.meta().getSchemas().stream().noneMatch( s -> schema.getName().equalsIgnoreCase( s.getName() ) ) )
                {
                    dsl.createSchema( schema ).execute();
                }

                dsl.createTableIfNotExists( DSL.name( schemaName, typesTableName ) )
                   .column( identityColumn )
                   .column( tableNameColumn )
                   .column( createdColumn )
                   .column( modifiedColumn )
                   .execute();

                dsl.createTableIfNotExists( DSL.name( schemaName, entitiesTableName ) )
                   .column( identityColumn )
                   .column( applicationVersionColumn )
                   .column( valueIdentityColumn )
                   .column( versionColumn )
                   .column( typeNameColumn )
                   .column( modifiedColumn )
                   .column( createdColumn )
                   .execute();
            }
            datasource.getConnection().commit();
        }

        @Override
        public void passivateService()
            throws Exception
        {

        }

        private SQLDialect getSqlDialect( JooqEntityStoreConfiguration config )
        {
            SQLDialect dialect = null;
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
