/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.api.service;

/**
 * Instances that want to get callbacks on activation and passivation should implement this.
 */
public interface Activatable
{
    /**
     * This is invoked on the service when the instance is being activated
     *
     * @throws Exception if service could not be activated
     */
    void activate()
        throws Exception;

    /**
     * This is invoked on the service when the instance is being passivated
     *
     * @throws Exception if the service could not be passivated
     */
    void passivate()
        throws Exception;
}
