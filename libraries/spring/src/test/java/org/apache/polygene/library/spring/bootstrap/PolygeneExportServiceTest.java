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
package org.apache.polygene.library.spring.bootstrap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.apache.polygene.library.spring.bootstrap.PolygeneTestBootstrap.COMMENT_SERVICE_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

@ExtendWith( SpringExtension.class)
@ContextConfiguration
public final class PolygeneExportServiceTest
{
    @Autowired
    private ApplicationContext appContext;

    @Test
    public final void testCommentService()
    {
        assertThat( appContext.containsBean( COMMENT_SERVICE_ID ), is( true ) );

        CommentService commentService = (CommentService) appContext.getBean( COMMENT_SERVICE_ID );
        assertThat( commentService, notNullValue() );

        String beerComment = commentService.comment( "beer" );
        assertThat( beerComment, equalTo( "BEER IS GOOD." ) );

        String colaComment = commentService.comment( "cola" );
        assertThat( colaComment, equalTo( "COLA IS GOOD." ) );

        String colaBeerComment = commentService.comment( "cola+beer" );
        assertThat( colaBeerComment, equalTo( "COLA+BEER IS BAAAD." ) );

        CommentServiceHolder holder = (CommentServiceHolder) appContext.getBean( "commentServiceHolder" );
        assertThat( commentService == holder.service(), is( true ) );
    }
}
