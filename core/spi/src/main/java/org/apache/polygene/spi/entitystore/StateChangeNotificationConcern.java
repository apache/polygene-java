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

package org.apache.polygene.spi.entitystore;

import org.apache.polygene.api.concern.ConcernOf;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.spi.entity.EntityState;

/**
 * State change notification Concern.
 */
public abstract class StateChangeNotificationConcern
    extends ConcernOf<EntityStoreSPI>
    implements EntityStoreSPI
{
    @Service
    Iterable<StateChangeListener> listeners;

    @Override
    public StateCommitter applyChanges( final EntityStoreUnitOfWork unitofwork,
                                        final Iterable<EntityState> state
    )
    {
        final StateCommitter committer = next.applyChanges( unitofwork, state );
        return new StateCommitter()
        {
            @Override
            public void commit()
            {
                for( StateChangeListener listener : listeners )
                {
                    listener.notifyChanges( state );
                }
                committer.commit();
            }

            @Override
            public void cancel()
            {
                committer.cancel();
            }
        };
    }
}
