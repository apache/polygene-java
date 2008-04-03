/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008, Edward Yakop. All Rights Reserved.
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

package org.qi4j.runtime.structure;

import static org.qi4j.composite.NullArgumentException.*;
import org.qi4j.service.Activatable;

/**
 * {@code ActivationStatusChange} represents which activatable that has its status changed.
 *
 * @author Rickard Öberg
 * @since 0.1.0
 */
public final class ActivationStatusChange
{
    private Activatable activatable;
    private ActivationStatus newStatus;

    /**
     * Construct an instance of {@code ActivationStatusChange}.
     *
     * @param anActivatable The activatable that status changed. This argument must not be {@code null}.
     * @param aNewStatus    The new status of activatable. This argument must not be {@code null}.
     * @throws IllegalArgumentException Thrown if one or both arguments are {@code null}.
     * @since 0.1.0
     */
    public ActivationStatusChange( Activatable anActivatable, ActivationStatus aNewStatus )
        throws IllegalArgumentException
    {
        validateNotNull( "anActivatable", anActivatable );
        validateNotNull( "aNewStatus", aNewStatus );

        activatable = anActivatable;
        newStatus = aNewStatus;
    }

    /**
     * Returns the activatable that has its status changed. Must not return {@code null}.
     *
     * @return The activatable that has its status changed.
     * @since 0.1.0
     */
    public final Activatable getActivatable()
    {
        return activatable;
    }

    /**
     * Returns the new status of the activatable. Must not return {@code null}.
     *
     * @return The new status of the activatable.
     * @since 0.1.0
     */
    public final ActivationStatus getNewStatus()
    {
        return newStatus;
    }
}
