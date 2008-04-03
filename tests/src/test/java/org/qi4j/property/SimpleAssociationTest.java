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

package org.qi4j.property;

import java.io.Serializable;
import javax.swing.Icon;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.association.ManyAssociation;
import org.qi4j.entity.memory.MemoryEntityStoreComposite;
import org.qi4j.spi.entity.UuidIdentityGeneratorComposite;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class SimpleAssociationTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addServices( MemoryEntityStoreComposite.class, UuidIdentityGeneratorComposite.class );
        module.addComposites( SimpleAssociationTest.Person.class,
                              SimpleAssociationTest.Company.class );
        module.addAssociation().
            setAssociationInfo( DisplayInfo.class, new DisplayInfo( "Employees", "Employees in the company", "Employees", null ) ).
            withAccessor( SimpleAssociationTest.Employer.class ).employees();
    }

    @Test
    public void testAssociation()
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();

        SimpleAssociationTest.Company company;
        {
            CompositeBuilder<Company> builder = unitOfWork.newEntityBuilder( SimpleAssociationTest.Company.class );
            builder.propertiesOfComposite().name().set( "JayWay" );
            company = builder.newInstance();
        }

        company.name().set( "Jayway" );

        System.out.println( "Name is:" + company.name().get() );

        CompositeBuilder<SimpleAssociationTest.Person> builder = unitOfWork.newEntityBuilder( SimpleAssociationTest.Person.class );
        builder.propertiesOfComposite().name().set( "Rickard" );
        SimpleAssociationTest.Person rickard = builder.newInstance();

        company.employees().add( rickard );

        for( SimpleAssociationTest.Employer employer : rickard.employers() )
        {
            System.out.println( ( (SimpleAssociationTest.Nameable) employer ).name() );
        }

        unitOfWork.discard();
    }

    public interface Company
        extends SimpleAssociationTest.Nameable,
                SimpleAssociationTest.Employer,
                SimpleAssociationTest.StandardComposite,
                EntityComposite
    {
    }

    public interface Person
        extends SimpleAssociationTest.Nameable,
                SimpleAssociationTest.Employee,
                SimpleAssociationTest.StandardComposite,
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
        ManyAssociation<SimpleAssociationTest.Employer> employers();
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
