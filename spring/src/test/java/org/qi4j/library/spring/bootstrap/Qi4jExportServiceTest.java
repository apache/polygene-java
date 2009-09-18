/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.spring.bootstrap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.qi4j.library.spring.bootstrap.Qi4jTestBootstrap.COMMENT_SERVICE_ID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public final class Qi4jExportServiceTest
{
    @Autowired
    private ApplicationContext appContext;

    @Test
    public final void testCommentService()
    {
        assertTrue( appContext.containsBean( COMMENT_SERVICE_ID ) );

        CommentService commentService = (CommentService) appContext.getBean( COMMENT_SERVICE_ID );
        assertNotNull( commentService );

        String beerComment = commentService.comment( "beer" );
        assertEquals( "BEER IS GOOD.", beerComment );

        String colaComment = commentService.comment( "cola" );
        assertEquals( "COLA IS GOOD.", colaComment );

        String colaBeerComment = commentService.comment( "cola+beer" );
        assertEquals( "COLA+BEER IS BAAAD.", colaBeerComment );

        CommentServiceHolder holder = (CommentServiceHolder) appContext.getBean( "commentServiceHolder" );
        assertTrue( commentService == holder.service() );
    }
}
