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
package org.qi4j.samples.forum.context.administration;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.library.rest.server.api.ResourceIndex;
import org.qi4j.library.rest.server.api.dci.Role;
import org.qi4j.samples.forum.data.entity.Forum;
import org.qi4j.samples.forum.data.entity.Forums;
import org.qi4j.samples.forum.data.entity.User;

/**
 * TODO
 */
public class ForumsAdministration
    implements ResourceIndex<Query<Forum>>
{
    @Structure
    Module module;

    ForumsAdmin forumsAdmin = new ForumsAdmin();
    Administrator administrator = new Administrator();

    public ForumsAdministration bind( @Uses Forums forums, @Uses User user )
    {
        forumsAdmin.bind( forums );
        administrator.bind( user );
        return this;
    }

    public Query<Forum> index()
    {
        return forumsAdmin.forums();
    }

    public Forum createForum( @Name( "name" ) String name )
    {
        return forumsAdmin.createForum( name );
    }

    protected class ForumsAdmin
        extends Role<Forums>
    {
        public Query<Forum> forums()
        {
            return self.forums();
        }

        public Forum createForum( String name )
        {
            Forum forum = module.currentUnitOfWork().newEntity( Forum.class );
            forum.name().set( name );
            administrator.makeModerator( forum );
            return forum;
        }
    }

    protected class Administrator
        extends Role<User>
    {
        public void makeModerator( Forum forum )
        {
            forum.moderators().add( self );
        }
    }
}
