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

import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.query.Query;
import org.apache.polygene.api.query.QueryBuilderFactory;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.sample.forum.data.Administrators;

import static org.apache.polygene.api.query.QueryExpressions.templateFor;

/**
 * TODO
 */
@Mixins( Forums.Mixin.class )
public interface Forums
    extends Administrators, EntityComposite
{
    Identity FORUMS_ID = StringIdentity.identity( "forums" );

    Query<Forum> forums();

    abstract class Mixin
        implements Forums
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        QueryBuilderFactory qbf;

        public Query<Forum> forums()
        {
            return uowf.currentUnitOfWork()
                .newQuery( qbf.newQueryBuilder( Forum.class ) )
                .orderBy( templateFor( Forum.class ).name() );
        }
    }
}
