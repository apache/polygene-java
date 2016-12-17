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
package org.apache.polygene.api.unitofwork;

import java.time.Instant;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.time.SystemTime;
import org.apache.polygene.api.usecase.Usecase;

/**
 * Factory for UnitOfWork.
 */
public interface UnitOfWorkFactory
{
    /**
     * Create a new UnitOfWork and associate it with the current thread.
     * <p>
     * The UnitOfWork will use the default Usecase settings.
     * </p>
     * <p>
     * Current time will be set to {@link SystemTime#now()}
     * </p>
     * @return a new UnitOfWork
     */
    UnitOfWork newUnitOfWork();

    /**
     * Create a new UnitOfWork and associate it with the current thread.
     * <p>
     * The UnitOfWork will use the default Usecase settings.
     * </p>
     *
     * @param currentTime the current time for this UnitOfWork
     *
     * @return a new UnitOfWork
     */
    UnitOfWork newUnitOfWork( Instant currentTime );

    /**
     * Create a new UnitOfWork for the given Usecase and associate it with the current thread.
     * <p>
     * Current time will be set to {@link SystemTime#now()}
     * </p>
     * @param usecase the Usecase for this UnitOfWork
     *
     * @return a new UnitOfWork
     */
    UnitOfWork newUnitOfWork( Usecase usecase );

    /**
     * Create a new UnitOfWork for the given Usecase and associate it with the current thread.
     *
     * @param usecase the Usecase for this UnitOfWork
     * @param currentTime the current time for this UnitOfWork
     *
     *
     * @return a new UnitOfWork
     */
    UnitOfWork newUnitOfWork( Usecase usecase, Instant currentTime );

    /**
     * @return true if there is an active UnitOfWork associated with the executing thread
     */
    boolean isUnitOfWorkActive();

    /**
     * Returns the UnitOfWork that is currently associated with the executing thread.
     *
     * @return The current UnitOfWork associated with the executing thread
     *
     * @throws IllegalStateException if no current UnitOfWork is active
     */
    UnitOfWork currentUnitOfWork()
        throws IllegalStateException;

    /**
     * Returns the UnitOfWork that the EntityComposite is bound to.
     *
     * @param entity the entity to be checked.
     *
     * @return The UnitOfWork instance that the Entity is bound to, or null if the entity is not associated with
     *         any UnitOfWork.
     */
    UnitOfWork getUnitOfWork( EntityComposite entity );
}
