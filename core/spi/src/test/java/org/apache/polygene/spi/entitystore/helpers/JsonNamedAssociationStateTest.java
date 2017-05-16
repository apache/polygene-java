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
package org.apache.polygene.spi.entitystore.helpers;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.time.SystemTime;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.serialization.javaxjson.JavaxJsonFactories;
import org.apache.polygene.spi.entity.EntityStatus;
import org.apache.polygene.spi.serialization.JsonSerialization;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JsonNamedAssociationStateTest extends AbstractPolygeneTest
{
    @Override
    public void assemble( ModuleAssembly module )
    {
    }

    @Service
    private JsonSerialization serialization;

    @Service
    private JavaxJsonFactories jsonFactories;

    @Test
    public void givenJsonNamedAssociationStateWhenChangingReferencesExpectCorrectBehavior()
    {
        // Fake JsonNamedAssociationState
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add( JSONKeys.VALUE, Json.createObjectBuilder().build() );
        JsonObject state = builder.build();
        JSONEntityState entityState = new JSONEntityState( module,
                                                           serialization,
                                                           jsonFactories,
                                                           "0",
                                                           SystemTime.now(),
                                                           EntityReference.parseEntityReference( "123" ),
                                                           EntityStatus.NEW,
                                                           null,
                                                           state );
        JSONNamedAssociationState jsonState = new JSONNamedAssociationState( jsonFactories, entityState, "under-test" );


        assertThat( jsonState.containsName( "foo" ), is( false ) );

        jsonState.put( "foo", EntityReference.parseEntityReference( "0" ) );
        jsonState.put( "bar", EntityReference.parseEntityReference( "1" ) );
        jsonState.put( "bazar", EntityReference.parseEntityReference( "2" ) );

        assertThat( jsonState.containsName( "bar" ), is( true ) );

        assertThat( jsonState.get( "foo" ).identity().toString(), equalTo( "0" ) );
        assertThat( jsonState.get( "bar" ).identity().toString(), equalTo( "1" ) );
        assertThat( jsonState.get( "bazar" ).identity().toString(), equalTo( "2" ) );

        assertThat( jsonState.count(), equalTo( 3 ) );

        jsonState.remove( "bar" );

        assertThat( jsonState.count(), equalTo( 2 ) );
        assertThat( jsonState.containsName( "bar" ), is( false ) );
        assertThat( jsonState.get( "foo" ).identity().toString(), equalTo( "0" ) );
        assertThat( jsonState.get( "bazar" ).identity().toString(), equalTo( "2" ) );

        jsonState.put( "bar", EntityReference.parseEntityReference( "1" ) );

        assertThat( jsonState.count(), equalTo( 3 ) );

        jsonState.put( "oof", EntityReference.parseEntityReference( "A" ) );
        jsonState.put( "rab", EntityReference.parseEntityReference( "B" ) );
        jsonState.put( "razab", EntityReference.parseEntityReference( "C" ) );

        assertThat( jsonState.count(), equalTo( 6 ) );

        assertThat( jsonState.get( "razab" ).identity().toString(), equalTo( "C" ) );
        assertThat( jsonState.get( "rab" ).identity().toString(), equalTo( "B" ) );
        assertThat( jsonState.get( "oof" ).identity().toString(), equalTo( "A" ) );

        Map<String, String> refMap = new LinkedHashMap<>();
        for( String name : jsonState )
        {
            refMap.put( name, jsonState.get( name ).identity().toString() );
        }
        assertThat( refMap.isEmpty(), is( false ) );
        assertThat( refMap.keySet(), hasItems( "foo", "bar", "bazar", "oof", "rab", "razab" ) );
        assertThat( refMap.values(), hasItems( "0", "1", "2", "A", "B", "C" ) );
    }
}
