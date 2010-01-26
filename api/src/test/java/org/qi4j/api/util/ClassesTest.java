/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.qi4j.api.util.Classes.*;

/**
 * Tests for Classes
 */
public class ClassesTest
{
    @Test
    public void givenClassWithInterfacesWhenInterfacesOfThenGetCorrectSet()
    {
        assertThat( "one interface returned", interfacesOf( A.class ).size(), equalTo( 1 ) );
        assertThat( "two interface returned", interfacesOf( B.class ).size(), equalTo( 2 ) );
        assertThat( "tree interface returned", interfacesOf( C.class ).size(), equalTo( 3 ) );
    }

    @Test
    public void givenClassWithInterfacesWhenGetInterfacesWithMethodsThenGetCorrectSet()
    {
        HashSet<Class> interfaces = new HashSet<Class>();
        interfaces.add( B.class );
        Set<Class> types = interfacesWithMethods( interfaces );
        assertThat( "one interface returned", types.size(), equalTo( 1 ) );
        assertThat( "correct interface returned", types.contains( B.class ), is( true ) );
    }

    @Test
    public void givenClassesWithInterfacesWhenGetInterfacesWithMethodsThenGetCorrectSet()
    {
        Set<Class> types = interfacesWithMethods( interfacesOf( C.class ) );
        assertThat( "one interface returned", types.size(), equalTo( 1 ) );
        assertThat( "correct interface returned", types.contains( B.class ), is( true ) );
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
        Type wildcardType = ( (ParameterizedType) returnType ).getActualTypeArguments()[ 0 ];
        assertThat( "Return type is A", (Class) Classes.getRawClass( wildcardType ), equalTo( (Class) A.class ) );
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
