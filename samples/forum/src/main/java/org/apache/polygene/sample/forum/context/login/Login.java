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
package org.apache.polygene.sample.forum.context.login;

import org.apache.polygene.api.constraint.Name;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.query.QueryBuilder;
import org.apache.polygene.api.query.QueryBuilderFactory;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.sample.forum.data.entity.User;

import static org.apache.polygene.api.query.QueryExpressions.*;

/**
 * TODO
 */
public class Login
{
    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    QueryBuilderFactory qbf;

    public void login( @Name( "name" ) String name, @Name( "password" ) String password )
    {
        QueryBuilder<User> builder = qbf.newQueryBuilder( User.class )
            .where( eq( templateFor( User.class ).name(), name ) );

        User user = uowf.currentUnitOfWork()
            .newQuery( builder )
            .find();

        if( user == null || !user.isCorrectPassword( password ) )
        {
            throw new IllegalArgumentException( "Login incorrect" );
        }
    }
}
