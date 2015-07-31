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
package org.qi4j.samples.forum.data.entity;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.samples.forum.context.Events;
import org.qi4j.samples.forum.context.signup.Registration;

import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * TODO
 */
@Mixins( Users.Mixin.class )
public interface Users
    extends EntityComposite, Events
{
    String USERS_ID = "users";

    public Query<User> users();

    abstract class Mixin
        implements Users
    {
        @Structure
        Module module;

        public Query<User> users()
        {
            return module.currentUnitOfWork()
                .newQuery( module.newQueryBuilder( User.class ) )
                .orderBy( templateFor( User.class ).realName() );
        }

        @Override
        public void signedup( Registration registration )
        {
            EntityBuilder<User> builder = module.currentUnitOfWork().newEntityBuilder( User.class );
            builder.instance().name().set( registration.name().get() );
            builder.instance().realName().set( registration.realName().get() );
            builder.instance().email().set( registration.email().get() );
            builder.instance().password().set( builder.instance().hashPassword( registration.password().get() ) );

            builder.newInstance();
        }
    }
}
