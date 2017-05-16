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
package org.apache.polygene.regression.qi377;

import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Test;
import org.apache.polygene.api.association.Association;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.EntityTestAssembler;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class InterfaceCollisionWithRelatedReturnTypesTest
    extends AbstractPolygeneTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( Employee.class, Company.class );
    }

    @Test
    public void shouldBeAbleToSetNameToTheCompany()
        throws UnitOfWorkCompletionException
    {
        Identity identity;
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            Company startUp = uow.newEntity( Company.class );
            startUp.name().set( "Acme" );
            identity = ((HasIdentity) startUp).identity().get();
            uow.complete();
        }
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            Company startUp = uow.get( Company.class, identity );
            assertThat( startUp.name().get(), equalTo( "Acme" ) );

            SalesTeam sales = uow.get( SalesTeam.class, identity );
            assertThat( sales.name().get(), equalTo( "Acme" ) );

            ResearchTeam research = uow.get( ResearchTeam.class, identity );
            assertThat( research.name().get(), equalTo( "Acme" ) );
        }
    }

    @Test
    public void shouldBeAbleToSetLeadToTheCompany()
        throws UnitOfWorkCompletionException
    {
        Identity identity;
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            Company startUp = uow.newEntity( Company.class );
            Employee niclas = uow.newEntity( Employee.class );

            startUp.lead().set( niclas );
            identity = ((HasIdentity) startUp).identity().get();

            uow.complete();
        }
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            Company startUp = uow.get( Company.class, identity );
            Employee niclas = startUp.lead().get();
            assertThat( niclas, notNullValue() );

            SalesTeam sales = uow.get( SalesTeam.class, identity );
            assertThat( sales.lead().get(), equalTo( niclas ) );

            ResearchTeam research = uow.get( ResearchTeam.class, identity );
            assertThat( research.lead().get(), equalTo( niclas ) );
        }
    }

    @Test
    public void shouldBeAbleToSetLeadToTheSalesTeam()
    {
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            SalesTeam startUp = uow.newEntity( SalesTeam.class );
            Employee niclas = uow.newEntity( Employee.class );

            startUp.lead().set( niclas );
        }
    }

    @Test
    public void shouldBeAbleToSetLeadToTheResearchTeam()
    {
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            ResearchTeam startUp = uow.newEntity( ResearchTeam.class );
            Employee niclas = uow.newEntity( Employee.class );

            startUp.lead().set( niclas );
        }
    }

    @Test
    public void shouldBeAbleToAddEmployeesToTheCompany()
    {
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            Company startUp = uow.newEntity( Company.class );
            Employee niclas = uow.newEntity( Employee.class );

            // To which team is Niclas added? Seems to be the interface listed first in the interface declaration?
            // This contrived example is probably just bad design...
            startUp.employees().add( niclas );
        }
    }

    @Test
    public void shouldBeAbleToAddEmployeesToTheSalesTeam()
    {
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            SalesTeam startUp = uow.newEntity( SalesTeam.class );
            Employee niclas = uow.newEntity( Employee.class );

            startUp.employees().add( niclas );
        }
    }

    @Test
    public void shouldBeAbleToAddEmployeesToTheResearchTeam()
    {
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            ResearchTeam startUp = uow.newEntity( ResearchTeam.class );
            Employee niclas = uow.newEntity( Employee.class );

            startUp.employees().add( niclas );
        }
    }

    public interface Employee
    {
    }

    public interface SalesTeam
    {
        @Optional
        Property<String> name();

        @Optional
        Association<Employee> lead();

        ManyAssociation<Employee> employees();
    }

    public interface ResearchTeam
    {
        @Optional
        Property<String> name();

        @Optional
        Association<Employee> lead();

        ManyAssociation<Employee> employees();
    }

    /**
     * This compiles, unlike the example in {@link InterfaceCollisionWithUnrelatedReturnTypesTest}.
     */
    public interface Company
        extends SalesTeam, ResearchTeam
    {
    }

}
