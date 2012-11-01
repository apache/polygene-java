/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.index.sql.internal;

import java.sql.SQLException;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.unitofwork.UnitOfWorkException;
import org.qi4j.index.sql.support.api.SQLIndexing;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.StateChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLStateChangeListener
        implements StateChangeListener
{

    private static final Logger LOGGER = LoggerFactory.getLogger( SQLStateChangeListener.class );

    @Service
    private SQLIndexing _indexing;

    @Override
    public void notifyChanges( Iterable<EntityState> changedStates )
    {
        try {

            this._indexing.indexEntities( changedStates );

        } catch ( SQLException sqle ) {

            SQLException lastException = sqle;
            while ( sqle.getNextException() != null ) {
                sqle = sqle.getNextException();
            }
            LOGGER.error( "Error when indexing entities", sqle );

            // TODO is UoWException right one for this?
            throw new UnitOfWorkException( lastException );

        }
    }

}
