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

package org.qi4j.runtime.property;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.swing.Icon;
import org.junit.Test;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyInfo;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Tests for properties
 */
public class PropertyTest
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addTransients( Company.class );
        module.forMixin( Nameable.class )
            .setMetaInfo( new DisplayInfo( "Name", "Name of something", "The name" ) )  // Add UI info
            .setMetaInfo( new RdfInfo( "label", "http://www.w3.org/1999/02/22-rdf-syntax-ns#" ) )  // Add persistence info
            .declareDefaults()
            .name()  // Select accessor
            .set( "Hello World" ); // Set default value
    }

    @Test
    public void testProperty()
    {
        Company company;
        {
            TransientBuilder<Company> builder = transientBuilderFactory.newTransientBuilder( Company.class );
            builder.prototype().name().set( "JayWay" );
            company = builder.newInstance();
        }

        company.name().set( "Jayway" );

        System.out.println( "Name is:" + company.name().get() );
    }

    public interface Company
        extends Nameable, StandardComposite, TransientComposite
    {
    }

    public interface StandardComposite
    {
    }

    @Concerns( CapitalizeConcern.class )
    public interface Nameable
    {
        @Capitalized
        Property<String> name();
    }

    @Retention( RetentionPolicy.RUNTIME )
    @Target( { ElementType.METHOD } )
    public @interface Capitalized
    {
    }

    @AppliesTo( Capitalized.class )
    public static abstract class CapitalizeConcern
        extends ConcernOf<Property<String>>
        implements Property<String>
    {
        public void set( String newValue )
            throws IllegalArgumentException
        {
            newValue = newValue.toUpperCase();
            next.set( newValue );
        }
    }

    public static abstract class LogPropertyAccess
        extends SideEffectOf<Property<String>>
        implements Property<String>
    {
        @This
        PropertyInfo info;

        public String get()
        {
            System.out.println( "Property " + info.qualifiedName().name() + " accessed with value " + result.get() );
            return null;
        }
    }

    public static abstract class LogPropertyChanges
        extends SideEffectOf<Property<String>>
        implements Property<Object>
    {
        @This
        Property current;
        @This
        PropertyInfo info;

        public void set( Object newValue )
            throws IllegalArgumentException
        {
            System.out
                .println( "Property " + info.qualifiedName()
                    .name() + " changed from " + current.get() + " to " + newValue );
        }
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
