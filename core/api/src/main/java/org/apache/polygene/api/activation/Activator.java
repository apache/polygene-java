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
package org.apache.polygene.api.activation;

/**
 * Assemble Activators to hook Services Activation.
 *
 * @param <ActivateeType> Type of the activatee.
 *
 * @see ActivatorAdapter
 * @see org.apache.polygene.api.service.ServiceActivation
 */
public interface Activator<ActivateeType>
{

    /**
     * Called before activatee activation.
     *
     * @param activating The instance that is about to be activated.
     *
     * @throws Exception Allowed to throw Exception which will be wrapped in an ActivationException
     */
    void beforeActivation( ActivateeType activating )
        throws Exception;

    /**
     * Called after activatee activation.
     *
     * @param activated The instance that has just been activated.
     *
     * @throws Exception Allowed to throw Exception which will be wrapped in an ActivationException
     */
    void afterActivation( ActivateeType activated )
        throws Exception;

    /**
     * Called before activatee passivation.
     *
     * @param passivating The instance that is about to be passivated.
     *
     * @throws Exception Allowed to throw Exception which will be wrapped in an PassivationException
     */
    void beforePassivation( ActivateeType passivating )
        throws Exception;

    /**
     * Called after activatee passivation.
     *
     * @param passivated The instance that has just been passivated.
     *
     * @throws Exception Allowed to throw Exception which will be wrapped in an PassivationException
     */
    void afterPassivation( ActivateeType passivated )
        throws Exception;
}
