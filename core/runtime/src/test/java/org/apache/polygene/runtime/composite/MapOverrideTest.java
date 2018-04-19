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
package org.apache.polygene.runtime.composite;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.concern.ConcernOf;
import org.apache.polygene.api.injection.scope.Invocation;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This test shows how to override any java.util.Map to become read-only. HashMap will be used as the Mixin!!
 *
 * Note that keySet(), values() and entrySet() would ALSO require overloading, but this has been left out for
 * clarity reasons.
 */
@Disabled( "Awaiting QI-298 - Allow Transients to be declared by classes" )
public class MapOverrideTest
    extends AbstractPolygeneTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // unable to add the concern, since it is applied on the prototype too!
        // this seems to be a generic problem with prototypes.
        module.transients( Map.class ).withMixins( HashMap.class ).withConcerns( ReadOnlyMapConcern.class );
    }

    @Test
    public void givenReadOnlyAnnotatedHashMapWhenCallingSizeExpectSuccess()
    {
        ValueBuilder<Map> builder = valueBuilderFactory.newValueBuilder( Map.class );
        Map<String, String> prototype = builder.prototype();
        prototype.put( "Niclas", "Hedhman" );
        Map<String, String> underTest = builder.newInstance();
        assertThat( underTest.size(), equalTo( 1 ) );
    }

    @Test
    public void givenReadOnlyAnnotatedHashMapWhenCallingIsEmptyExpectSuccess()
    {
        ValueBuilder<Map> builder = valueBuilderFactory.newValueBuilder( Map.class );
        Map<String, String> prototype = builder.prototype();
        prototype.put( "Niclas", "Hedhman" );
        Map<String, String> underTest = builder.newInstance();
        assertThat( underTest.isEmpty(), equalTo( false ) );
    }

    @Test
    public void givenReadOnlyAnnotatedHashMapWhenCallingContainsKeyExpectSuccess()
    {
        ValueBuilder<Map> builder = valueBuilderFactory.newValueBuilder( Map.class );
        Map<String, String> prototype = builder.prototype();
        prototype.put( "Niclas", "Hedhman" );
        Map<String, String> underTest = builder.newInstance();
        assertThat( underTest.containsKey( "Niclas" ), equalTo( true ) );
    }

    @Test
    public void givenReadOnlyAnnotatedHashMapWhenCallingContainsValueExpectSuccess()
    {
        ValueBuilder<Map> builder = valueBuilderFactory.newValueBuilder( Map.class );
        Map<String, String> prototype = builder.prototype();
        prototype.put( "Niclas", "Hedhman" );
        Map<String, String> underTest = builder.newInstance();
        assertThat( underTest.containsValue( "Hedhman" ), equalTo( true ) );
    }

    @Test
    public void givenReadOnlyAnnotatedHashMapWhenCallingGetExpectSuccess()
    {
        ValueBuilder<Map> builder = valueBuilderFactory.newValueBuilder( Map.class );
        Map<String, String> prototype = builder.prototype();
        prototype.put( "Niclas", "Hedhman" );
        Map<String, String> underTest = builder.newInstance();
        assertThat( underTest.get( "Niclas" ), equalTo( "Hedhman" ) );
    }

    @Test
    public void givenReadOnlyAnnotatedHashMapWhenCallingKeySetExpectSuccess()
    {
        ValueBuilder<Map> builder = valueBuilderFactory.newValueBuilder( Map.class );
        Map<String, String> prototype = builder.prototype();
        prototype.put( "Niclas", "Hedhman" );
        Map<String, String> underTest = builder.newInstance();
        assertThat( underTest.keySet(), equalTo( Collections.singleton( "Niclas" ) ) );
    }

    @Test
    public void givenReadOnlyAnnotatedHashMapWhenCallingEntrySetExpectSuccess()
    {
        // TODO
    }

    @Test
    public void givenReadOnlyAnnotatedHashMapWhenCallingValuesExpectSuccess()
    {
        ValueBuilder<Map> builder = valueBuilderFactory.newValueBuilder( Map.class );
        Map<String, String> prototype = builder.prototype();
        prototype.put( "Niclas", "Hedhman" );
        Map<String, String> underTest = builder.newInstance();
        Collection<String> values = Collections.singletonList( "Hedhman" );
        assertThat( underTest.values().size(), equalTo( values.size() ) );
        assertThat( underTest.values().contains( "Hedhman" ), equalTo( true ) );
    }

    @Test
    public void givenReadOnlyAnnotatedHashMapWhenCallingPutExpectReadOnlyException()
    {
        assertThrows( ReadOnlyException.class, () -> {
            // TODO
        } );
    }

    @Test
    public void givenReadOnlyAnnotatedHashMapWhenCallingRemoveExpectReadOnlyException()
    {
        assertThrows( ReadOnlyException.class, () -> {
            // TODO
        } );
    }

    @Test
    public void givenReadOnlyAnnotatedHashMapWhenCallingPutAllExpectReadOnlyException()
    {
        assertThrows( ReadOnlyException.class, () -> {
            // TODO
        } );
    }

    @Test
    public void givenReadOnlyAnnotatedHashMapWhenCallingClearExpectReadOnlyException()
    {
        assertThrows( ReadOnlyException.class, () -> {
            // TODO
        } );
    }

    public static abstract class ReadOnlyMapConcern extends ConcernOf<Map>
        implements Map
    {
        @Invocation
        private Method method;

        @This
        private Composite me;

        @Override
        public Object put( Object key, Object value )
        {
            throw new ReadOnlyException( me, method );
        }

        @Override
        public Object remove( Object key )
        {
            throw new ReadOnlyException( me, method );
        }

        @Override
        public void putAll( Map m )
        {
            throw new ReadOnlyException( me, method );
        }

        @Override
        public void clear()
        {
            throw new ReadOnlyException( me, method );
        }
    }

    private static class ReadOnlyException
        extends RuntimeException
    {
        public ReadOnlyException( Composite me, Method method )
        {
            super( "Method " + method.getName() + " in [" + me.toString() + "] is READ ONLY." );
        }
    }

}
