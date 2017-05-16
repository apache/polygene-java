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
package org.apache.polygene.runtime.association;

import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Test;
import org.apache.polygene.api.association.Association;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.association.NamedAssociation;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.EntityTestAssembler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Assert that Association, ManyAssociation and NamedAssociation equals/hashcode methods combine AssociationDescriptor and State.
 */
public class AssociationEqualityTest
    extends AbstractPolygeneTest
{

    //
    // --------------------------------------:: Types under test ::-----------------------------------------------------
    //
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( AnEntity.class );
        module.values( SomeWithAssociations.class, OtherWithAssociations.class );
    }

    public interface AnEntity
    {
    }

    public interface SomeWithAssociations
    {

        @Optional
        Association<AnEntity> anEntity();

        ManyAssociation<AnEntity> manyEntities();

        NamedAssociation<AnEntity> namedEntities();
    }

    public interface OtherWithAssociations
    {

        @Optional
        Association<AnEntity> anEntity();

        ManyAssociation<AnEntity> manyEntities();

        NamedAssociation<AnEntity> namedEntities();
    }

    //
    // ----------------------------:: AssociationDescriptor equality tests ::-------------------------------------------
    //
    @Test
    public void givenValuesOfTheSameTypeAndSameStateWhenTestingAssociationDescriptorEqualityExpectEquals()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            AnEntity anEntity = uow.newEntity( AnEntity.class );

            SomeWithAssociations some = buildSomeWithAssociation( anEntity );
            AssociationDescriptor someAssocDesc = polygene.api().associationDescriptorFor( some.anEntity() );
            AssociationDescriptor someManyAssocDesc = polygene.api().associationDescriptorFor( some.manyEntities() );
            AssociationDescriptor someNamedAssocDesc = polygene.api().associationDescriptorFor( some.namedEntities() );

            SomeWithAssociations some2 = buildSomeWithAssociation( anEntity );
            AssociationDescriptor some2AssocDesc = polygene.api().associationDescriptorFor( some2.anEntity() );
            AssociationDescriptor some2ManyAssocDesc = polygene.api().associationDescriptorFor( some2.manyEntities() );
            AssociationDescriptor some2NamedAssocDesc = polygene.api().associationDescriptorFor( some2.namedEntities() );

            assertThat( "AssociationDescriptor equal",
                        someAssocDesc,
                        equalTo( some2AssocDesc ) );
            assertThat( "AssociationDescriptor hashcode equal",
                        someAssocDesc.hashCode(),
                        equalTo( some2AssocDesc.hashCode() ) );
            assertThat( "ManyAssociationDescriptor equal",
                        someManyAssocDesc,
                        equalTo( some2ManyAssocDesc ) );
            assertThat( "ManyAssociationDescriptor hashcode equal",
                        someManyAssocDesc.hashCode(),
                        equalTo( some2ManyAssocDesc.hashCode() ) );
            assertThat( "NamedAssociationDescriptor equal",
                        someNamedAssocDesc,
                        equalTo( some2NamedAssocDesc ) );
            assertThat( "NamedAssociationDescriptor hashcode equal",
                        someNamedAssocDesc.hashCode(),
                        equalTo( some2NamedAssocDesc.hashCode() ) );
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenValuesOfTheSameTypeAndDifferentStateWhenTestingAssociationDescriptorEqualityExpectEquals()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            SomeWithAssociations some = buildSomeWithAssociation( uow.newEntity( AnEntity.class ) );
            AssociationDescriptor someAssocDesc = polygene.api().associationDescriptorFor( some.anEntity() );
            AssociationDescriptor someManyAssocDesc = polygene.api().associationDescriptorFor( some.manyEntities() );
            AssociationDescriptor someNamedAssocDesc = polygene.api().associationDescriptorFor( some.namedEntities() );

            SomeWithAssociations some2 = buildSomeWithAssociation( uow.newEntity( AnEntity.class ) );
            AssociationDescriptor some2AssocDesc = polygene.api().associationDescriptorFor( some2.anEntity() );
            AssociationDescriptor some2ManyAssocDesc = polygene.api().associationDescriptorFor( some2.manyEntities() );
            AssociationDescriptor some2NamedAssocDesc = polygene.api().associationDescriptorFor( some2.namedEntities() );

            assertThat( "AssociationDescriptor equal",
                        someAssocDesc,
                        equalTo( some2AssocDesc ) );
            assertThat( "AssociationDescriptor hashcode equal",
                        someAssocDesc.hashCode(),
                        equalTo( some2AssocDesc.hashCode() ) );
            assertThat( "ManyAssociationDescriptor equal",
                        someManyAssocDesc,
                        equalTo( some2ManyAssocDesc ) );
            assertThat( "ManyAssociationDescriptor hashcode equal",
                        someManyAssocDesc.hashCode(),
                        equalTo( some2ManyAssocDesc.hashCode() ) );
            assertThat( "NamedAssociationDescriptor equal",
                        someNamedAssocDesc,
                        equalTo( some2NamedAssocDesc ) );
            assertThat( "NamedAssociationDescriptor hashcode equal",
                        someNamedAssocDesc.hashCode(),
                        equalTo( some2NamedAssocDesc.hashCode() ) );
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenValuesOfDifferentTypeAndSameStateWhenTestingAssociationDescriptorEqualityExpectNotEquals()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            AnEntity anEntity = uow.newEntity( AnEntity.class );

            SomeWithAssociations some = buildSomeWithAssociation( anEntity );
            AssociationDescriptor someAssocDesc = polygene.api().associationDescriptorFor( some.anEntity() );
            AssociationDescriptor someManyAssocDesc = polygene.api().associationDescriptorFor( some.manyEntities() );
            AssociationDescriptor someNamedAssocDesc = polygene.api().associationDescriptorFor( some.namedEntities() );

            OtherWithAssociations other = buildOtherWithAssociation( anEntity );
            AssociationDescriptor otherAssocDesc = polygene.api().associationDescriptorFor( other.anEntity() );
            AssociationDescriptor otherManyAssocDesc = polygene.api().associationDescriptorFor( other.manyEntities() );
            AssociationDescriptor otherNamedAssocDesc = polygene.api().associationDescriptorFor( other.namedEntities() );

            assertThat( "AssociationDescriptor not equal",
                        someAssocDesc,
                        not( equalTo( otherAssocDesc ) ) );
            assertThat( "AssociationDescriptor hashcode not equal",
                        someAssocDesc.hashCode(),
                        not( equalTo( otherAssocDesc.hashCode() ) ) );
            assertThat( "ManyAssociationDescriptor not equal",
                        someManyAssocDesc,
                        not( equalTo( otherManyAssocDesc ) ) );
            assertThat( "ManyAssociationDescriptor hashcode not equal",
                        someManyAssocDesc.hashCode(),
                        not( equalTo( otherManyAssocDesc.hashCode() ) ) );
            assertThat( "NamedAssociationDescriptor not equal",
                        someNamedAssocDesc,
                        not( equalTo( otherNamedAssocDesc ) ) );
            assertThat( "NamedAssociationDescriptor hashcode not equal",
                        someNamedAssocDesc.hashCode(),
                        not( equalTo( otherNamedAssocDesc.hashCode() ) ) );
        }
        finally
        {
            uow.discard();
        }
    }

    //
    // --------------------------------:: Association State equality tests ::----------------------------------------------
    //
    @Test
    public void givenValuesOfSameTypeAndDifferentStateWhenTestingAssociationStateEqualityExpectNotEquals()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            SomeWithAssociations some = buildSomeWithAssociation( uow.newEntity( AnEntity.class ) );
            SomeWithAssociations some2 = buildSomeWithAssociation( uow.newEntity( AnEntity.class ) );

            assertThat( "Association State not equal",
                        some.anEntity().get(),
                        not( equalTo( some2.anEntity().get() ) ) );
            assertThat( "Association State hashcode not equal",
                        some.anEntity().get().hashCode(),
                        not( equalTo( some2.anEntity().get().hashCode() ) ) );
            assertThat( "ManyAssociation State not equal",
                        some.manyEntities().toList(),
                        not( equalTo( some2.manyEntities().toList() ) ) );
            assertThat( "ManyAssociation State hashcode not equal",
                        some.manyEntities().toList().hashCode(),
                        not( equalTo( some2.manyEntities().toList().hashCode() ) ) );
            assertThat( "NamedAssociation State not equal",
                        some.namedEntities().toMap(),
                        not( equalTo( some2.namedEntities().toMap() ) ) );
            assertThat( "NamedAssociation State hashcode not equal",
                        some.namedEntities().toMap().hashCode(),
                        not( equalTo( some2.namedEntities().toMap().hashCode() ) ) );
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenValuesOfDifferentTypesAndSameStateWhenTestingAssociationStateEqualityExpectEquals()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            AnEntity anEntity = uow.newEntity( AnEntity.class );

            SomeWithAssociations some = buildSomeWithAssociation( anEntity );
            OtherWithAssociations other = buildOtherWithAssociation( anEntity );

            assertThat( "Association State equal",
                        some.anEntity().get(),
                        equalTo( other.anEntity().get() ) );
            assertThat( "Association State hashcode equal",
                        some.anEntity().get().hashCode(),
                        equalTo( other.anEntity().get().hashCode() ) );
            assertThat( "ManyAssociation State equal",
                        some.manyEntities().toList(),
                        equalTo( other.manyEntities().toList() ) );
            assertThat( "ManyAssociation State hashcode equal",
                        some.manyEntities().toList().hashCode(),
                        equalTo( other.manyEntities().toList().hashCode() ) );
            assertThat( "NamedAssociation State equal",
                        some.namedEntities().toMap(),
                        equalTo( other.namedEntities().toMap() ) );
            assertThat( "NamedAssociation State hashcode equal",
                        some.namedEntities().toMap().hashCode(),
                        equalTo( other.namedEntities().toMap().hashCode() ) );
        }
        finally
        {
            uow.discard();
        }
    }

    //
    // ----------------------------------:: Association equality tests ::-----------------------------------------------
    //
    @Test
    public void givenValuesOfTheSameTypeAndSameStateWhenTestingAssociationEqualityExpectEquals()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            AnEntity anEntity = uow.newEntity( AnEntity.class );

            SomeWithAssociations some = buildSomeWithAssociation( anEntity );
            SomeWithAssociations some2 = buildSomeWithAssociation( anEntity );

            assertThat( "Association equal",
                        some.anEntity(),
                        equalTo( some2.anEntity() ) );
            assertThat( "Association hashcode equal",
                        some.anEntity().hashCode(),
                        equalTo( some2.anEntity().hashCode() ) );
            assertThat( "ManyAssociation equal",
                        some.manyEntities(),
                        equalTo( some2.manyEntities() ) );
            assertThat( "ManyAssociation hashcode equal",
                        some.manyEntities().hashCode(),
                        equalTo( some2.manyEntities().hashCode() ) );
            assertThat( "NamedAssociation equal",
                        some.namedEntities(),
                        equalTo( some2.namedEntities() ) );
            assertThat( "NamedAssociation hashcode equal",
                        some.namedEntities().hashCode(),
                        equalTo( some2.namedEntities().hashCode() ) );
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenValuesOfTheSameTypeAndDifferentStateWhenTestingAssociationEqualityExpectNotEquals()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            SomeWithAssociations some = buildSomeWithAssociation( uow.newEntity( AnEntity.class ) );
            SomeWithAssociations some2 = buildSomeWithAssociation( uow.newEntity( AnEntity.class ) );

            assertThat( "Association not equal",
                        some.anEntity(),
                        not( equalTo( some2.anEntity() ) ) );
            assertThat( "Association hashcode not equal",
                        some.anEntity().hashCode(),
                        not( equalTo( some2.anEntity().hashCode() ) ) );
            assertThat( "ManyAssociation not equal",
                        some.manyEntities(),
                        not( equalTo( some2.manyEntities() ) ) );
            assertThat( "ManyAssociation hashcode not equal",
                        some.manyEntities().hashCode(),
                        not( equalTo( some2.manyEntities().hashCode() ) ) );
            assertThat( "NamedAssociation not equal",
                        some.namedEntities(),
                        not( equalTo( some2.namedEntities() ) ) );
            assertThat( "NamedAssociation hashcode not equal",
                        some.namedEntities().hashCode(),
                        not( equalTo( some2.namedEntities().hashCode() ) ) );
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenValuesOfDifferentTypesAndSameStateWhenTestingAssociationEqualityExpectNotEquals()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            AnEntity anEntity = uow.newEntity( AnEntity.class );

            SomeWithAssociations some = buildSomeWithAssociation( anEntity );
            OtherWithAssociations other = buildOtherWithAssociation( anEntity );

            assertThat( "Association not equal",
                        some.anEntity(),
                        not( equalTo( other.anEntity() ) ) );
            assertThat( "Association hashcode not equal",
                        some.anEntity().hashCode(),
                        not( equalTo( other.anEntity().hashCode() ) ) );
            assertThat( "ManyAssociation not equal",
                        some.manyEntities(),
                        not( equalTo( other.manyEntities() ) ) );
            assertThat( "ManyAssociation hashcode not equal",
                        some.manyEntities().hashCode(),
                        not( equalTo( other.manyEntities().hashCode() ) ) );
            assertThat( "NamedAssociation not equal",
                        some.namedEntities(),
                        not( equalTo( other.namedEntities() ) ) );
            assertThat( "NamedAssociation hashcode not equal",
                        some.namedEntities().hashCode(),
                        not( equalTo( other.namedEntities().hashCode() ) ) );
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenValuesOfDifferentTypesAndDifferentStateWhenTestingAssociationEqualityExpectNotEquals()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            SomeWithAssociations some = buildSomeWithAssociation( uow.newEntity( AnEntity.class ) );
            OtherWithAssociations other = buildOtherWithAssociation( uow.newEntity( AnEntity.class ) );

            assertThat( "Association not equal",
                        some.anEntity(),
                        not( equalTo( other.anEntity() ) ) );
            assertThat( "Association hashcode not equal",
                        some.anEntity().hashCode(),
                        not( equalTo( other.anEntity().hashCode() ) ) );
            assertThat( "ManyAssociation not equal",
                        some.manyEntities(),
                        not( equalTo( other.manyEntities() ) ) );
            assertThat( "ManyAssociation hashcode not equal",
                        some.manyEntities().hashCode(),
                        not( equalTo( other.manyEntities().hashCode() ) ) );
            assertThat( "NamedAssociation not equal",
                        some.namedEntities(),
                        not( equalTo( other.namedEntities() ) ) );
            assertThat( "NamedAssociation hashcode not equal",
                        some.namedEntities().hashCode(),
                        not( equalTo( other.namedEntities().hashCode() ) ) );
        }
        finally
        {
            uow.discard();
        }
    }

    //
    // -----------------------------------:: Values factory methods ::--------------------------------------------------
    //
    private SomeWithAssociations buildSomeWithAssociation( AnEntity associated )
    {
        SomeWithAssociations some;
        {
            ValueBuilder<SomeWithAssociations> builder = valueBuilderFactory.newValueBuilder( SomeWithAssociations.class );
            builder.prototype().anEntity().set( associated );
            builder.prototype().manyEntities().add( associated );
            builder.prototype().namedEntities().put( "someKey", associated );
            some = builder.newInstance();
        }
        return some;
    }

    private OtherWithAssociations buildOtherWithAssociation( AnEntity associated )
    {
        OtherWithAssociations some;
        {
            ValueBuilder<OtherWithAssociations> builder = valueBuilderFactory.newValueBuilder( OtherWithAssociations.class );
            builder.prototype().anEntity().set( associated );
            builder.prototype().manyEntities().add( associated );
            builder.prototype().namedEntities().put( "someKey", associated );
            some = builder.newInstance();
        }
        return some;
    }
}
