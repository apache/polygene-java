/*
 * Copyright (c) 2011, Rickard Öberg.
 * Copyright (c) 2012, Niclas Hedhman.
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
package org.qi4j.runtime.activation;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.activation.ActivationEvent;
import org.qi4j.api.activation.ActivationEventListener;
import org.qi4j.api.activation.ActivationEventListenerRegistration;

/**
 * Internal helper for managing registrations and firing events
 */
/* package */ class ActivationEventListenerSupport
    implements ActivationEventListenerRegistration, ActivationEventListener
{
    protected List<ActivationEventListener> listeners = new ArrayList<>();

    @Override
    public void registerActivationEventListener( ActivationEventListener listener )
    {
        List<ActivationEventListener> newListeners = new ArrayList<>();
        newListeners.addAll( listeners );
        newListeners.add( listener );
        listeners = newListeners;
    }

    @Override
    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        List<ActivationEventListener> newListeners = new ArrayList<>();
        newListeners.addAll( listeners );
        newListeners.remove( listener );
        listeners = newListeners;
    }

    /* package */ void fireEvent( ActivationEvent event )
        throws Exception
    {
        for( ActivationEventListener listener : listeners )
        {
            listener.onEvent( event );
        }
    }

    @Override
    public void onEvent( ActivationEvent event )
        throws Exception
    {
        fireEvent( event );
    }
}
