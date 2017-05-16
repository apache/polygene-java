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
package org.apache.polygene.runtime.mixin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.concern.Concerns;
import org.apache.polygene.api.concern.GenericConcern;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Assert that JDK classes are usable as Mixins.
 */
public class JDKMixinTest extends AbstractPolygeneTest
{
    @Concerns( JDKMixinConcern.class )
    public interface JSONSerializableMap extends Map<String, String>
    {
        JsonObject toJSON();
    }

    @SuppressWarnings( "serial" )
    public static class ExtendsJDKMixin extends HashMap<String, String>
        implements JSONSerializableMap
    {
        @Override
        public JsonObject toJSON()
        {
            System.out.println( ">>>> Call ExtendsJDKMixin.toJSON()" );
            JsonObjectBuilder builder = Json.createObjectBuilder();
            entrySet().forEach( entry -> builder.add( entry.getKey(), entry.getValue() ) );
            return builder.build();
        }
    }

    public static abstract class ComposeWithJDKMixin
        implements JSONSerializableMap
    {
        @This
        private Map<String, String> map;

        @Override
        public JsonObject toJSON()
        {
            System.out.println( ">>>> Call ComposeWithJDKMixin.toJSON()" );
            JsonObjectBuilder builder = Json.createObjectBuilder();
            map.entrySet().forEach( entry -> builder.add( entry.getKey(), entry.getValue() ) );
            return builder.build();
        }
    }

    public static class JDKMixinConcern extends GenericConcern
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

    private static final Identity EXTENDS_IDENTITY = new StringIdentity( ExtendsJDKMixin.class.getName() );
    private static final Identity COMPOSE_IDENTITY = new StringIdentity( ComposeWithJDKMixin.class.getName() );
    private static final Predicate<ServiceReference<?>> EXTENDS_IDENTITY_SPEC = new ServiceIdentitySpec(
        EXTENDS_IDENTITY );
    private static final Predicate<ServiceReference<?>> COMPOSE_IDENTITY_SPEC = new ServiceIdentitySpec(
        COMPOSE_IDENTITY );
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
        module.services( JSONSerializableMap.class )
              .identifiedBy( EXTENDS_IDENTITY.toString() )
              .withMixins( ExtendsJDKMixin.class )
              .instantiateOnStartup();

        module.layer().module( "compose" ).services( JSONSerializableMap.class )
              .visibleIn( Visibility.layer )
              .identifiedBy( COMPOSE_IDENTITY.toString() )
              .withMixins( HashMap.class, ComposeWithJDKMixin.class )
              .instantiateOnStartup();
    }

    @Test
    public void testMixinExtendsJDK()
    {
        List<ServiceReference<JSONSerializableMap>> services = serviceFinder.findServices( JSONSerializableMap.class )
                                                                            .filter( EXTENDS_IDENTITY_SPEC )
                                                                            .collect( Collectors.toList() );

        assertThat( services.size(), equalTo( 1 ) );
        assertThat( services.get( 0 ).identity(), equalTo( EXTENDS_IDENTITY ) );

        JSONSerializableMap extending = services.get( 0 ).get();
        extending.put( "foo", "bar" ); // Concern trigger #1 (put)
        JsonObject json = extending.toJSON(); // Concern trigger #2 and #3 (toJSON, entrySet)

        assertThat( json.size(), equalTo( 1 ) );
        assertThat( json.getString( "foo" ), equalTo( "bar" ) );

        assertThat( CONCERN_RECORDS.size(), equalTo( 3 ) );
    }

    @Test
    public void testComposeJDKMixin()
    {
        List<ServiceReference<JSONSerializableMap>> services = serviceFinder.findServices( JSONSerializableMap.class )
                                                                            .filter( COMPOSE_IDENTITY_SPEC )
                                                                            .collect( Collectors.toList() );

        assertThat( services.size(), equalTo( 1 ) );
        assertThat( services.get( 0 ).identity(), equalTo( COMPOSE_IDENTITY ) );

        JSONSerializableMap composing = services.get( 0 ).get();
        composing.put( "foo", "bar" ); // Concern trigger #1 (put)
        JsonObject json = composing.toJSON(); // Concern trigger #2 and #3 (toJSON, entrySet)

        assertThat( json.size(), equalTo( 1 ) );
        assertThat( json.getString( "foo" ), equalTo( "bar" ) );

        assertThat( CONCERN_RECORDS.size(), equalTo( 3 ) );
    }

    private static class ServiceIdentitySpec
        implements Predicate<ServiceReference<?>>
    {
        private final Identity identity;

        ServiceIdentitySpec( Identity identity )
        {
            this.identity = identity;
        }

        @Override
        public boolean test( ServiceReference<?> item )
        {
            return item.identity().equals( identity );
        }
    }
}