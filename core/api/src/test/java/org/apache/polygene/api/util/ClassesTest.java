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
package org.apache.polygene.api.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.apache.polygene.api.util.Classes.interfacesOf;
import static org.apache.polygene.api.util.Classes.interfacesWithMethods;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for Classes
 */
public class ClassesTest
{

    @Test
    public void givenClassWithInterfacesWhenInterfacesOfThenGetCorrectSet()
    {
        assertThat( "one interface returned", interfacesOf( A.class ).count(), equalTo( 1L ) );
        assertThat( "two interface returned", interfacesOf( B.class ).count(), equalTo( 2L ) );
        assertThat( "tree interface returned", interfacesOf( C.class ).count(), equalTo( 4L ) );
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
        assertThat( "one interface returned", interfacesOf( C.class ).filter( Methods.HAS_METHODS )
            .count(), equalTo( 1L ) );
        boolean isIn = interfacesOf( C.class ).filter( Methods.HAS_METHODS )
            .anyMatch( B.class::equals );
        assertThat( "correct interface returned", isIn, is( true ) );
    }

    @Test
    public void givenClassNameWhenToUriThenUriIsReturned()
    {
        assertThat( "URI is correct", Classes.toURI( A.class ), equalTo( "urn:polygene:type:org.apache.polygene.api.util.ClassesTest-A" ) );
    }

    @Test
    public void givenUriWhenToClassNameThenClassNameIsReturned()
    {
        assertThat( "Class name is correct", Classes.toClassName( "urn:polygene:type:org.apache.polygene.api.util.ClassesTest-A" ), equalTo( "org.apache.polygene.api.util.ClassesTest$A" ) );
    }

    @Test
    public void givenGenericTypeWithWildCardWhenGetRawClassThenCorrectTypeIsReturned()
        throws NoSuchMethodException
    {
        Type returnType = Generics.class.getMethod( "wildcard" ).getGenericReturnType();
        Type wildcardType = ( (ParameterizedType) returnType ).getActualTypeArguments()[ 0 ];
        assertThat( "Return type is A", Classes.RAW_CLASS.apply( wildcardType ), equalTo( (Class) A.class ) );
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
            switch( method.getName() )
            {
            case "type":
                assertThat( resolvedType, equalTo( String.class ) );
                break;
            case "type1":
                assertThat( resolvedType, equalTo( String.class ) );
                break;
            case "type2":
                assertThat( resolvedType, equalTo( Long.class ) );
                break;
            }
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
