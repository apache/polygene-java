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
 * <p>Application and Layer expose this interface so you can activate and passivate them.</p>
 * <p>Module and ServiceComposite activation/passivation is handled by the Qi4j runtime.</p>
 */
public interface Activation
{
    /**
     * Activate.
     * <p>Fail fast execution order is:</p>
     * <ul>
     *   <li>Fire {@link ActivationEvent.EventType#ACTIVATING}</li>
     *   <li>Call {@link Activator#beforeActivation(java.lang.Object)} on each Activator</li>
     *   <li>Call {@link #activate()} children</li>
     *   <li>Call {@link Activator#afterActivation(java.lang.Object)} on each Activator</li>
     *   <li>Fire {@link ActivationEvent.EventType#ACTIVATED}</li>
     * </ul>
     * <p>If an Exception is thrown, already activated nodes are passivated.</p>
     * @throws ActivationException with first Exception of activation plus the PassivationException if any
     */
    void activate()
        throws ActivationException;

    /**
     * Passivate.
     * <p>Fail safe execution order is:</p>
     * <ul>
     *   <li>Fire {@link ActivationEvent.EventType#PASSIVATING}</li>
     *   <li>Call {@link Activator#beforePassivation(java.lang.Object)} on each Activator</li>
     *   <li>Call {@link #passivate()} children</li>
     *   <li>Call {@link Activator#afterPassivation(java.lang.Object)} on each Activator</li>
     *   <li>Fire {@link ActivationEvent.EventType#PASSIVATED}</li>
     * </ul>
     * @throws PassivationException after passivation with all Exceptions of passivation if any
     */
    void passivate()
        throws PassivationException;
}
