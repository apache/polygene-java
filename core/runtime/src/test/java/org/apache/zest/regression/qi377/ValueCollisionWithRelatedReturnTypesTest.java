/*
 * Copyright (c) 2013, Chris Chapman. All Rights Reserved.
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.apache.zest.regression.qi377;

import org.junit.Test;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;

public class ValueCollisionWithRelatedReturnTypesTest
    extends AbstractZestTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values( Employee.class, Company.class );
    }

    @Test
    public void shouldBeAbleToSetNameToTheCompany()
    {
        ValueBuilder<Company> builder = valueBuilderFactory.newValueBuilder( Company.class );
        builder.prototype().name().set( "Acme" );
        Company startUp = builder.newInstance();
    }

    @Test
    public void shouldBeAbleToSetLeadToTheCompany()
    {
        Company startUp = valueBuilderFactory.newValue( Company.class );
        ValueBuilder<Employee> builder = valueBuilderFactory.newValueBuilder( Employee.class );
        builder.prototype().identity().set( "niclas" );
        Employee niclas = builder.newInstance();
        startUp.lead().set( niclas );
    }

    @Test
    public void shouldBeAbleToSetLeadToTheSalesTeam()
    {
        SalesTeam startUp = valueBuilderFactory.newValue( SalesTeam.class );
        ValueBuilder<Employee> builder = valueBuilderFactory.newValueBuilder( Employee.class );
        builder.prototype().identity().set( "niclas" );
        Employee niclas = builder.newInstance();
        startUp.lead().set( niclas );
    }

    @Test
    public void shouldBeAbleToSetLeadToTheResearchTeam()
    {
        ResearchTeam startUp = valueBuilderFactory.newValue( ResearchTeam.class );
        ValueBuilder<Employee> builder = valueBuilderFactory.newValueBuilder( Employee.class );
        builder.prototype().identity().set( "niclas" );
        Employee niclas = builder.newInstance();
        startUp.lead().set( niclas );
    }

    @Test
    public void shouldBeAbleToAddEmployeesToTheCompany()
    {
        Company startUp = valueBuilderFactory.newValue( Company.class );
        ValueBuilder<Employee> builder = valueBuilderFactory.newValueBuilder( Employee.class );
        builder.prototype().identity().set( "niclas" );
        Employee niclas = builder.newInstance();
        startUp.employees().add( niclas );
    }

    @Test
    public void shouldBeAbleToAddEmployeesToTheSalesTeam()
    {
        SalesTeam startUp = valueBuilderFactory.newValue( SalesTeam.class );
        ValueBuilder<Employee> builder = valueBuilderFactory.newValueBuilder( Employee.class );
        builder.prototype().identity().set( "niclas" );
        Employee niclas = builder.newInstance();
        startUp.employees().add( niclas );
    }

    @Test
    public void shouldBeAbleToAddEmployeesToTheResearchTeam()
    {
        ResearchTeam startUp = valueBuilderFactory.newValue( ResearchTeam.class );
        ValueBuilder<Employee> builder = valueBuilderFactory.newValueBuilder( Employee.class );
        builder.prototype().identity().set( "niclas" );
        Employee niclas = builder.newInstance();
        startUp.employees().add( niclas );
    }

    public interface Employee
        extends Identity
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
