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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.common.QualifiedName;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.property.PropertyDescriptor;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.type.EntityCompositeType;
import org.apache.polygene.api.unitofwork.NoSuchEntityTypeException;
import org.apache.polygene.api.util.Classes;
import org.apache.polygene.spi.entitystore.EntityNotFoundException;
import org.apache.polygene.spi.entitystore.EntityStoreUnitOfWork;
import org.apache.polygene.spi.entitystore.helpers.DefaultEntityState;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Schema;
import org.jooq.SelectJoinStep;
import org.jooq.SelectQuery;
import org.jooq.Table;

public class EntitiesTable
    implements TableFields
{
    private Map<EntityCompositeType, Set<Class<?>>> mixinTypeCache = new ConcurrentHashMap<>();
    private Map<Class<?>, MixinTable> mixinTablesCache = new ConcurrentHashMap<>();

    private final Table<Record> entitiesTable;
    private JooqDslContext dsl;
    private final TypesTable types;
    private final Schema schema;
    private String applicationVersion;

    EntitiesTable( JooqDslContext dsl, Schema schema, TypesTable types, String applicationVersion, String entitiesTableName )
    {
        this.dsl = dsl;
        this.types = types;
        this.schema = schema;
        this.applicationVersion = applicationVersion;
        entitiesTable = types.tableOf( entitiesTableName );
    }

    public BaseEntity fetchEntity( EntityReference reference, ModuleDescriptor module )
    {
        BaseEntity result = new BaseEntity();

        Result<Record> baseEntityResult = dsl
            .selectFrom( entitiesTable )
            .where( identityColumn.eq( reference.identity().toString() ) )
            .fetch();

        if( baseEntityResult.isEmpty() )
        {
            throw new EntityNotFoundException( reference );
        }
        Record baseEntity = baseEntityResult.get( 0 );
        String typeName = baseEntity.field( typeNameColumn ).get( baseEntity );
        result.type = findEntityDescriptor( typeName, module );
        result.version = baseEntity.field( versionColumn ).get( baseEntity );
        result.applicationVersion = baseEntity.field( applicationVersionColumn ).get( baseEntity );
        result.identity = EntityReference.parseEntityReference( baseEntity.field( identityColumn ).get( baseEntity ) ).identity();
        result.currentValueIdentity = EntityReference.parseEntityReference( baseEntity.field( valueIdentityColumn ).get( baseEntity ) ).identity();
        result.modifedAt = Instant.ofEpochMilli( baseEntity.field( modifiedColumn ).get( baseEntity ).getTime() );
        result.createdAt = Instant.ofEpochMilli( baseEntity.field( createdColumn ).get( baseEntity ).getTime() );
        return result;
    }

    private EntityDescriptor findEntityDescriptor( String typeName, ModuleDescriptor module )
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

    void insertEntity( DefaultEntityState state )
    {
        EntityCompositeType compositeType = state.entityDescriptor().valueType();
        Set<Class<?>> mixinTypes = mixinTypeCache.computeIfAbsent( compositeType, type ->
        {
            Set<Class<?>> mixins = compositeType
                .properties()
                .map( PropertyDescriptor::accessor )
                .filter( Classes.instanceOf( Method.class ) )
                .map( accessor -> (Method) accessor )
                .map( Method::getDeclaringClass )
                .collect( Collectors.toSet() );
            Set<Class<?>> mixinsWithAssociations = mixinsOf( compositeType.associations() );
            Set<Class<?>> mixinsWithManyAssociations = mixinsOf( compositeType.manyAssociations() );
            Set<Class<?>> mixinsWithNamedAssociations = mixinsOf( compositeType.namedAssociations() );
            mixins.addAll( mixinsWithAssociations );
            mixins.addAll( mixinsWithManyAssociations );
            mixins.addAll( mixinsWithNamedAssociations );
            return mixins;
        } );
        String valueIdentity = UUID.randomUUID().toString();
        mixinTypes.forEach( type ->
                            {
                                MixinTable table = findMixinTable( type, state.entityDescriptor() );
                                table.insertMixinState( state, valueIdentity );
                            } );
    }

    private MixinTable findMixinTable( Class<?> type, EntityDescriptor entityDescriptor )
    {
        return mixinTablesCache.computeIfAbsent( type, t -> new MixinTable( dsl, schema, types, type, entityDescriptor ) );
    }

    private Set<Class<?>> mixinsOf( Stream<? extends AssociationDescriptor> stream )
    {
        return stream
            .map( AssociationDescriptor::accessor )
            .filter( Classes.instanceOf( Method.class ) )
            .map( accessor -> (Method) accessor )
            .map( Method::getDeclaringClass )
            .collect( Collectors.toSet() );
    }

    private String columnNameOf( QualifiedName propertyName )
    {
        return null;
    }

    void modifyEntity( Class<?> mixinType, DefaultEntityState state )
    {

    }

    void createNewBaseEntity( EntityReference reference, EntityDescriptor descriptor, EntityStoreUnitOfWork uow )
    {
        String valueIdentity = UUID.randomUUID().toString();
        dsl.insertInto( entitiesTable )
           .set( identityColumn, reference.identity().toString() )
           .set( createdColumn, new Timestamp( uow.currentTime().toEpochMilli() ) )
           .set( modifiedColumn, new Timestamp( uow.currentTime().toEpochMilli() ) )
           .set( valueIdentityColumn, valueIdentity )
           .set( typeNameColumn, descriptor.primaryType().getName() )
           .set( versionColumn, "1" )
           .set( applicationVersionColumn, applicationVersion )
           .execute();
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

    public void fetchAssociations( Record record, Consumer<AssociationValue> consume )
    {
        AssociationValue value = new AssociationValue();
        value.name = record.getValue( nameColumn );
        value.position = record.getValue( indexColumn );
        value.reference = record.getValue( referenceColumn );
        consume.accept( value );
    }

    public List<Table<Record>> getTableJoins( EntityDescriptor entityDescriptor )
    {
        return entityDescriptor
            .mixinTypes()
            .map( ( Class<?> type ) -> types.tableFor( type, entityDescriptor ) )
            .collect( Collectors.toList() );
    }
}
