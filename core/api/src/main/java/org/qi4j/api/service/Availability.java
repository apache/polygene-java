/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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
 * Services can implement this interface in order to allow Qi4j to ask
 * it whether it is currently available for use or not. This is accessed
 * by clients through the ServiceReference of the service. Services that do not
 * implement this are always considered to be available.
 */
public interface Availability
{
    /**
     * Implementations should return true if the underlying service is currently available for use.
     *
     * Reasons why a service might not be available is either if it has been configured not to be (see
     * the Enabled interface), or if an underlying resource is currently unavailable.
     *
     * @return true if the service is available, false otherwise.
     */
    boolean isAvailable();
}
