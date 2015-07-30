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
package org.apache.zest.sample.forum.context.account;

import org.apache.zest.api.constraint.Name;
import org.apache.zest.api.injection.scope.Uses;
import org.apache.zest.library.rest.server.api.ResourceIndex;
import org.apache.zest.library.rest.server.api.dci.Role;
import org.apache.zest.sample.forum.context.Context;
import org.apache.zest.sample.forum.data.entity.User;

/**
 * TODO
 */
public class UpdateProfile
    extends Context
    implements ResourceIndex<UpdateProfile.Profile>
{
    Profile profile;

    public UpdateProfile bind( @Uses User user )
    {
        profile = role( Profile.class, user );
        return this;
    }

    public Profile index()
    {
        return profile;
    }

    public void updateProfile( @Name( "name" ) String name,
                               @Name( "realname" ) String realName,
                               @Name( "email" ) String email
    )
    {
        profile.updateProfile( name, realName, email );
    }

    protected class Profile
        extends Role<User>
    {
        public void updateProfile( String name, String realName, String email )
        {
            self.name().set( name );
            self.realName().set( realName );
            self.email().set( email );
        }
    }
}
