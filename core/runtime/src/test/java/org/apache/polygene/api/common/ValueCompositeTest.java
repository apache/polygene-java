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

package org.apache.polygene.api.common;

import java.util.List;
import org.apache.polygene.api.constraint.ConstraintViolationException;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for ValueComposites
 */
public class ValueCompositeTest
    extends AbstractPolygeneTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values( SomeValue.class, AnotherValue.class );
        module.entities( SomeEntity.class );
        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void testImmutabilityOfValueComposite()
    {
        assertThrows( IllegalStateException.class, () -> {
            ValueBuilder<SomeValue> builder = valueBuilderFactory.newValueBuilder( SomeValue.class );
            SomeValue some = builder.prototype();
            some.other().set( "test" );
            some = builder.newInstance();
            some.other().set( "test2" );
        } );
    }

    @Test
    public void testCreationOfValueComposite()
    {
        ValueBuilder<SomeValue> builder = valueBuilderFactory.newValueBuilder( SomeValue.class );
        SomeValue some = builder.prototype();
        some.other().set( "test" );
        builder.newInstance();
    }

    @Test
    public void testEqualityOfValueComposite()
    {
        ValueBuilder<SomeValue> builder = valueBuilderFactory.newValueBuilder( SomeValue.class );
        SomeValue prototype = builder.prototype();
        prototype.other().set( "test" );
        SomeValue instance = builder.newInstance();

        builder = valueBuilderFactory.newValueBuilder( SomeValue.class );
        prototype = builder.prototype();
        prototype.other().set( "test" );
        SomeValue other = builder.newInstance();
        assertThat( "Instances should not be the same.", instance == other, is( false ) );
        assertThat( "Equal values.", other, equalTo( instance ) );
    }

    @Test
    public void testHashcodeOfValueComposite()
    {
        ValueBuilder<SomeValue> builder = valueBuilderFactory.newValueBuilder( SomeValue.class );
        SomeValue prototype = builder.prototype();
        prototype.other().set( "test" );
        SomeValue instance = builder.newInstance();

        builder = valueBuilderFactory.newValueBuilder( SomeValue.class );
        prototype = builder.prototype();
        prototype.other().set( "test" );
        SomeValue other = builder.newInstance();
        assertThat( "Instances should not be the same.", instance == other, is( false ) );
        assertThat( "Equal values.", other.hashCode(), equalTo( instance.hashCode() ) );
    }

    @Test
    public void testModifyValue()
    {
        ValueBuilder<AnotherValue> anotherBuilder = valueBuilderFactory.newValueBuilder( AnotherValue.class );
        anotherBuilder.prototype().val1().set( "Val1" );
        AnotherValue anotherValue = anotherBuilder.newInstance();

        ValueBuilder<SomeValue> builder = valueBuilderFactory.newValueBuilder( SomeValue.class );
        SomeValue prototype = builder.prototype();
        prototype.some().set( "foo" );
        prototype.other().set( "test" );
        prototype.xyzzyList().get().add( "blah" );
        prototype.another().set( anotherValue );
        SomeValue instance = builder.newInstance();

        assertThat( "List has value blah", instance.xyzzyList().get().get( 0 ), equalTo( "blah" ) );

        // Modify value
        builder = valueBuilderFactory.newValueBuilderWithPrototype( instance );
        builder.prototype().some().set( "bar" );
        instance = builder.newInstance();

        assertThat( "Other is set to test", instance.other().get(), equalTo( "test" ) );
        assertThat( "List has value blah", instance.xyzzyList().get().get( 0 ), equalTo( "blah" ) );
        assertThat( "AnotherValue.val1 has value Val1", instance.another().get().val1().get(), equalTo( "Val1" ) );

        // Modify value again using method 2
        builder = valueBuilderFactory.newValueBuilderWithPrototype( instance );
        builder.prototype().other().set( "test2" );
        instance = builder.newInstance();

        assertThat( "Other is set to test2", instance.other().get(), equalTo( "test2" ) );
        assertThat( "Some is set to bar", instance.some().get(), equalTo( "bar" ) );
    }

    @Test
    public void givenValueWhenModifyToIncorrectValueThenThrowConstraintException()
    {
        assertThrows( ConstraintViolationException.class, () -> {
            ValueBuilder<SomeValue> builder = valueBuilderFactory.newValueBuilder( SomeValue.class );
            SomeValue prototype = builder.prototype();
            prototype.some().set( "foo" );
            SomeValue instance = builder.newInstance();

            builder = valueBuilderFactory.newValueBuilderWithPrototype( instance );
            builder.prototype().some().set( null );
        } );
    }

    @Test
    public void givenValueWithListOfValueWhenPrototypeThenListedValuesAreEditable()
    {
        ValueBuilder<SomeValue> builder = valueBuilderFactory.newValueBuilder( SomeValue.class );
        builder.prototype().anotherList().get().add( valueBuilderFactory.newValue( AnotherValue.class ) );
        SomeValue some = builder.newInstance();

        builder = valueBuilderFactory.newValueBuilderWithPrototype( some );
        builder.prototype().anotherList().get().get( 0 ).val1().set( "Foo" );
        builder.prototype().anotherList().get().add( valueBuilderFactory.newValue( AnotherValue.class ) );
        some = builder.newInstance();

        assertThat( "Val1 has been set", some.anotherList().get().get( 0 ).val1().get(), equalTo( "Foo" ) );

        try
        {
            some.anotherList().get().get( 0 ).val1().set( "Bar" );
            fail( "Should not be allowed to modify value" );
        }
        catch( IllegalStateException e )
        {
            // Ok
        }
    }

    @Test
    public void givenEntityWhenUpdateValueThenValueIsSet()
        throws UnitOfWorkCompletionException
    {
        ValueBuilder<SomeValue> builder = valueBuilderFactory.newValueBuilder( SomeValue.class );
        builder.prototype().anotherList().get().add( valueBuilderFactory.newValue( AnotherValue.class ) );
        ValueBuilder<AnotherValue> valueBuilder = valueBuilderFactory.newValueBuilder( AnotherValue.class );
        valueBuilder.prototype().val1().set( "Foo" );
        builder.prototype().another().set( valueBuilder.newInstance() );
        builder.prototype().number().set( 42L );
        SomeValue some = builder.newInstance();

        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        EntityBuilder<SomeEntity> entityBuilder = unitOfWork.newEntityBuilder( SomeEntity.class );
        entityBuilder.instance().someValue().set( some );
        SomeEntity entity = entityBuilder.newInstance();

        assertThat( "Value has been set", entity.someValue().get().another().get().val1().get(), equalTo( "Foo" ) );

        unitOfWork.complete();
    }

    public enum TestEnum
    {
        somevalue, anothervalue
    }

    public interface SomeValue
        extends ValueComposite
    {
        @UseDefaults
        Property<String> some();

        @UseDefaults
        Property<String> other();

        @UseDefaults
        Property<Long> number();

        @UseDefaults
        Property<List<String>> xyzzyList();

        @Optional
        Property<AnotherValue> another();

        @UseDefaults
        Property<List<AnotherValue>> anotherList();

        @UseDefaults
        Property<TestEnum> testEnum();
    }

    public interface AnotherValue
        extends ValueComposite
    {
        @UseDefaults
        Property<String> val1();
    }

    public interface SomeEntity
        extends EntityComposite
    {
        Property<SomeValue> someValue();
    }
}