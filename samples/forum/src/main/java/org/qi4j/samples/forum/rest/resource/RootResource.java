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
package org.qi4j.samples.forum.rest.resource;

import java.util.Collections;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.library.rest.server.api.ContextResource;
import org.qi4j.library.rest.server.api.SubResource;
import org.qi4j.library.rest.server.api.constraint.Requires;
import org.qi4j.samples.forum.data.entity.User;
import org.qi4j.samples.forum.data.entity.Users;
import org.qi4j.samples.forum.rest.resource.administration.AdministrationResource;
import org.qi4j.samples.forum.rest.resource.forum.ForumResource;
import org.qi4j.samples.forum.rest.resource.signup.SignupResource;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import static org.qi4j.library.rest.server.api.ObjectSelection.current;

/**
 * TODO
 */
public class RootResource
    extends ContextResource
{
    @SubResource
    public void signup()
    {
        select( Users.class, Users.USERS_ID );
        subResource( SignupResource.class );
    }

    @SubResource
    public void administration()
    {
        ChallengeResponse challenge = Request.getCurrent().getChallengeResponse();
        if( challenge == null )
        {
            Response.getCurrent()
                .setChallengeRequests( Collections.singletonList( new ChallengeRequest( ChallengeScheme.HTTP_BASIC, "Forum" ) ) );
            throw new ResourceException( Status.CLIENT_ERROR_UNAUTHORIZED );
        }

        User user = module.currentUnitOfWork().newQuery( module.newQueryBuilder( User.class ).where( QueryExpressions
                                                                                                         .eq( QueryExpressions
                                                                                                                  .templateFor( User.class )
                                                                                                                  .name(), challenge
                                                                                                             .getIdentifier() ) ) )
            .find();
        if( user == null || !user.isCorrectPassword( new String( challenge.getSecret() ) ) )
        {
            throw new ResourceException( Status.CLIENT_ERROR_UNAUTHORIZED );
        }

        current().select( user );

        subResource( AdministrationResource.class );
    }

    @SubResource
    @Requires( User.class )
    public void forum()
    {
        subResource( ForumResource.class );
    }
}
