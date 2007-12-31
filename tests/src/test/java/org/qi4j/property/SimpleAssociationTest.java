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

import javax.swing.Icon;
import org.qi4j.bootstrap.AssemblyHelper;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.Mixins;
import org.qi4j.association.ManyAssociation;
import org.qi4j.library.framework.entity.AssociationMixin;
import org.qi4j.library.framework.entity.PropertyMixin;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class SimpleAssociationTest extends AbstractQi4jTest
{
    public void configure( ModuleAssembly module )
    {
        module.addComposites( SimpleAssociationTest.Person.class );
        module.addComposites( SimpleAssociationTest.Company.class );
        module.addAssociation().
            addAssociationInfo( new SimpleAssociationTest.DisplayInfo( "Employees", "Employees in the company", "Employees", null ) ).
            withAccessor( SimpleAssociationTest.Employer.class ).employees();

        new AssemblyHelper( module ).add( SimpleAssociationTest.Employee.class );
    }

    public void testAssociation()
    {
        SimpleAssociationTest.Company company;
        {
            CompositeBuilder<Company> builder = compositeBuilderFactory.newCompositeBuilder( SimpleAssociationTest.Company.class );
            builder.propertiesOfComposite().name().set( "JayWay" );
            company = builder.newInstance();
        }

        company.name().addChangeObserver( new PropertyChangeObserver<String>()
        {
            public void onChange( PropertyChange<String> propertyChange )
            {
                System.out.println( "Name changed from " + propertyChange.getProperty().get() + " to " + propertyChange.getNewValue() );
            }
        } );

        company.name().addAccessObserver( new PropertyAccessObserver<String>()
        {
            public void onAccess( PropertyAccess<String> propertyAccess )
            {
                System.out.println( "Name " + propertyAccess.getProperty().get() + " accessed from:" );
                new Exception().printStackTrace();
            }
        } );

        company.name().set( "Jayway" );

        System.out.println( "Name is:" + company.name().get() );

        CompositeBuilder<SimpleAssociationTest.Person> builder = compositeBuilderFactory.newCompositeBuilder( SimpleAssociationTest.Person.class );
        builder.propertiesOfComposite().name().set( "Rickard" );
        SimpleAssociationTest.Person rickard = builder.newInstance();

        company.employees().add( rickard );

        for( SimpleAssociationTest.Employer employer : rickard.employers() )
        {
            System.out.println( ( (SimpleAssociationTest.Nameable) employer ).name() );
        }
    }

    public interface Company
        extends SimpleAssociationTest.Nameable,
                SimpleAssociationTest.Employer,
                SimpleAssociationTest.StandardComposite,
                Composite
    {
    }

    public interface Person
        extends SimpleAssociationTest.Nameable,
                SimpleAssociationTest.Employee,
                SimpleAssociationTest.StandardComposite,
                Composite
    {
    }

    @Mixins( { PropertyMixin.class, AssociationMixin.class } )
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
