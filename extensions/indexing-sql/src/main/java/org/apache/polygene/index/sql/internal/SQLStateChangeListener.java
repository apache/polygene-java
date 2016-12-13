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
package org.apache.polygene.index.sql.internal;

import java.sql.SQLException;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.unitofwork.UnitOfWorkException;
import org.apache.polygene.index.sql.support.api.SQLIndexing;
import org.apache.polygene.library.sql.common.SQLUtil;
import org.apache.polygene.spi.entity.EntityState;
import org.apache.polygene.spi.entitystore.StateChangeListener;

public class SQLStateChangeListener
    implements StateChangeListener
{
    @Service
    private SQLIndexing _indexing;

    @Override
    public void notifyChanges( Iterable<EntityState> changedStates )
    {
        try
        {
            this._indexing.indexEntities( changedStates );
        }
        catch( SQLException sqle )
        {
            // TODO is UoWException right one for this?
            throw new UnitOfWorkException( SQLUtil.withAllSQLExceptions( sqle ) );
        }
    }
}
