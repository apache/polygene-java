/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.library.observations;

import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.library.exception.ExceptionHandling;

public class ObservableMixin<T extends Observation>
    implements Observable<T>, Observer<T>
{
    @Optional @Service private ExceptionHandling exceptionHandling;
    @This private Composite meAsComposite;

    private Observer<T> observer;

    public void addObserver( Observer<T> observer )
    {
        synchronized( this )
        {
            if( this.observer == null )
            {
                this.observer = observer;
                return;
            }
            if( this.observer instanceof MulticastObserver )
            {
                ( (MulticastObserver<T>) this.observer ).addObserver( observer );
                return;
            }
            Observer<T> existing = this.observer;
            this.observer = new MulticastObserver<T>( existing );
        }
    }

    public void removeObserver( Observer<T> observer )
    {
        synchronized( this )
        {
            if( this.observer == null )
            {
                return;
            }
            if( this.observer instanceof MulticastObserver )
            {
                MulticastObserver<T> multicastObserver = (MulticastObserver<T>) this.observer;
                multicastObserver.removeObserver( observer );
                if( multicastObserver.isOnlyOneLeft() )
                {
                    this.observer = multicastObserver.getFirstOne();
                }
                return;
            }
            if( this.observer.equals( observer ) )
            {
                this.observer = null;
            }
        }
    }

    public void notify( T observation )
    {
        if( observer == null )
        {
            return;
        }
        try
        {
            observer.notify( observation );
        }
        catch( Exception e )
        {
            if( exceptionHandling != null )
            {
                exceptionHandling.exceptionOccurred( "Exception in observer: " + observer, meAsComposite, e );
            }
            else
            {
            }
        }
    }
}
