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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.swing.Icon;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.AppliesTo;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.Concerns;
import org.qi4j.composite.ConstraintDeclaration;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.scope.ConcernFor;
import org.qi4j.composite.scope.SideEffectFor;
import org.qi4j.composite.scope.ThisCompositeAs;
import org.qi4j.library.framework.entity.AssociationMixin;
import org.qi4j.library.framework.entity.PropertyMixin;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class SimplePropertyTest extends AbstractQi4jTest
{

    public void configure( ModuleAssembly module )
    {
        module.addComposites( Company.class );
        module.addProperty().
            setPropertyInfo( DisplayInfo.class, new DisplayInfo( "Name", "Name of something", "The name" ) ). // Add UI info
            setPropertyInfo( RdfInfo.class, new RdfInfo( "label", "http://www.w3.org/1999/02/22-rdf-syntax-ns#" ) ). // Add persistence info
            withAccessor( Nameable.class ).name(). // Select accessor
            set( "Hello World" ); // Set default value
    }

    public void testProperty()
    {
        Company company;
        {
            CompositeBuilder<Company> builder = compositeBuilderFactory.newCompositeBuilder( Company.class );
            builder.propertiesOfComposite().name().set( "JayWay" );
            company = builder.newInstance();
        }

        company.name().set( "Jayway" );

        System.out.println( "Name is:" + company.name().get() );
    }

    public interface Company
        extends Nameable,
                StandardComposite,
                Composite
    {
    }

    @Mixins( { PropertyMixin.class, AssociationMixin.class } )
    public interface StandardComposite
    {
    }

    @Concerns( CapitalizeConcern.class )
    public interface Nameable
    {
        @Capitalized Property<String> name();
    }

    @ConstraintDeclaration
    @Retention( RetentionPolicy.RUNTIME )
    @Target( { ElementType.METHOD } )
    public @interface Capitalized
    {
    }

    @AppliesTo( Capitalized.class )
    public static abstract class CapitalizeConcern
        implements Property<String>
    {
        @ConcernFor Property<String> next;

        public void set( String newValue ) throws PropertyVetoException
        {
            newValue = newValue.toUpperCase();
            next.set( newValue );
        }
    }

    public static abstract class LogPropertyAccess
        implements ImmutableProperty<String>
    {
        @SideEffectFor ImmutableProperty<String> next;
        @ThisCompositeAs PropertyInfo info;

        public String get()
        {
            System.out.println( "Property " + info.getName() + " accessed with value " + next.get() );
            return null;
        }
    }

    public static abstract class LogPropertyChanges
        implements Property<Object>
    {
        @SideEffectFor Property<Object> next;
        @ThisCompositeAs ImmutableProperty current;
        @ThisCompositeAs PropertyInfo info;

        public void set( Object newValue ) throws PropertyVetoException
        {
            System.out.println( "Property " + info.getName() + " changed from " + current.get() + " to " + newValue );
        }
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
