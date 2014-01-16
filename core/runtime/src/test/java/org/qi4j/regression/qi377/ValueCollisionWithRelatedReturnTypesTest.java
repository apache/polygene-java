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
package org.qi4j.regression.qi377;

import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

@Ignore( "This test exhibit QI-377" )
public class ValueCollisionWithRelatedReturnTypesTest
    extends AbstractQi4jTest
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
        Company startUp = module.newValue( Company.class );
        startUp.name().set( "Acme" );
    }

    @Test
    public void shouldBeAbleToSetLeadToTheCompany()
    {
        Company startUp = module.newValue( Company.class );
        Employee niclas = module.newValue( Employee.class );
        startUp.lead().set( niclas );
    }

    @Test
    public void shouldBeAbleToSetLeadToTheSalesTeam()
    {
        SalesTeam startUp = module.newValue( SalesTeam.class );
        Employee niclas = module.newValue( Employee.class );
        startUp.lead().set( niclas );
    }

    @Test
    public void shouldBeAbleToSetLeadToTheResearchTeam()
    {
        ResearchTeam startUp = module.newValue( ResearchTeam.class );
        Employee niclas = module.newValue( Employee.class );
        startUp.lead().set( niclas );
    }

    @Test
    public void shouldBeAbleToAddEmployeesToTheCompany()
    {
        Company startUp = module.newValue( Company.class );
        Employee niclas = module.newValue( Employee.class );
        startUp.employees().add( niclas );
    }

    @Test
    public void shouldBeAbleToAddEmployeesToTheSalesTeam()
    {
        SalesTeam startUp = module.newValue( SalesTeam.class );
        Employee niclas = module.newValue( Employee.class );
        startUp.employees().add( niclas );
    }

    @Test
    public void shouldBeAbleToAddEmployeesToTheResearchTeam()
    {
        ResearchTeam startUp = module.newValue( ResearchTeam.class );
        Employee niclas = module.newValue( Employee.class );
        startUp.employees().add( niclas );
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
