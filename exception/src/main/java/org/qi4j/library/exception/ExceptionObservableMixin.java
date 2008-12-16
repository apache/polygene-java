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

public class ExceptionObservableMixin
    implements ExceptionObservable
{
    @This private Property<ExceptionObserver> observer;
    
    public void addExceptionObserver( ExceptionObserver observer )
    {
        synchronized( this )
        {
            ExceptionObserver existing = this.observer.get();
            if( existing == null )
            {
                this.observer.set( observer );
                return;
            }
            this.observer.set( new DelegatingExceptionObserver( existing ) );
        }
    }

    public void removeExceptionObserver( ExceptionObserver observer )
    {
        synchronized( this )
        {
            ExceptionObserver current = this.observer.get();
            if( current == null )
            {
                return;
            }
            if( current == observer )
            {
                this.observer.set( null );
                return;
            }
            if( current instanceof DelegatingExceptionObserver )
            {
                DelegatingExceptionObserver delegator = (DelegatingExceptionObserver) this.observer;
                this.observer.set( delegator.removeExceptionObserver( observer ));
            }
        }
    }

}
