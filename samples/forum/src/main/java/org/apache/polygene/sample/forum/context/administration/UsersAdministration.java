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
package org.apache.polygene.sample.forum.context.administration;

import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.library.rest.server.api.dci.Role;
import org.apache.polygene.sample.forum.data.entity.User;
import org.apache.polygene.sample.forum.data.entity.Users;

/**
 * TODO
 */
public class UsersAdministration
{
    @Structure
    Module module;

    UsersAdmin usersAdmin = new UsersAdmin();
    Administrator administrator = new Administrator();

    public UsersAdministration bind( @Uses Users users, @Uses User user )
    {
        usersAdmin.bind( users );
        administrator.bind( user );
        return this;
    }

    protected class UsersAdmin
        extends Role<Users>
    {
    }

    protected class Administrator
        extends Role<User>
    {
    }
}
