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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.common.QualifiedName;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.property.PropertyDescriptor;
import org.apache.polygene.spi.entity.ManyAssociationState;
import org.apache.polygene.spi.entity.NamedAssociationState;
import org.apache.polygene.spi.entitystore.helpers.DefaultEntityState;
import org.jooq.Field;
import org.jooq.InsertSetMoreStep;
import org.jooq.InsertSetStep;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DSL;

class MixinTable
    implements TableFields
{

    private final Table<Record> mixinTable;
    private final Table<Record> mixinAssocsTable;

    private final JooqDslContext dsl;
    private final Map<QualifiedName, Field<Object>> properties = new ConcurrentHashMap<>();
    private final Map<QualifiedName, Field<String>> associations = new ConcurrentHashMap<>();
    private final List<QualifiedName> manyAssociations = new CopyOnWriteArrayList<>();
    private final List<QualifiedName> namedAssociations = new CopyOnWriteArrayList<>();

    private TypesTable types;
    private final Class<?> mixinType;

    MixinTable( JooqDslContext dsl, Schema schema, TypesTable types, Class<?> mixinType,
                EntityDescriptor descriptor )
    {
        this.dsl = dsl;
        this.types = types;
        this.mixinType = mixinType;
        mixinTable = types.tableFor( mixinType, descriptor );
        mixinAssocsTable = getAssocsTable( descriptor, schema );

        descriptor.valueType().properties()
                  .filter( this::isThisMixin )
                  .forEach( propDescriptor ->
                            {
                                QualifiedName propertyName = propDescriptor.qualifiedName();
                                Field<Object> propertyField = types.fieldOf( propDescriptor );
                                properties.put( propertyName, propertyField );
                            }
                          );

        descriptor.valueType().associations()
                  .filter( this::isThisMixin )
                  .forEach( assocDescriptor ->
                            {
                                QualifiedName assocName = assocDescriptor.qualifiedName();
                                Field<String> assocField = types.fieldOf( assocDescriptor );
                                associations.put( assocName, assocField );
                            }
                          );

        descriptor.valueType().manyAssociations()
                  .filter( this::isThisMixin )
                  .forEach( assocDescriptor -> manyAssociations.add( assocDescriptor.qualifiedName() ) );

        descriptor.valueType().namedAssociations()
                  .filter( this::isThisMixin )
                  .forEach( assocDescriptor -> manyAssociations.add( assocDescriptor.qualifiedName() ) );
    }

    void insertMixinState( DefaultEntityState state, String valueIdentity )
    {
        InsertSetMoreStep<Record> primaryTable =
            dsl.insertInto( mixinTable )
               .set( identityColumn, valueIdentity )
               .set( createdColumn, new Timestamp( System.currentTimeMillis() ) );

        properties.forEach( ( propertyName, propertyField ) -> primaryTable.set( propertyField, state.propertyValueOf( propertyName ) ) );
        associations.forEach( ( assocName, assocField ) ->
                              {
                                  EntityReference reference = state.associationValueOf( assocName );
                                  String identity = null;
                                  if( reference != null )
                                  {
                                      identity = reference.identity().toString();
                                  }
                                  primaryTable.set( assocField, identity );
                              }
                            );
        int result = primaryTable.execute();

        if( mixinAssocsTable != null )
        {
            insertManyAndNamedAssociations( state, valueIdentity );
        }
    }

    private void insertManyAndNamedAssociations( DefaultEntityState state, String valueIdentity )
    {
        InsertSetStep<Record> assocsTable = dsl.insertInto( mixinAssocsTable );
        manyAssociations.forEach( assocName ->
                                  {
                                      ManyAssociationState entityReferences = state.manyAssociationValueOf( assocName );
                                      entityReferences.stream().forEach( setManyAssociation( assocName, valueIdentity, assocsTable ) );
                                  } );

        namedAssociations.forEach( assocName ->
                                   {
                                       NamedAssociationState entityReferences = state.namedAssociationValueOf( assocName );
                                       entityReferences.stream().forEach( setNamedAssociation( assocName, valueIdentity, assocsTable ) );
                                   } );

        InsertSetMoreStep<Record> assocs = assocsTable.set( Collections.emptyMap() );
        assocs.execute();
    }

    Table<Record> associationsTable()
    {
        return mixinAssocsTable;
    }

    /**
     * Writes one ManyAssoc Reference to the _ASSOCS table.
     * <ul>
     * <li>identityColumn - valueIdentity of primaryTable row</li>
     * <li>nameColumn - index in the of association in state holder</li>
     * <li>indexColumn - the position within the many association, starting at 0</li>
     * <li>referenceColumn - referenced entity's identity</li>
     * </ul>
     */
    private Consumer<? super EntityReference> setManyAssociation( QualifiedName assocName,
                                                                  String valueIdentity,
                                                                  InsertSetStep<Record> assocsTable )
    {
        return new Consumer<EntityReference>()
        {
            private int counter = 0;

            @Override
            public void accept( EntityReference ref )
            {
                InsertSetMoreStep<Record> set = assocsTable.set( identityColumn, valueIdentity )
                                                           .set( nameColumn, assocName.name() )
                                                           .set( indexColumn, "" + counter++ )
                                                           .set( referenceColumn, ref == null ? null : ref.identity().toString() );
            }
        };
    }

    /**
     * Writes one Named Reference to the _ASSOCS table.
     * <ul>
     * <li>identityColumn - valueIdentity of primaryTable row</li>
     * <li>nameColumn - name of association in state holder</li>
     * <li>indexColumn - the key/lookup name of the reference</li>
     * <li>referenceColumn - referenced entity's identity</li>
     * </ul>
     */
    private Consumer<? super Map.Entry<String, EntityReference>> setNamedAssociation( QualifiedName assocName,
                                                                                      String valueIdentity,
                                                                                      InsertSetStep<Record> assocsTable )
    {
        return ref ->
        {
            InsertSetMoreStep<Record> set = assocsTable.set( identityColumn, valueIdentity )
                                                       .set( nameColumn, assocName.name() )
                                                       .set( indexColumn, ref.getKey() )
                                                       .set( referenceColumn, ref.getValue().identity().toString() );
        };
    }

    private boolean isThisMixin( PropertyDescriptor descriptor )
    {
        Class<?> declaringClass = declaredIn( descriptor );
        return mixinType.equals( declaringClass );
    }

    private boolean isThisMixin( AssociationDescriptor descriptor )
    {
        Class<?> declaringClass = declaredIn( descriptor );
        return mixinType.equals( declaringClass );
    }

    private Class<?> declaredIn( PropertyDescriptor descriptor )
    {
        AccessibleObject accessor = descriptor.accessor();
        if( accessor instanceof Method )
        {
            return ( (Method) accessor ).getDeclaringClass();
        }
        throw new UnsupportedOperationException( "Property declared as " + accessor.getClass() + " is not supported in this Entity Store yet." );
    }

    private Class<?> declaredIn( AssociationDescriptor descriptor )
    {
        AccessibleObject accessor = descriptor.accessor();
        if( accessor instanceof Method )
        {
            return ( (Method) accessor ).getDeclaringClass();
        }
        throw new UnsupportedOperationException( "Property declared as " + accessor.getClass() + " is not supported in this Entity Store yet." );
    }

    void modifyMixinState( DefaultEntityState state, String valueId )
    {
        UpdateSetMoreStep<Record> primaryTable =
            dsl.update( mixinTable )
               .set( Collections.emptyMap() );  // empty map is a hack to get the right type returned from JOOQ.

        properties.forEach( ( propertyName, propertyField ) -> primaryTable.set( propertyField, state.propertyValueOf( propertyName ) ) );
        associations.forEach( ( assocName, assocField ) ->
                              {
                                  EntityReference reference = state.associationValueOf( assocName );
                                  primaryTable.set( assocField,
                                                    reference == null ? null : reference.identity().toString()
                                                  );
                              }
                            );
        int result = primaryTable.execute();

        if( mixinAssocsTable != null )
        {
            // Need to remove existing records.
            dsl.delete( mixinAssocsTable )
               .where( identityColumn.eq( valueId ) )
               .execute();
            insertManyAndNamedAssociations( state, valueId );
        }
    }

    private Table<Record> getAssocsTable( EntityDescriptor descriptor, Schema schema )
    {
        if( descriptor.state().manyAssociations().count() > 0
            || descriptor.state().namedAssociations().count() > 0 )
        {
            Name tableName = DSL.name( schema.getName(), mixinTable.getName() + ASSOCS_TABLE_POSTFIX );
            Table<Record> table = DSL.table( tableName );
            int result2 = dsl.createTableIfNotExists( table )
                             .column( identityColumn )
                             .column( nameColumn )
                             .column( indexColumn )
                             .column( referenceColumn )
                             .execute();
            return table;
        }
        else
        {
            return null;
        }
    }
}
