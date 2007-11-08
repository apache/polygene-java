/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.library.framework.service;

import org.qi4j.annotation.scope.ConcernFor;
import org.qi4j.annotation.scope.ThisCompositeAs;
import org.qi4j.service.Activatable;
import org.qi4j.service.ActivationStatus;
import org.qi4j.service.MutableServiceStatus;

/**
 * When the Activatable interface is invoked, set the
 */
public class ActivatableServiceStatusConcern
    implements Activatable
{
    @ThisCompositeAs MutableServiceStatus status;
    @ConcernFor Activatable next;

    public void activate() throws Exception
    {
        status.setActivationStatus( ActivationStatus.STARTING );
        try
        {
            next.activate();
            status.setActivationStatus( ActivationStatus.ACTIVE );
        }
        catch( Exception e )
        {
            status.setActivationStatus( ActivationStatus.INACTIVE );
        }
    }

    public void passivate() throws Exception
    {
        status.setActivationStatus( ActivationStatus.STOPPING );
        try
        {
            next.passivate();
        }
        finally
        {
            status.setActivationStatus( ActivationStatus.INACTIVE );
        }
    }
}
