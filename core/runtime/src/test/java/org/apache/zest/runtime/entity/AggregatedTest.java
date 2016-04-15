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

package org.apache.zest.runtime.entity;

import org.junit.Test;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.entity.Aggregated;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.unitofwork.NoSuchEntityException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

import static org.junit.Assert.fail;

/**
 * JAVADOC
 */
public class AggregatedTest
    extends AbstractZestTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( CompanyEntity.class, EmployeeEntity.class, PersonEntity.class );

        new EntityTestAssembler().assemble( module );

        module.objects( getClass() );
    }

    @Test
    public void givenAggregatedEntitiesWhenAggregateRootIsDeletedThenDeleteAggregatedEntities()
        throws Exception
    {
        CompanyEntity companyEntity;
        PersonEntity personEntity, personEntity2;
        EmployeeEntity employeeEntity, employeeEntity2;
        try( UnitOfWork unitOfWork = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( "Creation" ) ) )
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

        try( UnitOfWork unitOfWork = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( "Removal" ) ) )
        {
            companyEntity = unitOfWork.get( companyEntity );
            unitOfWork.remove( companyEntity );

            unitOfWork.complete();
        }

        try( UnitOfWork unitOfWork = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( "No 1st employee" ) ) )
        {
            unitOfWork.get( employeeEntity );
            fail( "Should not work" );
        }
        catch( NoSuchEntityException e )
        {
            // Expected
        }

        try( UnitOfWork unitOfWork = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( "No 2nd employee" ) ) )
        {
            unitOfWork.get( employeeEntity2 );
            fail( "Should not work" );
        }
        catch( NoSuchEntityException e )
        {
            // Expected
        }

        try( UnitOfWork unitOfWork = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( "Persons not removed" ) ) )
        {
            unitOfWork.get( personEntity );
            unitOfWork.get( personEntity2 );
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
