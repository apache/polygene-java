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

package org.apache.polygene.runtime.entity.associations;

import java.io.Serializable;
import javax.swing.Icon;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Assert;
import org.junit.Test;
import org.apache.polygene.api.association.Association;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.EntityTestAssembler;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for associations
 */
public class AssociationTest
    extends AbstractPolygeneTest
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
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();

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

            // Empty associations
            niclas.friend().set( null );
            niclas.members().clear();
            assertThat( niclas.friend().get(), nullValue() );
            assertThat( niclas.employers().count(), is( 0 ) );
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
