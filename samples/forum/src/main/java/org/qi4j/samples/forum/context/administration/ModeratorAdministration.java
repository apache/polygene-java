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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.library.rest.server.api.ResourceDelete;
import org.qi4j.library.rest.server.api.ResourceIndex;
import org.qi4j.library.rest.server.api.dci.Role;
import org.qi4j.samples.forum.data.Moderators;
import org.qi4j.samples.forum.data.entity.User;

/**
 * TODO
 */
public class ModeratorAdministration
    implements ResourceIndex<User>, ResourceDelete
{
    @Structure
    Module module;

    ModeratorAdmin moderatorAdmin = new ModeratorAdmin();
    Moderator moderator = new Moderator();

    public ModeratorAdministration bind( @Uses Moderators moderators, @Uses User user )
    {
        moderatorAdmin.bind( moderators );
        moderator.bind( user );
        return this;
    }

    public User index()
    {
        return moderator.self();
    }

    public void delete()
    {
        moderatorAdmin.removeModerator();
    }

    protected class Moderator
        extends Role<User>
    {
    }

    protected class ModeratorAdmin
        extends Role<Moderators>
    {
        @Structure
        Module module;

        public void removeModerator()
        {
            self().moderators().remove( moderator.self() );
        }
    }
}
