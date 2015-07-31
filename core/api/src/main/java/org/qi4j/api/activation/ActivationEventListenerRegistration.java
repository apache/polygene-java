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
package org.qi4j.api.activation;

/**
 * Use this to register listeners for ActivationEvents.
 *
 * This is implemented by Application, Layer, Module, for example.
 */
public interface ActivationEventListenerRegistration
{
    /**
     * @param listener will be notified when Activation events occur
     */
    void registerActivationEventListener( ActivationEventListener listener );

    /**
     * @param listener will not be notified when Activation events occur anymore
     */
    void deregisterActivationEventListener( ActivationEventListener listener );
}