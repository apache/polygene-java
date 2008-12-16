/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.library.exception;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.Property;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.mixin.Mixins;

@Mixins( ExceptionObservableMixin.class )
public class ExceptionHandlingNotificationConcern extends ConcernOf<ExceptionHandling>
    implements ExceptionHandling
{
    @This private Property<ExceptionObserver> observer;

    public void exceptionOccurred( String message, Object location, Throwable exception )
    {
        ExceptionObserver current;
        synchronized( this )
        {
            current = observer.get();
        }
        current.notify( message, location, exception );
        next.exceptionOccurred( message, location, exception );
    }
}
