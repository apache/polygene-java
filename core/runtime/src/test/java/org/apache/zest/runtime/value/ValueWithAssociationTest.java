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
package org.apache.zest.runtime.value;

import org.apache.zest.api.identity.HasIdentity;
import org.apache.zest.api.identity.Identity;
import org.apache.zest.api.identity.StringIdentity;
import org.junit.Test;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.association.AssociationStateHolder;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.association.NamedAssociation;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.memory.MemoryEntityStoreService;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationService;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ValueWithAssociationTest extends AbstractZestTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( SimpleName.class );
        module.entities( DualFaced.class );
        module.values( SimpleName.class );
        module.values( DualFaced.class );
        module.services( MemoryEntityStoreService.class );
        module.services( OrgJsonValueSerializationService.class ).taggedWith( ValueSerialization.Formats.JSON );
    }

    @Test
    public void givenEntityInStoreWhenFetchEntityReferenceExpectSuccess()
        throws UnitOfWorkCompletionException
    {
        Identity identity1;
        Identity identity2;
        DualFaced value;
        try (UnitOfWork uow = unitOfWorkFactory.newUnitOfWork())
        {
            EntityBuilder<SimpleName> builder1 = uow.newEntityBuilder( SimpleName.class );
            builder1.instance().name().set( "Niclas" );
            SimpleName simpleEntity = builder1.newInstance();
            identity1 = simpleEntity.identity().get();

            EntityBuilder<DualFaced> builder2 = uow.newEntityBuilder( DualFaced.class );
            DualFaced proto = builder2.instance();
            proto.name().set( "Hedhman" );
            proto.simple().set( simpleEntity );
            proto.simples().add( simpleEntity );
            proto.namedSimples().put( "niclas", simpleEntity );
            DualFaced faced = builder2.newInstance();
            identity2 = faced.identity().get();
            value = uow.toValue( DualFaced.class, faced );
            assertThat( value.identity().get(), equalTo( identity2 ) );
            uow.complete();
        }

        try (UnitOfWork uow = unitOfWorkFactory.newUnitOfWork())
        {
            DualFaced entity = uow.get( DualFaced.class, identity2 );
            AssociationStateHolder holder = spi.stateOf( (EntityComposite) entity );
            Association<?> simple = holder.allAssociations().iterator().next();
            ManyAssociation<?> simples = holder.allManyAssociations().iterator().next();
            NamedAssociation<?> namedSimples = holder.allNamedAssociations().iterator().next();

            assertThat( spi.entityReferenceOf( simple ), equalTo( EntityReference.create( identity1 ) ) );
            assertThat( spi.entityReferencesOf( simples )
                            .iterator()
                            .next(), equalTo( EntityReference.create( identity1 ) ) );
            assertThat( spi.entityReferencesOf( namedSimples )
                            .iterator()
                            .next()
                            .getValue(), equalTo( EntityReference.create( identity1 ) ) );

            DualFaced resurrected = uow.toEntity( DualFaced.class, value );
            assertThat( resurrected.simple(), equalTo( entity.simple() ) );
            assertThat( resurrected.simples(), equalTo( entity.simples() ) );
            assertThat( resurrected.namedSimples(), equalTo( entity.namedSimples() ) );
        }
    }

    @Test
    public void givenNewValueWhenConvertingToEntityExpectNewEntityInStore()
        throws UnitOfWorkCompletionException
    {
        ValueBuilder<DualFaced> builder = valueBuilderFactory.newValueBuilder( DualFaced.class );
        builder.prototype().identity().set( new StringIdentity( "1234" ) );
        builder.prototype().name().set( "Hedhman" );
        DualFaced value = builder.newInstance();

        try (UnitOfWork uow = unitOfWorkFactory.newUnitOfWork())
        {
            uow.toEntity( DualFaced.class, value );
            uow.complete();
        }

        try (UnitOfWork uow = unitOfWorkFactory.newUnitOfWork())
        {
            DualFaced entity = uow.get( DualFaced.class, new StringIdentity( "1234" ) );
            assertThat( entity.identity().get(), equalTo( new StringIdentity( "1234" ) ) );
            assertThat( entity.name().get(), equalTo( "Hedhman" ) );
            uow.complete();
        }
    }

    @Test
    public void givenValueWithIdentityAlreadyInStoreWhenConvertingToEntityExpectExistingEntityToBeUpdated()
        throws UnitOfWorkCompletionException
    {
        Identity identity1;
        Identity identity2;
        try (UnitOfWork uow = unitOfWorkFactory.newUnitOfWork())
        {
            EntityBuilder<SimpleName> builder1 = uow.newEntityBuilder( SimpleName.class );
            builder1.instance().name().set( "Niclas" );
            SimpleName simpleEntity = builder1.newInstance();
            identity1 = simpleEntity.identity().get();

            EntityBuilder<DualFaced> builder2 = uow.newEntityBuilder( DualFaced.class );
            DualFaced proto = builder2.instance();
            proto.name().set( "Hedhman" );
            proto.simple().set( simpleEntity );
            proto.simples().add( simpleEntity );
            proto.namedSimples().put( "niclas", simpleEntity );
            DualFaced faced = builder2.newInstance();
            identity2 = faced.identity().get();
            uow.complete();
        }
        ValueBuilder<SimpleName> vb1 = valueBuilderFactory.newValueBuilder( SimpleName.class );
        vb1.prototype().identity().set( identity1 );
        vb1.prototype().name().set( "Paul" );
        SimpleName simpleValue = vb1.newInstance();

        ValueBuilder<DualFaced> vb2 = valueBuilderFactory.newValueBuilder( DualFaced.class );
        vb2.prototype().identity().set( identity2 );
        vb2.prototype().name().set( "Merlin" );
        vb2.prototype().simple().set( simpleValue );
        vb2.prototype().simples().add( simpleValue );
        vb2.prototype().namedSimples().put( "paul", simpleValue );
        DualFaced dualValue = vb2.newInstance();

        try (UnitOfWork uow = unitOfWorkFactory.newUnitOfWork())
        {
            DualFaced dualEntity = uow.toEntity( DualFaced.class, dualValue );
            // The root entity is expected to have changed value,
            assertThat( dualEntity.name().get(), equalTo( "Merlin" ) );
            // But the referenced entity is not updated, only using the EntityReference, which still points to "Niclas",
            // even though the value contains "Paul" for that entity. That entity needds to be updated separately
            assertThat( dualEntity.simple().get().name().get(), equalTo( "Niclas" ) );
            assertThat( dualEntity.simples().get(0).name().get(), equalTo( "Niclas" ) );
            assertThat( dualEntity.namedSimples().get("paul").name().get(), equalTo( "Niclas" ) );
            assertThat( dualEntity.namedSimples().get("niclas"), equalTo( null ) );
        }
    }

    public interface SimpleName extends HasIdentity
    {
        Property<String> name();
    }

    public interface DualFaced extends HasIdentity
    {
        Property<String> name();

        @Optional
        Association<SimpleName> simple();

        ManyAssociation<SimpleName> simples();

        NamedAssociation<SimpleName> namedSimples();
    }
}
