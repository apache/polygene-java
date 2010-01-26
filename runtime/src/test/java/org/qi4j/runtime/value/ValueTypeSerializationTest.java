/*
 * Copyright (c) 2007, Rickard �berg. All Rights Reserved.
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

package org.qi4j.runtime.value;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.junit.Test;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Tests for ValueComposites
 */
public class ValueTypeSerializationTest
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addValues( SomeValue.class, AnotherValue.class, FooValue.class, CustomFooValue.class );
        //, SpecificCollection.class, SpecificValue.class, GenericValue.class);
    }

    @Test
    public void testTypeSerialization()
    {
        ValueBuilder<SomeValue> builder = valueBuilderFactory.newValueBuilder( SomeValue.class );
        SomeValue proto = builder.prototype();
        proto.anotherList().get().add( valueBuilderFactory.newValue( AnotherValue.class ) );
/*
        proto.specificCollection().set(valueBuilderFactory.newValue(SpecificCollection.class));
        proto.specificCollection().get().genericList().get().add("Some");
        proto.specificCollection().get().genericList().get().add("String");
        ValueBuilder<SpecificValue> specificValue = valueBuilderFactory.newValueBuilder(SpecificValue.class);
        specificValue.prototype().item().set("Foo");
        proto.specificValue().set(specificValue.newInstance());
*/
        ValueBuilder<AnotherValue> valueBuilder = valueBuilderFactory.newValueBuilder( AnotherValue.class );
        valueBuilder.prototype().val1().set( "Foo" );

        proto.string().set( "Foo\"Bar\"\nTest\f\t\b" );
        proto.string2().set( "/Foo/bar" );
        proto.number().set( 42L );
        proto.date().set( new Date() );
        proto.entityReference().set( EntityReference.parseEntityReference( "12345" ) );
        proto.another().set( valueBuilder.newInstance() );
        proto.serializable().set( new SerializableObject() );
        proto.foo().set( valueBuilderFactory.newValue( FooValue.class ) );
        proto.fooValue().set( valueBuilderFactory.newValue( FooValue.class ) );
        proto.customFoo().set( valueBuilderFactory.newValue( CustomFooValue.class ) );
        proto.customFooValue().set( valueBuilderFactory.newValue( CustomFooValue.class ) );
        proto.explicit().set( "foobar" );
        SomeValue some = builder.newInstance();

        String json = some.toJSON();

        Logger.getLogger( getClass().getName() ).info( some.string().get() );
        Logger.getLogger( getClass().getName() ).info( json );

        SomeValue some2 = valueBuilderFactory.newValueFromJSON( SomeValue.class, json );

        // Test date formats
        // ISO-6801 with timezone
        int idx1 = json.indexOf( "date\":\"" ) + 7;
        int idx2 = json.indexOf( '"', idx1 );
        String jsonTZ = json.substring( 0, idx1 ) + "2009-08-12T14:54:27.895+0800" + json.substring( idx2 );
        SomeValue someTZ = valueBuilderFactory.newValueFromJSON( SomeValue.class, jsonTZ );

        // @ format
        String jsonAt = json.substring( 0, idx1 ) + "@" + System.currentTimeMillis() + "@" + json.substring( idx2 );
        SomeValue someAt = valueBuilderFactory.newValueFromJSON( SomeValue.class, jsonAt );

        // Microsoft format
        String jsonMS = json.substring( 0, idx1 ) + "/Date(" + System.currentTimeMillis() + ")/" + json.substring( idx2 );
        SomeValue someMS = valueBuilderFactory.newValueFromJSON( SomeValue.class, jsonMS );

        System.out.println( some.toJSON() );
        System.out.println( some2.toJSON() );
        System.out.println( someTZ.toJSON() );
        System.out.println( someAt.toJSON() );
        System.out.println( someMS.toJSON() );

        assertThat( "Same value", some.toString(), equalTo( some2.toString() ) );
        assertThat( "Same value", some, equalTo( some2 ) );
        assertThat( "Same JSON value", json, equalTo( some2.toJSON() ) );
        assertThat( "Same JSON value", some.customFoo().get() instanceof CustomFooValue, is( true ) );
        assertThat( "Same JSON value", some.customFooValue().get() instanceof CustomFooValue, is( true ) );
    }

    public enum TestEnum
    {
        somevalue, anothervalue
    }

    public interface ExplicitPropertyType
        extends Property<String>
    {
    }

    public interface SomeValue
        extends ValueComposite
    {
        Property<String> string();

        Property<String> string2();

        @Optional
        Property<String> nullString();

        @UseDefaults
        Property<String> emptyString();

        @UseDefaults
        Property<Long> number();

        Property<Date> date();

        Property<EntityReference> entityReference();

        @UseDefaults
        Property<List<String>> stringList();

        @Optional
        Property<AnotherValue> another();

        @UseDefaults
        Property<List<AnotherValue>> anotherList();

        @UseDefaults
        Property<TestEnum> testEnum();

        Property<Object> serializable();

        Property<Foo> foo();

        Property<FooValue> fooValue();

        Property<Foo> customFoo();

        Property<FooValue> customFooValue();

        ExplicitPropertyType explicit();

/* Too complicated to do generics here for now
        Property<SpecificCollection> specificCollection();

        Property<SpecificValue> specificValue();
*/
    }

/*
    public interface SpecificCollection
        extends GenericCollection<String>
    {}

    public interface GenericCollection<TYPE>
        extends ValueComposite
    {
        @UseDefaults
        Property<List<TYPE>> genericList();
    }

    public interface SpecificValue
        extends GenericValue<String>
    {}

    public interface GenericValue<TYPE>
        extends ValueComposite
    {
        @Optional
        Property<TYPE> item();
    }
*/

    public interface AnotherValue
        extends ValueComposite
    {
        @UseDefaults
        Property<String> val1();
    }

    public interface Foo
    {
        @UseDefaults
        Property<String> bar();
    }

    public interface FooValue
        extends Foo, ValueComposite
    {
    }

    public interface CustomFooValue
        extends FooValue
    {
        @UseDefaults
        Property<String> custom();
    }

    public static class SerializableObject
        implements Serializable
    {
        String foo = "Foo";
        int val = 35;

        @Override
        public boolean equals( Object o )
        {
            if( this == o )
            {
                return true;
            }
            if( o == null || getClass() != o.getClass() )
            {
                return false;
            }
            SerializableObject that = (SerializableObject) o;
            return val == that.val && foo.equals( that.foo );
        }

        @Override
        public int hashCode()
        {
            int result = foo.hashCode();
            result = 31 * result + val;
            return result;
        }
    }
}
