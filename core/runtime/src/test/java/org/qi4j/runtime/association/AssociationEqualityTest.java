/*
 * Copyright (c) 2013, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.runtime.association;

import org.junit.Test;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Assert that Association and ManyAssociation equals/hashcode methods combine AssociationDescriptor and State.
 */
public class AssociationEqualityTest
    extends AbstractQi4jTest
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
    }

    public interface OtherWithAssociations
    {

        @Optional
        Association<AnEntity> anEntity();

        ManyAssociation<AnEntity> manyEntities();
    }

    //
    // ----------------------------:: AssociationDescriptor equality tests ::-------------------------------------------
    //
    @Test
    public void givenValuesOfTheSameTypeAndSameStateWhenTestingAssociationDescriptorEqualityExpectEquals()
    {
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            AnEntity anEntity = uow.newEntity( AnEntity.class );

            SomeWithAssociations some = buildSomeWithAssociation( anEntity );
            AssociationDescriptor someAssocDesc = qi4j.api().associationDescriptorFor( some.anEntity() );
            AssociationDescriptor someManyAssocDesc = qi4j.api().associationDescriptorFor( some.manyEntities() );

            SomeWithAssociations some2 = buildSomeWithAssociation( anEntity );
            AssociationDescriptor some2AssocDesc = qi4j.api().associationDescriptorFor( some2.anEntity() );
            AssociationDescriptor some2ManyAssocDesc = qi4j.api().associationDescriptorFor( some2.manyEntities() );

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
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenValuesOfTheSameTypeAndDifferentStateWhenTestingAssociationDescriptorEqualityExpectEquals()
    {
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            SomeWithAssociations some = buildSomeWithAssociation( uow.newEntity( AnEntity.class ) );
            AssociationDescriptor someAssocDesc = qi4j.api().associationDescriptorFor( some.anEntity() );
            AssociationDescriptor someManyAssocDesc = qi4j.api().associationDescriptorFor( some.manyEntities() );

            SomeWithAssociations some2 = buildSomeWithAssociation( uow.newEntity( AnEntity.class ) );
            AssociationDescriptor some2AssocDesc = qi4j.api().associationDescriptorFor( some2.anEntity() );
            AssociationDescriptor some2ManyAssocDesc = qi4j.api().associationDescriptorFor( some2.manyEntities() );

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
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenValuesOfDifferentTypeAndSameStateWhenTestingAssociationDescriptorEqualityExpectNotEquals()
    {
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            AnEntity anEntity = uow.newEntity( AnEntity.class );

            SomeWithAssociations some = buildSomeWithAssociation( anEntity );
            AssociationDescriptor someAssocDesc = qi4j.api().associationDescriptorFor( some.anEntity() );
            AssociationDescriptor someManyAssocDesc = qi4j.api().associationDescriptorFor( some.manyEntities() );

            OtherWithAssociations other = buildOtherWithAssociation( anEntity );
            AssociationDescriptor otherAssocDesc = qi4j.api().associationDescriptorFor( other.anEntity() );
            AssociationDescriptor some2ManyAssocDesc = qi4j.api().associationDescriptorFor( other.manyEntities() );

            assertThat( "AssociationDescriptor not equal",
                        someAssocDesc,
                        not( equalTo( otherAssocDesc ) ) );
            assertThat( "AssociationDescriptor hashcode not equal",
                        someAssocDesc.hashCode(),
                        not( equalTo( otherAssocDesc.hashCode() ) ) );
            assertThat( "ManyAssociationDescriptor not equal",
                        someManyAssocDesc,
                        not( equalTo( some2ManyAssocDesc ) ) );
            assertThat( "ManyAssociationDescriptor hashcode not equal",
                        someManyAssocDesc.hashCode(),
                        not( equalTo( some2ManyAssocDesc.hashCode() ) ) );
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
        UnitOfWork uow = module.newUnitOfWork();
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
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenValuesOfDifferentTypesAndSameStateWhenTestingAssociationStateEqualityExpectEquals()
    {
        UnitOfWork uow = module.newUnitOfWork();
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
        UnitOfWork uow = module.newUnitOfWork();
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
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenValuesOfTheSameTypeAndDifferentStateWhenTestingAssociationEqualityExpectNotEquals()
    {
        UnitOfWork uow = module.newUnitOfWork();
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
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenValuesOfDifferentTypesAndSameStateWhenTestingAssociationEqualityExpectNotEquals()
    {
        UnitOfWork uow = module.newUnitOfWork();
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
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenValuesOfDifferentTypesAndDifferentStateWhenTestingAssociationEqualityExpectNotEquals()
    {
        UnitOfWork uow = module.newUnitOfWork();
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
            ValueBuilder<SomeWithAssociations> builder = module.newValueBuilder( SomeWithAssociations.class );
            builder.prototype().anEntity().set( associated );
            builder.prototype().manyEntities().add( associated );
            some = builder.newInstance();
        }
        return some;
    }

    private OtherWithAssociations buildOtherWithAssociation( AnEntity associated )
    {
        OtherWithAssociations some;
        {
            ValueBuilder<OtherWithAssociations> builder = module.newValueBuilder( OtherWithAssociations.class );
            builder.prototype().anEntity().set( associated );
            builder.prototype().manyEntities().add( associated );
            some = builder.newInstance();
        }
        return some;
    }
}
