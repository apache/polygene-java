/*
 * Copyright (c) 2011, Rickard Öberg.
 * Copyright (c) 2012, Niclas Hedhman.
 * Copyright (c) 2012, Paul Merlin.
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
 * Interface used by Structure elements and Services that can be activated and passivated.
 *
 * Application and Layer expose this interface so you can activate and passivate them.
 * Module and ServiceComposite activation/passivation is handled by the Qi4j runtime.
 */
public interface Activation
{

    /**
     * Called on activation.
     */
    void activate()
        throws ActivationException;

    /**
     * Called on passivation.
     */
    void passivate()
        throws PassivationException;
}
