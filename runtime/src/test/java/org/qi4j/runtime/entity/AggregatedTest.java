/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.entity;

import org.junit.Test;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.*;

/**
 * JAVADOC
 */
public class AggregatedTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addEntities( CompanyEntity.class, EmployeeEntity.class, PersonEntity.class );

        module.addServices( MemoryEntityStoreService.class, UuidIdentityGeneratorService.class );

        module.addObjects( getClass() );
    }

    @Test
    public void givenAggregatedEntitiesWhenAggregateRootIsDeletedThenDeleteAggregatedEntities()
        throws Exception
    {
        CompanyEntity companyEntity;
        PersonEntity personEntity, personEntity2;
        EmployeeEntity employeeEntity, employeeEntity2;
        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            try
            {
                {
                    EntityBuilder<PersonEntity> builder = unitOfWork.newEntityBuilder( PersonEntity.class );
                    personEntity = builder.instance();
                    personEntity.name().set( "Rickard" );
                    personEntity = builder.newInstance();
                }

                {
                    EntityBuilder<PersonEntity> builder = unitOfWork.newEntityBuilder( PersonEntity.class );
                    personEntity2 = builder.instance();
                    personEntity2.name().set( "Niclas" );
                    builder.newInstance();
                }

                {
                    EntityBuilder<EmployeeEntity> builder = unitOfWork.newEntityBuilder( EmployeeEntity.class );
                    employeeEntity = builder.instance();
                    employeeEntity.person().set( personEntity );
                    employeeEntity.salary().set( 50000 );
                    employeeEntity.title().set( "Director" );
                    employeeEntity = builder.newInstance();
                }

                {
                    EntityBuilder<EmployeeEntity> builder = unitOfWork.newEntityBuilder( EmployeeEntity.class );
                    employeeEntity2 = builder.instance();
                    employeeEntity2.person().set( personEntity );
                    employeeEntity2.salary().set( 40000 );
                    employeeEntity2.title().set( "Developer" );
                    employeeEntity2 = builder.newInstance();
                }

                {
                    EntityBuilder<CompanyEntity> builder = unitOfWork.newEntityBuilder( CompanyEntity.class );
                    companyEntity = builder.instance();
                    companyEntity.director().set( employeeEntity );
                    companyEntity.employees().add( 0, employeeEntity );
                    companyEntity.employees().add( 0, employeeEntity2 );
                    companyEntity = builder.newInstance();
                }

                unitOfWork.complete();
            }
            catch( Exception e )
            {
                unitOfWork.discard();
                throw e;
            }
        }

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            try
            {
                companyEntity = unitOfWork.get( companyEntity );
                unitOfWork.remove( companyEntity );

                unitOfWork.complete();
            }
            catch( Exception e )
            {
                unitOfWork.discard();
                throw e;
            }
        }

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            try
            {
                unitOfWork.get( employeeEntity );

                fail( "Should not work" );

                unitOfWork.complete();
            }
            catch( NoSuchEntityException e )
            {
                unitOfWork.discard();
            }
        }

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            try
            {
                unitOfWork.get( employeeEntity2 );
                fail( "Should not work" );

                unitOfWork.complete();
            }
            catch( NoSuchEntityException e )
            {
                unitOfWork.discard();
            }
        }
    }

    public interface CompanyEntity
        extends EntityComposite
    {
        @Aggregated
        Association<EmployeeEntity> director();

        @Aggregated
        ManyAssociation<EmployeeEntity> employees();
    }

    public interface EmployeeEntity
        extends EntityComposite
    {
        Property<String> title();

        Property<Integer> salary();

        Association<PersonEntity> person();
    }

    public interface PersonEntity
        extends EntityComposite
    {
        Property<String> name();
    }
}
