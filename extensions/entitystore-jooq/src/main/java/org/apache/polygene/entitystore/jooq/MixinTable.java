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
import java.util.Map;
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
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.impl.DSL;

class MixinTable
    implements TableFields
{

    private final Table<Record> mixinTable;
    private final Table<Record> mixinAssocsTable;

    private final JooqDslContext dsl;
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
    }

    void insertMixinState( DefaultEntityState state, String valueIdentity )
    {
        InsertSetMoreStep<Record> primaryTable =
            dsl.insertInto( mixinTable )
               .set( identityColumn, valueIdentity )
               .set( createdColumn, new Timestamp( System.currentTimeMillis() ) )
               .set( modifiedColumn, new Timestamp( System.currentTimeMillis() ) );

        EntityDescriptor entityDescriptor = state.entityDescriptor();
        entityDescriptor.valueType().properties()
                        .filter( this::isThisMixin )
                        .forEach( propDescriptor ->
                                  {
                                      QualifiedName propertyName = propDescriptor.qualifiedName();
                                      Field<Object> propertyField = types.fieldOf( propDescriptor );
                                      primaryTable.set( propertyField, state.propertyValueOf( propertyName ) );
                                  }
                                );
        entityDescriptor.valueType().associations()
                        .filter( this::isThisMixin )
                        .forEach( assocDescriptor ->
                                  {
                                      QualifiedName assocName = assocDescriptor.qualifiedName();
                                      Field<String> assocField = types.fieldOf( assocDescriptor );
                                      EntityReference reference = state.associationValueOf( assocName );
                                      String identity = null;
                                      if( reference != null )
                                      {
                                          identity = reference.identity().toString();
                                      }
                                      primaryTable.set( assocField, identity );
                                  }
                                );

        if( mixinAssocsTable != null )
        {
            entityDescriptor.valueType().manyAssociations()
                            .filter( this::isThisMixin )
                            .forEach( setManyAssociations( state, valueIdentity ) );

            entityDescriptor.valueType().namedAssociations()
                            .filter( this::isThisMixin )
                            .forEach( setNamedAssociations( state, valueIdentity ) );
        }
    }

    private Consumer<? super AssociationDescriptor> setManyAssociations( DefaultEntityState state, String valueIdentity )
    {
        return assocDescriptor ->
        {
            QualifiedName assocName = assocDescriptor.qualifiedName();
            ManyAssociationState entityReferences = state.manyAssociationValueOf( assocName );
            entityReferences.stream().forEach( setManyAssociation( state, assocDescriptor, valueIdentity ) );
        };
    }

    private Consumer<? super EntityReference> setManyAssociation( DefaultEntityState state,
                                                                  AssociationDescriptor assocDescriptor,
                                                                  String valueIdentity )
    {
        QualifiedName assocName = assocDescriptor.qualifiedName();
        Field<String> assocField = types.fieldOf( assocDescriptor );
        InsertSetMoreStep<Record> assocsTable = dsl.insertInto( mixinAssocsTable ).set( identityColumn, valueIdentity );
        return ref ->
        {
            assocsTable.newRecord();
            assocsTable.set( assocField, state.associationValueOf( assocName ).identity().toString() );
        };
    }

    private Consumer<? super AssociationDescriptor> setNamedAssociations( DefaultEntityState state,
                                                                          String valueIdentity )
    {
        return assocDescriptor ->
        {
            QualifiedName assocName = assocDescriptor.qualifiedName();
            NamedAssociationState entityReferences = state.namedAssociationValueOf( assocName );
            entityReferences.stream().forEach( setNamedAssociation( state, assocDescriptor, valueIdentity ) );
        };
    }

    private Consumer<? super Map.Entry<String, EntityReference>> setNamedAssociation( DefaultEntityState state,
                                                                                      AssociationDescriptor assocDescriptor,
                                                                                      String valueIdentity )
    {
        QualifiedName assocName = assocDescriptor.qualifiedName();
        Field<String> assocField = types.fieldOf( assocDescriptor );
        InsertSetMoreStep<Record> assocsTable = dsl.insertInto( mixinAssocsTable ).set( identityColumn, valueIdentity );
        return ref ->
        {
            assocsTable.newRecord();
            assocsTable.set( assocField, state.associationValueOf( assocName ).identity().toString() );
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

    void modifyMixinState( Class<?> mixinType, DefaultEntityState state )
    {

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
