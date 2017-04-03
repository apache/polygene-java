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

import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.time.SystemTime;
import org.apache.polygene.spi.entity.EntityStatus;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class JSONManyAssociationStateTest
{

    @Test
    public void givenEmptyJSONManyAssociationStateWhenAddingTwoRefsAtZeroIndexExpectCorrectOrder()
    {
        // Fake JSONManyAssociationState
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add( JSONKeys.PROPERTIES, Json.createObjectBuilder().build() );
        builder.add( JSONKeys.ASSOCIATIONS, Json.createObjectBuilder().build() );
        builder.add( JSONKeys.MANY_ASSOCIATIONS, Json.createObjectBuilder().build() );
        builder.add( JSONKeys.NAMED_ASSOCIATIONS, Json.createObjectBuilder().build() );
        JsonObject state = builder.build();
        JSONEntityState entityState = new JSONEntityState( null,
                                                           null,
                                                           "0",
                                                           SystemTime.now(),
                                                           EntityReference.parseEntityReference( "123" ),
                                                           EntityStatus.NEW,
                                                           null,
                                                           state );
        JSONManyAssociationState jsonState = new JSONManyAssociationState( entityState, "under-test" );

        jsonState.add( 0, EntityReference.parseEntityReference( "first" ) );
        jsonState.add( 0, EntityReference.parseEntityReference( "second" ) );

        assertThat( jsonState.count(), equalTo( 2 ) );
    }

    @Test
    public void givenJSONManyAssociationStateWhenChangingReferencesExpectCorrectBehavior()
    {
        // Fake JSONManyAssociationState
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add( JSONKeys.PROPERTIES, Json.createObjectBuilder().build() );
        builder.add( JSONKeys.ASSOCIATIONS, Json.createObjectBuilder().build() );
        builder.add( JSONKeys.MANY_ASSOCIATIONS, Json.createObjectBuilder().build() );
        builder.add( JSONKeys.NAMED_ASSOCIATIONS, Json.createObjectBuilder().build() );
        JsonObject state = builder.build();
        JSONEntityState entityState = new JSONEntityState( null,
                                                           null,
                                                           "0",
                                                           SystemTime.now(),
                                                           EntityReference.parseEntityReference( "123" ),
                                                           EntityStatus.NEW,
                                                           null,
                                                           state );
        JSONManyAssociationState jsonState = new JSONManyAssociationState( entityState, "under-test" );

        assertThat( jsonState.contains( EntityReference.parseEntityReference( "NOT_PRESENT" ) ), is( false ) );

        jsonState.add( 0, EntityReference.parseEntityReference( "0" ) );
        jsonState.add( 1, EntityReference.parseEntityReference( "1" ) );
        jsonState.add( 2, EntityReference.parseEntityReference( "2" ) );

        assertThat( jsonState.contains( EntityReference.parseEntityReference( "1" ) ), is( true ) );

        assertThat( jsonState.get( 0 ).identity().toString(), equalTo( "0" ) );
        assertThat( jsonState.get( 1 ).identity().toString(), equalTo( "1" ) );
        assertThat( jsonState.get( 2 ).identity().toString(), equalTo( "2" ) );

        assertThat( jsonState.count(), equalTo( 3 ) );

        jsonState.remove( EntityReference.parseEntityReference( "1" ) );

        assertThat( jsonState.count(), equalTo( 2 ) );
        assertThat( jsonState.contains( EntityReference.parseEntityReference( "1" ) ), is( false ) );
        assertThat( jsonState.get( 0 ).identity().toString(), equalTo( "0" ) );
        assertThat( jsonState.get( 1 ).identity().toString(), equalTo( "2" ) );

        jsonState.add( 2, EntityReference.parseEntityReference( "1" ) );

        assertThat( jsonState.count(), equalTo( 3 ) );

        jsonState.add( 0, EntityReference.parseEntityReference( "A" ) );
        jsonState.add( 0, EntityReference.parseEntityReference( "B" ) );
        jsonState.add( 0, EntityReference.parseEntityReference( "C" ) );

        assertThat( jsonState.count(), equalTo( 6 ) );

        assertThat( jsonState.get( 0 ).identity().toString(), equalTo( "C" ) );
        assertThat( jsonState.get( 1 ).identity().toString(), equalTo( "B" ) );
        assertThat( jsonState.get( 2 ).identity().toString(), equalTo( "A" ) );

        assertThat( jsonState.contains( EntityReference.parseEntityReference( "C" ) ), is( true ) );
        assertThat( jsonState.contains( EntityReference.parseEntityReference( "B" ) ), is( true ) );
        assertThat( jsonState.contains( EntityReference.parseEntityReference( "A" ) ), is( true ) );
        assertThat( jsonState.contains( EntityReference.parseEntityReference( "0" ) ), is( true ) );
        assertThat( jsonState.contains( EntityReference.parseEntityReference( "2" ) ), is( true ) );
        assertThat( jsonState.contains( EntityReference.parseEntityReference( "1" ) ), is( true ) );

        List<String> refList = new ArrayList<>();
        for( EntityReference ref : jsonState )
        {
            refList.add( ref.identity().toString() );
        }
        assertThat( refList.isEmpty(), is( false ) );
        assertArrayEquals( new String[]
        {
            "C", "B", "A", "0", "2", "1"
        }, refList.toArray() );
    }
}
