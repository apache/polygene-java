/*
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.runtime.composite;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.Invocation;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * This test shows how to override any java.util.Map to become read-only. HashMap will be used as the Mixin!!
 *
 * Note that keySet(), values() and entrySet() would ALSO require overloading, but this has been left out for
 * clarity reasons.
 */
@Ignore("Subclassing can not occur in java.* packages, and need to be prefixed.")
public class MapOverrideTest
    extends AbstractQi4jTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values( Map.class ).withMixins( HashMap.class ).withConcerns( ReadOnlyMapConcern.class );
    }

    @Test @Ignore("Until we can override JDK classes in FragmentClassLoader" )
    public void givenReadOnlyAnnotatedHashMapWhenCallingSizeExpectSuccess()
    {
        ValueBuilder<Map> builder = module.newValueBuilder( Map.class );
        Map<String,String> prototype = builder.prototype();
        prototype.put( "Niclas", "Hedhman" );
        Map<String,String> underTest = builder.newInstance();
        assertThat( underTest.get( "Niclas" ), equalTo("Hedhman") );
    }

    @Test
    public void givenReadOnlyAnnotatedHashMapWhenCallingIsEmptyExpectSuccess()
    {

    }

    @Test
    public void givenReadOnlyAnnotatedHashMapWhenCallingContainsKeyExpectSuccess()
    {

    }

    @Test
    public void givenReadOnlyAnnotatedHashMapWhenCallingContainsValueExpectSuccess()
    {

    }

    @Test
    public void givenReadOnlyAnnotatedHashMapWhenCallingGetExpectSuccess()
    {

    }

    @Test
    public void givenReadOnlyAnnotatedHashMapWhenCallingKeySetExpectSuccess()
    {

    }

    @Test
    public void givenReadOnlyAnnotatedHashMapWhenCallingEntrySetExpectSuccess()
    {

    }

    @Test
    public void givenReadOnlyAnnotatedHashMapWhenCallingValuesExpectSuccess()
    {

    }

    @Test( expected = ReadOnlyException.class )
    public void givenReadOnlyAnnotatedHashMapWhenCallingPutExpectReadOnlyException()
    {

    }

    @Test( expected = ReadOnlyException.class )
    public void givenReadOnlyAnnotatedHashMapWhenCallingRemoveExpectReadOnlyException()
    {

    }

    @Test( expected = ReadOnlyException.class )
    public void givenReadOnlyAnnotatedHashMapWhenCallingPutAllExpectReadOnlyException()
    {

    }

    @Test( expected = ReadOnlyException.class )
    public void givenReadOnlyAnnotatedHashMapWhenCallingClearExpectReadOnlyException()
    {

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

    private static class ReadOnlyException extends RuntimeException
    {
        public ReadOnlyException( Composite me, Method method )
        {
            super( "Method " + method.getName() + " in [" + me.toString() + "] is READ ONLY." );
        }
    }
}
