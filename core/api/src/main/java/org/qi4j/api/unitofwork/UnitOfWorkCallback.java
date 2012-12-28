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

package org.qi4j.api.unitofwork;

/**
 * Callback interface for UnitOfWork completion or discard. Implementations
 * of this interface can be registered through {@link UnitOfWork#addUnitOfWorkCallback(UnitOfWorkCallback)}.
 *
 * If Entities implement this interface they will also receive invocations of this callback interface.
 */
public interface UnitOfWorkCallback
{
    /**
     * This is called before the completion of the UnitOfWork.
     * The callback may do any validation checks and throw
     * UnitOfWorkCompletionException if there is any reason
     * why the UnitOfWork is not in a valid state to be completed.
     *
     * @throws UnitOfWorkCompletionException
     */
    void beforeCompletion()
        throws UnitOfWorkCompletionException;

    /**
     * This is called after the completion or discarding
     * of the UnitOfWork. The callback may do any cleanup
     * necessary related to the UnitOfWork. Note that the
     * UnitOfWork is no longer active when this method is
     * called, so no methods on it may be invoked.
     *
     * @param status
     */
    void afterCompletion( UnitOfWorkStatus status );

    enum UnitOfWorkStatus
    {
        COMPLETED, DISCARDED
    }
}
