/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.usecase;

/**
 * Selection of CAP properties for a Usecase.
 *
 * See http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.20.1495 for an explanation
 * of CAP.
 *
 * If Consistent, then a Usecase has to always return correct data. This means that it may not rely
 * on cached data. If a fatal network failure occurs this error may not be hidden.
 *
 * If Available, then a Usecase has to always produce a result, even if dependent services are unavailable.
 *
 * If Partition tolerant, then a Usecase
 */
public enum CAP
{
    /**
     * In Consistent&Available cases, the Usecase has to always produce consistent responses, and a response
     * has to always be created to requests.
     * It is only allowed to use caches for response values if the value is checked to be up-to-date.
     *
     * If a dependent service is not available to produce a result, then it is required to wait for
     * that service to become available in order to produce a consistent response.
     *
     * This setting is for Usecases where it is important that correct information
     * is provided. Avoid depending on networked services in this setting, as they may not be available
     * due to partitioning.
     *
     * This is the default setting.
     */
    CA,

    /**
     * In Available&Partition Tolerant cases, the Usecase has to always produce a result, and may use
     * cached values if a dependent service is not available. Cached values do not have to be checked
     * if they are up-to-date.
     *
     * This setting is for Usecases where quick responses and avoidance of error reporting is important.
     * Typically only used for read-only Usecases.
     */
    AP,

    /**
     * In Consistent&Partition Tolerant cases, the Usecase must always produce consistent responses, but
     * must not always produce responses. In case of Partitioning, such that a dependent service is not available,
     * it is therefore allowd to throw an exception to indicate that the request could not be served.
     *
     * This setting is for Usecases where it is important that correct responses are created, but where
     * it is not a good idea to wait for unavailable services to become available again, since this might
     * take too long time.
     */
    CP
}
