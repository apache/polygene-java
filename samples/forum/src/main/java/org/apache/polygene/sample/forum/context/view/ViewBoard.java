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
package org.apache.polygene.sample.forum.context.view;

import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.library.rest.server.api.ResourceIndex;
import org.apache.polygene.library.rest.server.api.dci.Role;
import org.apache.polygene.sample.forum.data.Moderators;
import org.apache.polygene.sample.forum.data.entity.Board;
import org.apache.polygene.sample.forum.data.entity.Forum;
import org.apache.polygene.sample.forum.data.entity.Post;
import org.apache.polygene.sample.forum.data.entity.Topic;
import org.apache.polygene.sample.forum.data.entity.User;

import static org.apache.polygene.api.property.Numbers.add;

/**
 * TODO
 */
public class ViewBoard
    implements ResourceIndex<Board>
{
    private Posting posting;
    private BoardViewer viewer;
    private BoardModeration boardModeration;
    private ForumModeration forumModeration;

    public ViewBoard bind( @Uses Forum forum, @Uses Board board, @Uses User user )
    {
        posting.bind( board );
        viewer.bind( user );
        boardModeration.bind( board );
        forumModeration.bind( forum );
        return this;
    }

    @Override
    public Board index()
    {
        return posting.self();
    }

    public Topic createTopic( String subject, String message )
    {
        return posting.createTopic( subject, message, viewer );
    }

    protected class BoardViewer
        extends Role<User>
    {
        boolean isModerator( Moderators moderators )
        {
            return moderators.moderators().contains( self );
        }
    }

    protected class BoardModeration
        extends Role<Moderators>
    {
        boolean isModerator( BoardViewer user )
        {
            return user.isModerator( self ) || forumModeration.isModerator( user );
        }
    }

    protected class ForumModeration
        extends Role<Moderators>
    {
        boolean isModerator( BoardViewer user )
        {
            return user.isModerator( self );
        }
    }

    protected class Posting
        extends Role<Board>
    {
        @Structure
        UnitOfWorkFactory uowf;

        Topic createTopic( String subject, String message, BoardViewer poster )
        {
            Topic topic = uowf.currentUnitOfWork().newEntity( Topic.class );

            topic.subject().set( subject );

            Post post = uowf.currentUnitOfWork().newEntity( Post.class );
            post.message().set( message );

            // Moderation checks
            if( boardModeration.isModerator( poster ) )
            {
                post.status().set( Post.Status.POSTED );

                self.lastPost().set( post );
                add( self.topicCount(), 1 );
                add( self.postCount(), 1 );

                return topic;
            }
            else
            {
                post.status().set( Post.Status.MODERATION );
            }

            return topic;
        }
    }
}
