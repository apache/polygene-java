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
package org.apache.polygene.sample.forum.data.entity;

import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.query.Query;
import org.apache.polygene.api.query.QueryBuilderFactory;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.sample.forum.context.Events;
import org.apache.polygene.sample.forum.context.signup.Registration;

import static org.apache.polygene.api.query.QueryExpressions.eq;
import static org.apache.polygene.api.query.QueryExpressions.templateFor;

/**
 * TODO
 */
@Mixins( Users.Mixin.class )
public interface Users
    extends EntityComposite, Events
{
    Identity USERS_ID = StringIdentity.identity( "users" );

    Query<User> users();

    User userNamed( String name );

    abstract class Mixin
        implements Users
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        QueryBuilderFactory qbf;

        @Override
        public Query<User> users()
        {
            return uowf.currentUnitOfWork()
                .newQuery( qbf.newQueryBuilder( User.class ) )
                .orderBy( templateFor( User.class ).realName() );
        }

        @Override
        public User userNamed( String name )
        {
            return uowf.currentUnitOfWork().newQuery(
                qbf.newQueryBuilder( User.class ).where( eq( templateFor( User.class ).name(), name ) )
            ).find();
        }

        @Override
        public void signedup( Registration registration )
        {
            EntityBuilder<User> builder = uowf.currentUnitOfWork().newEntityBuilder( User.class );
            builder.instance().name().set( registration.name().get() );
            builder.instance().realName().set( registration.realName().get() );
            builder.instance().email().set( registration.email().get() );
            builder.instance().password().set( builder.instance().hashPassword( registration.password().get() ) );

            builder.newInstance();
        }
    }
}
