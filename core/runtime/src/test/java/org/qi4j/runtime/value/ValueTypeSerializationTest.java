/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin.
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Tests for ValueType serialization and deserialization.
 */
public class ValueTypeSerializationTest
    extends AbstractQi4jTest
{

    private static final Logger LOG = LoggerFactory.getLogger( ValueTypeSerializationTest.class );

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values( SomeValue.class, AnotherValue.class, FooValue.class, CustomFooValue.class,
                       SpecificCollection.class /*, SpecificValue.class, GenericValue.class */ );
    }

    @Test
    public void testTypeSerialization()
    {
        SomeValue some = buildSomeValue();

        // Serialize
        String json = some.toString();

        // Deserialize
        SomeValue some2 = module.newValueFromJSON( SomeValue.class, json );

        // Test date formats
        // ISO-6801 with timezone
        int idx1 = json.indexOf( "date\":\"" ) + 7;
        int idx2 = json.indexOf( '"', idx1 );
        String jsonTZ = json.substring( 0, idx1 ) + "2009-08-12T14:54:27.895+0800" + json.substring( idx2 );
        SomeValue someTZ = module.newValueFromJSON( SomeValue.class, jsonTZ );

        // @ format
        String jsonAt = json.substring( 0, idx1 ) + "@" + System.currentTimeMillis() + "@" + json.substring( idx2 );
        SomeValue someAt = module.newValueFromJSON( SomeValue.class, jsonAt );

        // Microsoft format
        String jsonMS = json.substring( 0, idx1 ) + "/Date(" + System.currentTimeMillis() + ")/" + json.substring( idx2 );
        SomeValue someMS = module.newValueFromJSON( SomeValue.class, jsonMS );

        // Log results
        LOG.info( "Some value toString():\n\n{}\n", some.toString() );
        LOG.info( "Some2 value toString():\n\n{}\n", some2.toString() );
        LOG.info( "Some value with ISO-6801 Date with TZ toString():\n\n{}\n", someTZ.toString() );
        LOG.info( "Some value with @ Date toString():\n\n{}\n", someAt.toString() );
        LOG.info( "Some value with Microsoft Date toString():\n\n{}\n", someMS.toString() );

        // Global assertions
        assertThat( "Same value", some.toString(), equalTo( some2.toString() ) );
        assertThat( "Same value", some, equalTo( some2 ) );
        assertThat( "Same JSON value", json, equalTo( some2.toString() ) );
        assertThat( "Same JSON value", some.customFoo().get() instanceof CustomFooValue, is( true ) );
        assertThat( "Same JSON value", some.customFooValue().get() instanceof CustomFooValue, is( true ) );

        // Other assertions
        assertThat( "String Integer Map", some2.stringIntMap().get().get( "foo" ), equalTo( 42 ) );
        assertThat( "String Value Map", some2.stringValueMap().get().get( "foo" ).internalVal(), equalTo( "Bar" ) );
    }

    /**
     * @return a SomeValue ValueComposite whose state is populated with test data.
     */
    private SomeValue buildSomeValue()
    {
        ValueBuilder<SomeValue> builder = module.newValueBuilder( SomeValue.class );
        SomeValue proto = builder.prototype();
        proto.anotherList().get().add( module.newValue( AnotherValue.class ) );

        ValueBuilder<SpecificCollection> specificColBuilder = module.newValueBuilder( SpecificCollection.class);
        SpecificCollection specificColProto = specificColBuilder.prototype();
        List<String> genericList = new ArrayList<String>();
        genericList.add( "Some" );
        genericList.add( "String" );
        specificColProto.genericList().set( genericList );
        proto.specificCollection().set( specificColBuilder.newInstance() );

/*
        ValueBuilder<SpecificValue> specificValue = module.newValueBuilder(SpecificValue.class);
        specificValue.prototype().item().set("Foo");
        proto.specificValue().set(specificValue.newInstance());
*/

        ValueBuilder<AnotherValue> valueBuilder = module.newValueBuilder( AnotherValue.class );
        valueBuilder.prototype().val1().set( "Foo" );
        valueBuilder.prototypeFor( AnotherValueInternalState.class ).val2().set( "Bar" );
        AnotherValue anotherValue = valueBuilder.newInstance();

        proto.string().set( "Foo\"Bar\"\nTest\f\t\b" );
        proto.string2().set( "/Foo/bar" );
        proto.number().set( 42L );
        proto.date().set( new Date() );
        proto.entityReference().set( EntityReference.parseEntityReference( "12345" ) );
        proto.stringIntMap().get().put( "foo", 42 );
        proto.stringIntMap().get().put( "bar", 67 );
        proto.stringValueMap().get().put( "foo", anotherValue );
        proto.another().set( anotherValue );
        proto.serializable().set( new SerializableObject() );
        proto.foo().set( module.newValue( FooValue.class ) );
        proto.fooValue().set( module.newValue( FooValue.class ) );
        proto.customFoo().set( module.newValue( CustomFooValue.class ) );
        proto.customFooValue().set( module.newValue( CustomFooValue.class ) );

        return builder.newInstance();
    }

    public enum TestEnum
    {
        somevalue, anothervalue
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

        @UseDefaults
        Property<Map<String, Integer>> stringIntMap();

        @UseDefaults
        Property<Map<String, AnotherValue>> stringValueMap();

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

        Property<SpecificCollection> specificCollection();

/* Too complicated to do generics here for now
        Property<SpecificValue> specificValue();
*/
    }

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

    @Mixins( AnotherValueMixin.class )
    public interface AnotherValue
        extends ValueComposite
    {

        @UseDefaults
        Property<String> val1();

        String internalVal();

    }

    public interface AnotherValueInternalState
    {

        @UseDefaults
        Property<String> val2();

    }

    public static abstract class AnotherValueMixin
        implements AnotherValue
    {

        @This
        private AnotherValueInternalState internalState;

        @Override
        public String internalVal()
        {
            return internalState.val2().get();
        }

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
        private static final long serialVersionUID = 1L;
        private String foo = "Foo";
        private int val = 35;

        @Override
        @SuppressWarnings( "AccessingNonPublicFieldOfAnotherObject" )
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
