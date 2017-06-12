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

import org.apache.polygene.api.association.Association;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Test;

public class ValueCollisionWithRelatedReturnTypesTest
    extends AbstractPolygeneTest
{

    public static final Identity NICLAS = StringIdentity.fromString( "niclas" );

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
        builder.prototype().identity().set(NICLAS);
        Employee niclas = builder.newInstance();
        startUp.lead().set( niclas );
    }

    @Test
    public void shouldBeAbleToSetLeadToTheSalesTeam()
    {
        SalesTeam startUp = valueBuilderFactory.newValue( SalesTeam.class );
        ValueBuilder<Employee> builder = valueBuilderFactory.newValueBuilder( Employee.class );
        builder.prototype().identity().set(NICLAS);
        Employee niclas = builder.newInstance();
        startUp.lead().set( niclas );
    }

    @Test
    public void shouldBeAbleToSetLeadToTheResearchTeam()
    {
        ResearchTeam startUp = valueBuilderFactory.newValue( ResearchTeam.class );
        ValueBuilder<Employee> builder = valueBuilderFactory.newValueBuilder( Employee.class );
        builder.prototype().identity().set(NICLAS);
        Employee niclas = builder.newInstance();
        startUp.lead().set( niclas );
    }

    @Test
    public void shouldBeAbleToAddEmployeesToTheCompany()
    {
        Company startUp = valueBuilderFactory.newValue( Company.class );
        ValueBuilder<Employee> builder = valueBuilderFactory.newValueBuilder( Employee.class );
        builder.prototype().identity().set(NICLAS);
        Employee niclas = builder.newInstance();
        startUp.employees().add( niclas );
    }

    @Test
    public void shouldBeAbleToAddEmployeesToTheSalesTeam()
    {
        SalesTeam startUp = valueBuilderFactory.newValue( SalesTeam.class );
        ValueBuilder<Employee> builder = valueBuilderFactory.newValueBuilder( Employee.class );
        builder.prototype().identity().set(NICLAS);
        Employee niclas = builder.newInstance();
        startUp.employees().add( niclas );
    }

    @Test
    public void shouldBeAbleToAddEmployeesToTheResearchTeam()
    {
        ResearchTeam startUp = valueBuilderFactory.newValue( ResearchTeam.class );
        ValueBuilder<Employee> builder = valueBuilderFactory.newValueBuilder( Employee.class );
        builder.prototype().identity().set( NICLAS );
        Employee niclas = builder.newInstance();
        startUp.employees().add( niclas );
    }

    public interface Employee
        extends HasIdentity
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
