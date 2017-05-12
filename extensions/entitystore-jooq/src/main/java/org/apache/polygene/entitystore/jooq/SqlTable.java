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

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.property.PropertyDescriptor;
import org.apache.polygene.api.service.ServiceActivation;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.api.unitofwork.NoSuchEntityTypeException;
import org.apache.polygene.spi.entitystore.EntityNotFoundException;
import org.jooq.Condition;
import org.jooq.CreateTableAsStep;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.Schema;
import org.jooq.SelectJoinStep;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;

@Mixins( SqlTable.Mixin.class )
public interface SqlTable
{
    String IDENTITY_COLUMN_NAME = "identity";
    String VALUEID_COLUMN_NAME = "value_id";
    String VERSION_COLUMN_NAME = "version";
    String APPLICATIONVERSION_COLUMN_NAME = "app_version";
    String TYPE_COLUMN_NAME = "type";
    String LASTMODIFIED_COLUMN_NAME = "modified_at";
    String CREATED_COLUMN_NAME = "created_at";
    String TABLENAME_COLUMN_NAME = "table_name";
    String ASSOCIATIONS_COLUMN_NAME = "assocations";
    String POSITION_COLUMN_NAME = "position";

    String createNewTableName( Class<?> type );

    Result<Record> createNewTable( Class<?> mixinType, EntityDescriptor descriptor );

    boolean isProperty( Method method );

    Table<Record> findTable( Class<?> type, EntityDescriptor descriptor );

    Table<Record> createTable( Class<?> type, EntityDescriptor descriptor );

    String findTableName( Class<?> type, EntityDescriptor descriptor );

    List<Table<Record>> getTableJoins( EntityDescriptor entityDescriptor );

    Result<Record> fetchTypeInfoFromTable( Class<?> entityType );

    BaseEntity fetchBaseEntity( EntityReference reference, ModuleDescriptor module );

    EntityDescriptor findEntityDescriptor( String typeName, ModuleDescriptor module );

    Record createNewBaseEntity( EntityReference reference, EntityDescriptor descriptor );

    SelectQuery<Record> createGetEntityQuery( EntityDescriptor entityDescriptor, EntityReference reference );

    void fetchAssociations( Record record, Consumer<AssociationValue> consume );

    class Mixin
        implements SqlTable, ServiceActivation
    {

        @This
        JooqDslContext dsl;

        @This
        private Configuration<JooqEntityStoreConfiguration> configuration;

        @This
        private SqlType sqlType;

        @Uses
        private ServiceDescriptor serviceDescriptor;

        private Table<Record> typesTable;
        private Table<Record> entitiesTable;
        private Map<Class, Table<Record>> entityTables;
        private Field<String> identityColumn;
        private Field<String> valueIdentityColumn;
        private Field<String> typeColumn;
        private Field<String> versionColumn;
        private Field<String> applicationVersionColumn;
        private Field<Instant> modifiedColumn;
        private Field<Instant> createdColumn;
        private Field<String> tableNameColumn;
        private Field<String> assocationsColumn;
        private Field<String> positionColumn;

        private Schema schema;

        private SQLDialect dialect;

        @Override
        public void activateService()
            throws Exception
        {
            configuration.refresh();
            JooqEntityStoreConfiguration config = configuration.get();

            // Prepare jooq DSL
            dialect = serviceDescriptor.metaInfo( SQLDialect.class );

            String schemaName = config.schemaName().get();
            String typesTableName = config.typesTableName().get();
            String entitiesTableName = config.entitiesTableName().get();
            schema = DSL.schema( DSL.name( schemaName ) );
            typesTable = DSL.table(
                dialect.equals( SQLDialect.SQLITE )
                ? DSL.name( typesTableName )
                : DSL.name( schema.getName(), typesTableName )
                                  );
            entitiesTable = DSL.table(
                dialect.equals( SQLDialect.SQLITE )
                ? DSL.name( entitiesTableName )
                : DSL.name( schema.getName(), entitiesTableName ) );

            identityColumn = DSL.field( DSL.name( IDENTITY_COLUMN_NAME ), String.class );
            valueIdentityColumn = DSL.field( DSL.name( VALUEID_COLUMN_NAME ), String.class );
            versionColumn = DSL.field( DSL.name( VERSION_COLUMN_NAME ), String.class );
            applicationVersionColumn = DSL.field( DSL.name( APPLICATIONVERSION_COLUMN_NAME ), String.class );
            typeColumn = DSL.field( DSL.name( TYPE_COLUMN_NAME ), String.class );
            modifiedColumn = DSL.field( DSL.name( LASTMODIFIED_COLUMN_NAME ), Instant.class );
            createdColumn = DSL.field( DSL.name( CREATED_COLUMN_NAME ), Instant.class );
            tableNameColumn = DSL.field( DSL.name( TABLENAME_COLUMN_NAME ), String.class );
            assocationsColumn = DSL.field( DSL.name( ASSOCIATIONS_COLUMN_NAME ), String.class );
            positionColumn = DSL.field( DSL.name( POSITION_COLUMN_NAME ), String.class );

            // Eventually create schema
            if( config.createIfMissing().get() )
            {
                if( !dialect.equals( SQLDialect.SQLITE )
                    && dsl.meta().getSchemas().stream().noneMatch( s -> schema.getName().equalsIgnoreCase( s.getName() ) ) )
                {
                    dsl.createSchema( schema ).execute();
                }
            }
        }

        @Override
        public BaseEntity fetchBaseEntity( EntityReference reference, ModuleDescriptor module )
        {
            BaseEntity result = new BaseEntity();

            Result<Record> baseEntityResult = dsl
                .select()
                .from( entitiesTable )
                .where( identityColumn.eq( reference.toURI() ) )
                .fetch();

            if( baseEntityResult.isEmpty() )
            {
                throw new EntityNotFoundException( reference );
            }
            Record baseEntity = baseEntityResult.get( 0 );
            String typeName = baseEntity.field( typeColumn ).get( baseEntity );
            result.type = findEntityDescriptor( typeName, module );
            result.version = baseEntity.field( versionColumn ).get( baseEntity );
            result.applicationVersion = baseEntity.field( applicationVersionColumn ).get( baseEntity );
            result.identity = EntityReference.parseEntityReference( baseEntity.field( identityColumn ).get( baseEntity ) ).identity();
            result.currentValueIdentity = EntityReference.parseEntityReference( baseEntity.field( valueIdentityColumn ).get( baseEntity ) ).identity();
            result.modifedAt = baseEntity.field( modifiedColumn ).get( baseEntity );
            result.createdAt = baseEntity.field( createdColumn ).get( baseEntity );
            return result;
        }

        @Override
        public void passivateService()
            throws Exception
        {
            schema = null;
        }

        public Result<Record> fetchTypeInfoFromTable( Class<?> entityType )
        {
            return dsl
                .select()
                .from( typesTable )
                .where( identityColumn.eq( entityType.getName() ) )
                .fetch();
        }

        public List<Table<Record>> getTableJoins( EntityDescriptor entityDescriptor )
        {
            return entityDescriptor
                .mixinTypes()
                .map( ( Class<?> type ) -> findTable( type, entityDescriptor ) )
                .collect( Collectors.toList() );
        }

        public Table<Record> findTable( Class<?> type, EntityDescriptor descriptor )
        {
            return entityTables.computeIfAbsent( type, t -> createTable( t, descriptor ) );
        }

        public Table<Record> createTable( Class<?> type, EntityDescriptor descriptor )
        {
            String tableName = findTableName( type, descriptor );
            return null;
        }

        public String findTableName( Class<?> type, EntityDescriptor descriptor )
        {
            Result<Record> typeInfo = fetchTypeInfoFromTable( type );
            if( typeInfo.isEmpty() )
            {
                typeInfo = createNewTable( type, descriptor );
            }
            return typeInfo.getValue( 0, tableNameColumn );
        }

        @Override
        public String createNewTableName( Class<?> type )
        {
            return null;
        }

        public Result<Record> createNewTable( Class<?> mixinType, EntityDescriptor descriptor )
        {
            String tableName = createNewTableName( mixinType );
            CreateTableAsStep<Record> table = dsl.createTable( tableName );
            Arrays.stream( mixinType.getDeclaredMethods() )
                  .filter( this::isProperty )
                  .forEach( method ->
                            {
                                PropertyDescriptor propertyDescriptor = descriptor.state().findPropertyModelByName( method.getName() );
                                ValueType valueType = propertyDescriptor.valueType();
                                Class<?> propertyType = valueType.primaryType();
                                String propertyName = method.getName();
                                table.column( propertyName, sqlType.getSqlDataTypeFor( propertyType ) );
                            } );

            return fetchTypeInfoFromTable( mixinType );
        }

        public boolean isProperty( Method method )
        {
            return Property.class.isAssignableFrom( method.getReturnType() ) && method.getParameterCount() == 0;
        }

        public EntityDescriptor findEntityDescriptor( String typeName, ModuleDescriptor module )
        {
            try
            {
                Class<?> type = getClass().getClassLoader().loadClass( typeName );
                return module.typeLookup().lookupEntityModel( type );
            }
            catch( ClassNotFoundException e )
            {
                throw new NoSuchEntityTypeException( typeName, module.name(), module.typeLookup() );
            }
        }

        @Override
        public Record createNewBaseEntity( EntityReference reference, EntityDescriptor descriptor )
        {
            return null;
        }

        /**
         * Builds the SELECT statement for a given entity.
         * <p>
         * Example; If we have the following entity
         * </p>
         * <code><pre>
         *     public interface LegalEntity
         *     {
         *         Property&lt;String&gt; registration();
         *     }
         * <p>
         *     public interface Person extends LegalEntity
         *     {
         *         Property&lt;String&gt; name();
         * <p>
         *         &#64;Optional
         *         Association&lt;Person&gt; spouse();
         * <p>
         *         ManyAssocation&lt;Person&gt; children();
         *     }
         * </pre></code>
         * <p>
         * and we do a simple;
         * <code><pre>
         *     Person p = uow.get( Person.class, "niclas" );
         * </pre></code>
         * <p>
         * then the generated query will be
         * </p>
         * <code><pre>
         *     SELECT * FROM ENTITIES
         *     JOIN Person ON identity = ENTITIES.value_id
         *     JOIN LegalEntity ON identity = ENTITIES.value_id
         *     JOIN Person_Assoc ON identity = ENTITIES.value_id
         *     WHERE ENTITIES.identity = '123'
         * </pre></code>
         *
         * @param entityDescriptor The descriptor of the entity type to be built.
         * @return The SELECT query that can be executed to retrieve the entity.
         */
        public SelectQuery<Record> createGetEntityQuery( EntityDescriptor entityDescriptor, EntityReference reference )
        {
            SelectJoinStep<Record> from = dsl.select().from( entitiesTable );
            List<Table<Record>> joins = getTableJoins( entityDescriptor );
            for( Table<Record> joinedTable : joins )
            {
                Field<String> joinedField = joinedTable.field( identityColumn );
                Condition joinCondition = joinedField.eq( entitiesTable.field( valueIdentityColumn ) );
                from = from.join( joinedTable ).on( joinCondition );
            }
            return from.where( identityColumn.eq( reference.identity().toString() ) ).getQuery();
        }

        @Override
        public void fetchAssociations( Record record, Consumer<AssociationValue> consume )
        {
            AssociationValue value = new AssociationValue();
            value.name = record.getValue( assocationsColumn );
            value.position = record.getValue( positionColumn );
            value.reference = record.getValue( this.assocationsColumn );
            consume.accept( value );
        }
    }
}
