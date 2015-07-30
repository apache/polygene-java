/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.sample.forum.context.view;

import java.util.Date;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.Uses;
import org.apache.zest.api.property.Numbers;
import org.apache.zest.api.structure.Module;
import org.apache.zest.library.rest.server.api.ResourceIndex;
import org.apache.zest.library.rest.server.api.dci.Role;
import org.apache.zest.sample.forum.data.entity.Post;
import org.apache.zest.sample.forum.data.entity.Topic;
import org.apache.zest.sample.forum.data.entity.User;

/**
 * TODO
 */
public class ViewPost
    implements ResourceIndex<Post>
{
    PostView viewPost = new PostView();
    ReplyTopic replyTopic = new ReplyTopic();
    Poster poster = new Poster();

    public ViewPost bind( @Uses Topic topic, @Uses Post post, @Uses User user )
    {
        viewPost.bind( post );
        replyTopic.bind( topic );
        poster.bind( user );
        return this;
    }

    @Override
    public Post index()
    {
        return viewPost.self();
    }

    public Post reply( String message )
    {
        return replyTopic.reply( message, viewPost );
    }

    protected class ReplyTopic
        extends Role<Topic>
    {
        @Structure
        Module module;

        public Post reply( String message, PostView viewPost )
        {
            Post post = module.currentUnitOfWork().newEntity( Post.class );
            post.message().set( message );
            post.createdBy().set( poster.self() );
            post.createdOn().set( new Date( module.currentUnitOfWork().currentTime() ) );
            post.replyTo().set( viewPost.self() );

            self().lastPost().set( post );
            Numbers.add( self().postCount(), 1 );

            return post;
        }
    }

    protected class PostView
        extends Role<Post>
    {
    }

    protected class Poster
        extends Role<User>
    {
    }
}
