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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.common.QualifiedName;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.StringIdentity;
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
import org.jooq.impl.DSL;

@SuppressWarnings( "WeakerAccess" )
public class EntitiesTable
    implements TableFields
{
    private static final Predicate<? super Class<?>> NOT_COMPOSITE = type -> !( type.equals( Composite.class ) || type.equals( EntityComposite.class ) );
    private static final Predicate<? super Class<?>> NOT_HASIDENTITY = type -> !( type.equals( HasIdentity.class ) );
    private Map<EntityCompositeType, Set<Class<?>>> mixinTypeCache = new ConcurrentHashMap<>();
    private Map<Class<?>, MixinTable> mixinTablesCache = new ConcurrentHashMap<>();

    private final Table<Record> entitiesTable;
    private JooqDslContext dsl;
    private final TypesTable types;
    private final Schema schema;
    private String applicationVersion;
    private boolean replacementStrategy = false;  // Figure out later if we should support both and if so, how.

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

        Result<Record> baseEntityResult = dsl
            .selectFrom( entitiesTable )
            .where( identityColumn.eq( reference.identity().toString() ) )
            .fetch();

        if( baseEntityResult.isEmpty() )
        {
            throw new EntityNotFoundException( reference );
        }
        Record row = baseEntityResult.get( 0 );
        return toBaseEntity( row, module );
    }

    protected BaseEntity toBaseEntity( Record row, ModuleDescriptor module )
    {
        BaseEntity result = new BaseEntity();
        String typeName = row.field( typeNameColumn ).get( row );
        result.type = findEntityDescriptor( typeName, module );
        result.version = row.field( versionColumn ).get( row );
        result.applicationVersion = row.field( applicationVersionColumn ).get( row );
        result.identity = new StringIdentity( row.field( identityColumn ).get( row ) );
        result.currentValueIdentity = EntityReference.parseEntityReference( row.field( valueIdentityColumn ).get( row ) ).identity();
        result.modifedAt = Instant.ofEpochMilli( row.field( modifiedColumn ).get( row ).getTime() );
        result.createdAt = Instant.ofEpochMilli( row.field( createdColumn ).get( row ).getTime() );
        return result;
    }

    public Stream<BaseEntity> fetchAll( EntityDescriptor type, ModuleDescriptor module )
    {
        Result<Record> baseEntityResult = dsl
            .selectFrom( entitiesTable )
            .fetch();
        return baseEntityResult.stream().map( record -> toBaseEntity( record, module ) );
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

    void insertEntity( DefaultEntityState state, BaseEntity baseEntity )
    {
        EntityCompositeType compositeType = state.entityDescriptor().valueType();
        Set<Class<?>> mixinTypes = mixinTypeCache.computeIfAbsent( compositeType, createMixinTypesSet( compositeType ) );
        mixinTypes.forEach( type ->
                            {
                                MixinTable table = findMixinTable( type, state.entityDescriptor() );
                                table.insertMixinState( state, baseEntity.currentValueIdentity.toString() );
                            } );
    }

    void modifyEntity( DefaultEntityState state, BaseEntity baseEntity, EntityStoreUnitOfWork uow )
    {
        updateBaseEntity( baseEntity, uow );
        if( replacementStrategy )
        {
            insertEntity( state, baseEntity );      // replacement strategy (more safe)
        }
        else
        {
            EntityCompositeType compositeType = state.entityDescriptor().valueType();
            Set<Class<?>> mixinTypes = mixinTypeCache.computeIfAbsent( compositeType, createMixinTypesSet( compositeType ) );
            mixinTypes.forEach( type ->
                                {
                                    MixinTable table = findMixinTable( type, state.entityDescriptor() );
                                    table.modifyMixinState( state, baseEntity.currentValueIdentity.toString() );
                                } );
        }
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
            .filter( NOT_HASIDENTITY )
            .filter( NOT_COMPOSITE )
            .collect( Collectors.toSet() );
    }

    private Function<EntityCompositeType, Set<Class<?>>> createMixinTypesSet( EntityCompositeType compositeType )
    {
        return type ->
        {
            Set<Class<?>> mixins = compositeType
                .properties()
                .map( PropertyDescriptor::accessor )
                .filter( Classes.instanceOf( Method.class ) )
                .map( accessor -> (Method) accessor )
                .map( Method::getDeclaringClass )
                .filter( NOT_HASIDENTITY )
                .filter( NOT_COMPOSITE )
                .collect( Collectors.toSet() );
            Set<Class<?>> mixinsWithAssociations = mixinsOf( compositeType.associations() );
            Set<Class<?>> mixinsWithManyAssociations = mixinsOf( compositeType.manyAssociations() );
            Set<Class<?>> mixinsWithNamedAssociations = mixinsOf( compositeType.namedAssociations() );
            mixins.addAll( mixinsWithAssociations );
            mixins.addAll( mixinsWithManyAssociations );
            mixins.addAll( mixinsWithNamedAssociations );
            return mixins;
        };
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

    private void updateBaseEntity( BaseEntity entity, EntityStoreUnitOfWork uow )
    {
        entity.version = increment( entity.version );
        if( replacementStrategy )
        {
            entity.currentValueIdentity = new StringIdentity( UUID.randomUUID().toString() );
        }
        dsl.update( entitiesTable )
           .set( modifiedColumn, new Timestamp( uow.currentTime().toEpochMilli() ) )
           .set( valueIdentityColumn, entity.currentValueIdentity.toString() )
           .set( versionColumn, entity.version )
           .set( applicationVersionColumn, applicationVersion )
           .execute();
    }

    private String increment( String version )
    {
        long ver = Long.parseLong( version );
        return Long.toString( ver + 1 );
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
        List<Table<Record>> joins = getMixinTables( entityDescriptor );
        SelectJoinStep<Record> from = dsl.select().from( entitiesTable );
        for( Table<Record> joinedTable : joins )
        {
            Condition joinCondition = valueIdentityColumn.eq( identityColumnOf( joinedTable ) );
            from = from.leftJoin( joinedTable ).on( joinCondition );
        }
        return from.where( identityColumnOf( entitiesTable ).eq( reference.identity().toString() ) ).getQuery();
    }

    public void fetchAssociations( BaseEntity entity, EntityDescriptor entityDescriptor, Consumer<AssociationValue> consume )
    {
        List<Table<Record>> joins = getAssocationsTables( entityDescriptor );
        SelectJoinStep<Record> from = dsl.select().from( entitiesTable );
        for( Table<Record> joinedTable : joins )
        {
            Condition joinCondition = valueIdentityColumn.eq( identityColumnOf( joinedTable ) );
            from = from.join( joinedTable ).on( joinCondition );
        }
        String reference = entity.identity.toString();
        SelectQuery<Record> query = from.where( identityColumnOf( entitiesTable ).eq( reference ) ).getQuery();
        Result<Record> result = query.fetch();
        result.forEach( record ->
                        {
                            AssociationValue value = new AssociationValue();
                            value.name = QualifiedName.fromClass( entityDescriptor.primaryType(), record.getValue( nameColumn ) );
                            value.position = record.getValue( indexColumn );
                            value.reference = record.getValue( referenceColumn );
                            consume.accept( value );
                        } );
    }

    private Field<String> identityColumnOf( Table<Record> joinedTable )
    {
        return DSL.field( DSL.name( joinedTable.getName(), identityColumn.getName() ), String.class );
    }

    public List<Table<Record>> getMixinTables( EntityDescriptor entityDescriptor )
    {
        return entityDescriptor
            .mixinTypes()
            .filter( NOT_COMPOSITE )
            .filter( NOT_HASIDENTITY )
            .map( ( Class<?> type ) -> types.tableFor( type, entityDescriptor ) )
            .collect( Collectors.toList() );
    }

    public List<Table<Record>> getAssocationsTables( EntityDescriptor entityDescriptor )
    {
        return entityDescriptor
            .mixinTypes()
            .filter( NOT_COMPOSITE )
            .filter( NOT_HASIDENTITY )
            .map( type -> findMixinTable( type, entityDescriptor ) )
            .map( MixinTable::associationsTable )
            .collect( Collectors.toList() );
    }

    public void removeEntity( EntityReference entityReference, EntityDescriptor descriptor )
    {
        ModuleDescriptor module = descriptor.module();
        BaseEntity baseEntity = fetchEntity( entityReference, module );
        if( replacementStrategy )
        {
            // TODO;  Mark deleted, I guess... not implemented
        }
        else
        {
            dsl.delete( entitiesTable )
               .where(
                   identityColumnOf( entitiesTable ).eq( entityReference.identity().toString() )
                     )
               .execute()
            ;
            String valueId = baseEntity.currentValueIdentity.toString();
            List<Table<Record>> mixinTables = getMixinTables( descriptor );
            List<Table<Record>> assocTables = getAssocationsTables( descriptor );
            mixinTables.forEach( table -> dsl.delete( table ).where( identityColumnOf( table ).eq( valueId ) ).execute() );
            assocTables.forEach( table -> dsl.delete( table ).where( identityColumnOf( table ).eq( valueId ) ).execute() );
        }
    }
}
