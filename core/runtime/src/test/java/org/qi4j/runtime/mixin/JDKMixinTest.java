/*
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
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
package org.qi4j.runtime.mixin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.functional.Specification;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.core.IsEqual.*;
import static org.junit.Assert.*;
import static org.qi4j.functional.Iterables.*;

/**
 * Assert that JDK classes are usable as Mixins.
 */
public class JDKMixinTest
    extends AbstractQi4jTest
{

    @Concerns( JDKMixinConcern.class )
    public interface JSONSerializableMap
        extends Map<String, String>
    {

        JSONObject toJSON();

    }

    @SuppressWarnings( "serial" )
    public static class ExtendsJDKMixin
        extends HashMap<String, String>
        implements JSONSerializableMap
    {

        @Override
        public JSONObject toJSON()
        {
            System.out.println( ">>>> Call ExtendsJDKMixin.toJSON()" );
            // Copy the Map before handing it to JSONObject so that the JSONObject do not use the Composite
            return new JSONObject( new HashMap<String, String>( this ) );
        }

    }

    public static abstract class ComposeWithJDKMixin
        implements JSONSerializableMap
    {

        @This
        private Map<String, String> map;

        @Override
        public JSONObject toJSON()
        {
            System.out.println( ">>>> Call ComposeWithJDKMixin.toJSON()" );
            // Copy the Map before handing it to JSONObject so that the JSONObject do not use the Composite
            return new JSONObject( new HashMap<String, String>( map ) );
        }

    }

    public static class JDKMixinConcern
        extends GenericConcern
    {

        @Override
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            System.out.println( ">>>> Call to JDKMixinConcern." + method.getName() );
            CONCERN_RECORDS.add( method.getName() );
            return next.invoke( proxy, method, args );
        }

    }

    private static final String EXTENDS_IDENTITY = ExtendsJDKMixin.class.getName();
    private static final String COMPOSE_IDENTITY = ComposeWithJDKMixin.class.getName();
    private static final Specification<ServiceReference<?>> EXTENDS_IDENTITY_SPEC = new ServiceIdentitySpec( EXTENDS_IDENTITY );
    private static final Specification<ServiceReference<?>> COMPOSE_IDENTITY_SPEC = new ServiceIdentitySpec( COMPOSE_IDENTITY );
    private static final List<String> CONCERN_RECORDS = new ArrayList<String>();

    @Before
    public void beforeEachTest()
    {
        CONCERN_RECORDS.clear();
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( JSONSerializableMap.class ).
            identifiedBy( EXTENDS_IDENTITY ).
            withMixins( ExtendsJDKMixin.class ).
            instantiateOnStartup();

        module.layer().module( "compose" ).services( JSONSerializableMap.class ).
            visibleIn( Visibility.layer ).
            identifiedBy( COMPOSE_IDENTITY ).
            withMixins( HashMap.class, ComposeWithJDKMixin.class ).
            instantiateOnStartup();
    }

    @Test
    public void testMixinExtendsJDK()
    {
        List<ServiceReference<JSONSerializableMap>> services = toList(
            filter( EXTENDS_IDENTITY_SPEC,
                    module.findServices( JSONSerializableMap.class ) ) );

        assertThat( services.size(), equalTo( 1 ) );
        assertThat( services.get( 0 ).identity(), equalTo( EXTENDS_IDENTITY ) );

        JSONSerializableMap extending = services.get( 0 ).get();
        extending.put( "foo", "bar" ); // Concern trigger #1 (put)
        JSONObject json = extending.toJSON(); // Concern trigger #2, #3 and #4 (toJSON, size, entrySet)

        assertThat( json.length(), equalTo( 1 ) );
        assertThat( json.optString( "foo" ), equalTo( "bar" ) );

        assertThat( CONCERN_RECORDS.size(), equalTo( 4 ) );
    }

    @Test
    public void testComposeJDKMixin()
    {
        List<ServiceReference<JSONSerializableMap>> services = toList(
            filter( COMPOSE_IDENTITY_SPEC,
                    module.findServices( JSONSerializableMap.class ) ) );

        assertThat( services.size(), equalTo( 1 ) );
        assertThat( services.get( 0 ).identity(), equalTo( COMPOSE_IDENTITY ) );

        JSONSerializableMap composing = services.get( 0 ).get();
        composing.put( "foo", "bar" ); // Concern trigger #1 (put)
        JSONObject json = composing.toJSON(); // Concern trigger #2, #3 and #4 (toJSON, size, entrySet)

        assertThat( json.length(), equalTo( 1 ) );
        assertThat( json.optString( "foo" ), equalTo( "bar" ) );

        assertThat( CONCERN_RECORDS.size(), equalTo( 4 ) );
    }

    private static class ServiceIdentitySpec
        implements Specification<ServiceReference<?>>
    {

        private final String identity;

        public ServiceIdentitySpec( String identity )
        {
            this.identity = identity;
        }

        @Override
        public boolean satisfiedBy( ServiceReference<?> item )
        {
            return item.identity().equals( identity );
        }

    }

}