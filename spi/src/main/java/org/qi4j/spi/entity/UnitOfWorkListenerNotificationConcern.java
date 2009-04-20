/*
 * Copyright 2008 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.spi.entity;

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.unitofwork.UnitOfWorkEventListener;
import org.qi4j.spi.unitofwork.event.UnitOfWorkEvent;

/**
 * Notify all EntityStoreListeners that a change occurred in EntityState
 *
 * @author Alin Dreghiciu
 * @since March 18, 2008
 */
public abstract class UnitOfWorkListenerNotificationConcern
    extends ConcernOf<EntityStore>
    implements EntityStore
{
    @Service private Iterable<UnitOfWorkEventListener> listeners;

    public StateCommitter apply( String unitOfWorkIdentity, final Iterable<UnitOfWorkEvent> events, Usecase usecase, MetaInfo metaInfo ) throws EntityStoreException
    {
        final StateCommitter committer = next.apply( unitOfWorkIdentity, events, usecase, metaInfo );

        return new StateCommitter()
        {
            public void commit()
            {
                committer.commit();

                // Only do this if UnitOfWork completed successfully
                for( UnitOfWorkEventListener listener : listeners )
                {
                    listener.notifyEvents( events );
                }
            }

            public void cancel()
            {
                committer.cancel();
            }
        };
    }
}
