/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
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
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.unitofwork.UnitOfWorkException;
import org.qi4j.index.sql.SQLIndexingEngineService;
import org.qi4j.library.sql.api.SQLIndexing;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.StateChangeListener;

/**
 *
 * @author Stanislav Muhametsin
 */
public abstract class SQLStateChangeListener implements StateChangeListener
{

   @This private SQLJDBCState _jdbcState;

   @Service private SQLIndexing _indexing;

   @Override
   public void notifyChanges(Iterable<EntityState> changedStates)
   {
      try
      {
         this._indexing.indexEntities(changedStates, this._jdbcState.connection().get());
      } catch (SQLException sqle)
      {
         Logger.getLogger(this.getClass().getName()).severe("Error when indexing entities:\n" + sqle);
         SQLException e = sqle;
         while (e != null)
         {
             e.printStackTrace( );
             e = e.getNextException( );
         }

         // TODO is UoWException right one for this?
         throw new UnitOfWorkException(sqle);
      }
   }
}
