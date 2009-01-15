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

import java.util.ArrayList;
import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.library.exception.ExceptionHandling;

class MulticastObserver<T extends Observation>
    implements Observer<T>, Observable<T>
{
    @Optional @Service private ExceptionHandling exceptionHandling;
    @This private Composite meAsComposite;

    private ArrayList<Observer<T>> observers;

    public MulticastObserver( Observer<T> existing )
    {
        observers = new ArrayList<Observer<T>>();
        observers.add( existing );
    }

    public void notify( T observation )
    {
        for( Observer<T> observer : observers )
        {
            try
            {
                observer.notify( observation );
            }
            catch( Exception e )
            {
                handleException( e, observer );
            }
        }
    }

    private void handleException( Exception exception, Observer<T> observer )
    {
        if( exceptionHandling != null )
        {
            exceptionHandling.exceptionOccurred( "Exception in observer: " + observer,  meAsComposite, exception);
        }
        else
        {
        }
    }

    public void addObserver( Observer<T> observer )
    {
        synchronized( this )
        {
            ArrayList<Observer<T>> clone = new ArrayList<Observer<T>>();
            clone.addAll( observers );
            clone.add( observer );
            observers = clone;
        }
    }

    public void removeObserver( Observer<T> observer )
    {
        synchronized( this )
        {
            ArrayList<Observer<T>> clone = new ArrayList<Observer<T>>();
            clone.addAll( observers );
            clone.remove( observer );
            observers = clone;
        }
    }

    boolean isOnlyOneLeft()
    {
        return observers.size() == 1;
    }

    Observer<T> getFirstOne()
    {
        return observers.get( 0 );
    }
}
