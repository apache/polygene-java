/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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
package org.qi4j.api.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.functional.Specifications;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.qi4j.api.util.Classes.interfacesOf;
import static org.qi4j.api.util.Classes.interfacesWithMethods;
import static org.qi4j.functional.Iterables.count;

/**
 * Tests for Classes
 */
public class ClassesTest
{

    @Test
    public void givenClassWithInterfacesWhenInterfacesOfThenGetCorrectSet()
    {
        assertThat( "one interface returned", count( interfacesOf( A.class ) ), equalTo( 1L ) );
        assertThat( "two interface returned", count( interfacesOf( B.class ) ), equalTo( 2L ) );
        assertThat( "tree interface returned", count( interfacesOf( C.class ) ), equalTo( 4L ) );
    }

    @Test
    public void givenClassWithInterfacesWhenGetInterfacesWithMethodsThenGetCorrectSet()
    {
        HashSet<Class<?>> interfaces = new HashSet<Class<?>>();
        interfaces.add( B.class );
        Set<Class<?>> types = interfacesWithMethods( interfaces );
        assertThat( "one interface returned", types.size(), equalTo( 1 ) );
        assertThat( "correct interface returned", types.contains( B.class ), is( true ) );
    }

    @Test
    public void givenClassesWithInterfacesWhenGetInterfacesWithMethodsThenGetCorrectSet()
    {
        Iterable<Type> types = Iterables.filter( Methods.HAS_METHODS, interfacesOf( C.class ) );
        assertThat( "one interface returned", count( types ), equalTo( 1L ) );
        assertThat( "correct interface returned", Iterables.matchesAny( (Specification) Specifications.in( B.class ), Iterables
            .<Class<?>>cast( types ) ), is( true ) );
    }

    @Test
    public void givenClassNameWhenToUriThenUriIsReturned()
    {
        assertThat( "URI is correct", Classes.toURI( A.class ), equalTo( "urn:qi4j:type:org.qi4j.api.util.ClassesTest-A" ) );
    }

    @Test
    public void givenUriWhenToClassNameThenClassNameIsReturned()
    {
        assertThat( "Class name is correct", Classes.toClassName( "urn:qi4j:type:org.qi4j.api.util.ClassesTest-A" ), equalTo( "org.qi4j.api.util.ClassesTest$A" ) );
    }

    @Test
    public void givenGenericTypeWithWildCardWhenGetRawClassThenCorrectTypeIsReturned()
        throws NoSuchMethodException
    {
        Type returnType = Generics.class.getMethod( "wildcard" ).getGenericReturnType();
        Type wildcardType = ( (ParameterizedType) returnType ).getActualTypeArguments()[ 0];
        assertThat( "Return type is A", Classes.RAW_CLASS.map( wildcardType ), equalTo( (Class) A.class ) );
    }

    @Test
    public void givenTypeVariableWhenResolveThenResolved()
    {
        for( Method method : Type1.class.getMethods() )
        {
            Type type = method.getGenericReturnType();
            TypeVariable typeVariable = (TypeVariable) type;
            Type resolvedType = Classes.resolveTypeVariable( typeVariable, method.getDeclaringClass(), Type1.class );
            System.out.println( type + "=" + resolvedType );
        }
    }

    @Test
    public void givenGenericTypeWhenGetSimpleGenericNameThenCorrectStringIsReturned()
        throws NoSuchMethodException
    {
        assertThat( "Simple Generic Name is 'A'",
                    Classes.simpleGenericNameOf( A.class ),
                    equalTo( "A" ) );
        assertThat( "Simple Generic Name is 'B'",
                    Classes.simpleGenericNameOf( B.class ),
                    equalTo( "B" ) );
        assertThat( "Simple Generic Name is 'C'",
                    Classes.simpleGenericNameOf( C.class ),
                    equalTo( "C" ) );

        assertThat( "Simple Generic Name is 'Generics'",
                    Classes.simpleGenericNameOf( Generics.class ),
                    equalTo( "Generics" ) );
        assertThat( "Simple Generic Name is 'Iterable<? extends A>'",
                    Classes.simpleGenericNameOf( Generics.class.getMethod( "wildcard" ).getGenericReturnType() ),
                    equalTo( "Iterable<? extends A>" ) );

        assertThat( "Simple Generic Name is 'Type1'",
                    Classes.simpleGenericNameOf( Type1.class ),
                    equalTo( "Type1" ) );
        assertThat( "Simple Generic Name is 'TYPE'",
                    Classes.simpleGenericNameOf( Type1.class.getMethod( "type" ).getGenericReturnType() ),
                    equalTo( "TYPE" ) );
        assertThat( "Simple Generic Name is 'TYPE1'",
                    Classes.simpleGenericNameOf( Type1.class.getMethod( "type1" ).getGenericReturnType() ),
                    equalTo( "TYPE1" ) );
        assertThat( "Simple Generic Name is 'TYPE2'",
                    Classes.simpleGenericNameOf( Type1.class.getMethod( "type2" ).getGenericReturnType() ),
                    equalTo( "TYPE2" ) );

        assertThat( "Simple Generic Name is 'Type2'",
                    Classes.simpleGenericNameOf( Type2.class ),
                    equalTo( "Type2" ) );
        assertThat( "Simple Generic Name is 'TYPE'",
                    Classes.simpleGenericNameOf( Type2.class.getMethod( "type" ).getGenericReturnType() ),
                    equalTo( "TYPE" ) );
        assertThat( "Simple Generic Name is 'TYPE1'",
                    Classes.simpleGenericNameOf( Type2.class.getMethod( "type1" ).getGenericReturnType() ),
                    equalTo( "TYPE1" ) );
        assertThat( "Simple Generic Name is 'TYPE2'",
                    Classes.simpleGenericNameOf( Type2.class.getMethod( "type2" ).getGenericReturnType() ),
                    equalTo( "TYPE2" ) );

        assertThat( "Simple Generic Name is 'Type3'",
                    Classes.simpleGenericNameOf( Type3.class ),
                    equalTo( "Type3" ) );
        assertThat( "Simple Generic Name is 'TYPE'",
                    Classes.simpleGenericNameOf( Type3.class.getMethod( "type" ).getGenericReturnType() ),
                    equalTo( "TYPE" ) );
    }

    interface A
    {
    }

    interface B
        extends A
    {

        public void doStuff();

    }

    interface C
        extends A, B
    {
    }

    interface Generics
    {

        Iterable<? extends A> wildcard();

    }

    interface Type1
        extends Type2<String, Long>
    {
    }

    interface Type2<TYPE1, TYPE2>
        extends Type3<TYPE1>
    {

        TYPE1 type1();

        TYPE2 type2();

    }

    interface Type3<TYPE>
    {

        TYPE type();

    }

}
