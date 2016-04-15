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
package org.apache.zest.sample.forum.context.administration;

import org.apache.zest.api.constraint.Name;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.Uses;
import org.apache.zest.api.structure.Module;
import org.apache.zest.library.rest.server.api.ResourceIndex;
import org.apache.zest.library.rest.server.api.dci.Role;
import org.apache.zest.sample.forum.data.entity.Board;
import org.apache.zest.sample.forum.data.entity.Forum;
import org.apache.zest.sample.forum.data.entity.User;

/**
 * TODO
 */
public class BoardAdministration
    implements ResourceIndex<Board>
{
    @Structure
    Module module;

    BoardAdmin boardAdmin = new BoardAdmin();

    public BoardAdministration bind( @Uses Forum forum, @Uses Board board, @Uses User user )
    {
        boardAdmin.bind( board );
        return this;
    }

    public Board index()
    {
        return boardAdmin.self();
    }

    public void updateName( @Name( "name" ) String name )
    {
        boardAdmin.updateName( name );
    }

    protected class BoardAdmin
        extends Role<Board>
    {
        @Structure
        Module module;

        public void updateName( String name )
        {
            self().name().set( name );
        }
    }
}
