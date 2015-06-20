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
package org.qi4j.samples.forum.context.login;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.structure.Module;
import org.qi4j.samples.forum.data.entity.User;

/**
 * TODO
 */
public class Login
{
    @Structure
    Module module;

    public void login( @Name( "name" ) String name, @Name( "password" ) String password )
    {
        User user = module.currentUnitOfWork()
            .newQuery( module.newQueryBuilder( User.class )
                           .where( QueryExpressions.eq( QueryExpressions.templateFor( User.class ).name(), name ) ) )
            .find();

        if( user == null || !user.isCorrectPassword( password ) )
        {
            throw new IllegalArgumentException( "Login incorrect" );
        }
    }
}
