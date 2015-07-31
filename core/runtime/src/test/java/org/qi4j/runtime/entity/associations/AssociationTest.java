/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.entity.associations;

import java.io.Serializable;
import javax.swing.Icon;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

/**
 * Tests for associations
 */
public class AssociationTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( AssociationTest.Person.class,
                         AssociationTest.Company.class );
        module.forMixin( Employer.class )
            .setMetaInfo( new DisplayInfo( "Employees", "Employees in the company", "Employees", null ) )
            .declareDefaults()
            .employees();
        module.forMixin( Company.class ).declareDefaults().name().set( "A Company" );
    }

    @Test
    public void testAssociation()
    {
        UnitOfWork unitOfWork = module.newUnitOfWork();

        try
        {
            Company company = unitOfWork.newEntity( Company.class );
            Assert.assertEquals( "Company Name Default", "A Company", company.name().get() );

            {
                EntityBuilder<Company> builder = unitOfWork.newEntityBuilder( Company.class );
                final Company companyPrototype = builder.instance();
                companyPrototype.name().set( "JayWay" );
                company = builder.newInstance();
                Assert.assertEquals( "Company Name ", "JayWay", company.name().get() );
            }

            company.name().set( "Jayway" );
            Assert.assertEquals( "Company Name ", "Jayway", company.name().get() );

            System.out.println( "Name is:" + company.name().get() );

            EntityBuilder<Person> builder = unitOfWork.newEntityBuilder( Person.class );
            builder.instance().name().set( "Rickard" );
            Person rickard = builder.newInstance();

            builder = unitOfWork.newEntityBuilder( Person.class );
            builder.instance().name().set( "Niclas" );
            builder.instance().friend().set( rickard );
            Person niclas = builder.newInstance();

            niclas.members().add( rickard );

            company.employees().add( 0, rickard );

            for( Employer employer : rickard.employers() )
            {
                System.out.println( ( (Nameable) employer ).name() );
            }

            Assert.assertEquals( niclas.friend().get(), rickard );
            Assert.assertEquals( niclas.members().get( 0 ), rickard );
        }
        finally
        {
            unitOfWork.discard();
        }
    }

    public interface Friend<T>
    {
        @Optional
        Association<T> friend();
    }

    public interface Team<T>
    {
        ManyAssociation<T> members();
    }

    public interface Company
        extends AssociationTest.Nameable,
                AssociationTest.Employer,
                AssociationTest.StandardComposite,
                EntityComposite
    {
    }

    public interface Person
        extends AssociationTest.Nameable,
                AssociationTest.Employee,
                AssociationTest.Friend<Person>,
                AssociationTest.Team<Person>,
                AssociationTest.StandardComposite,
                EntityComposite
    {
    }

    public interface StandardComposite
    {
    }

    public interface Nameable
    {
        Property<String> name();
    }

    public interface Employer
    {
        ManyAssociation<Employee> employees();
    }

    public interface Employee
    {
        ManyAssociation<AssociationTest.Employer> employers();
    }

    public static class DisplayInfo
        implements Serializable
    {
        private String name;
        private String description;
        private String toolTip;
        private Icon icon;

        public DisplayInfo( String name, String description, String toolTip )
        {
            this.name = name;
            this.description = description;
            this.toolTip = toolTip;
        }

        public DisplayInfo( String name, String description, String toolTip, Icon icon )
        {
            this.name = name;
            this.description = description;
            this.toolTip = toolTip;
            this.icon = icon;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public String getToolTip()
        {
            return toolTip;
        }

        public Icon getIcon()
        {
            return icon;
        }
    }

    public static class RdfInfo
        implements Serializable
    {
        private String predicate;
        private String namespace;

        public RdfInfo( String predicate, String namespace )
        {
            this.predicate = predicate;
            this.namespace = namespace;
        }

        public String getPredicate()
        {
            return predicate;
        }

        public String getNamespace()
        {
            return namespace;
        }
    }
}
