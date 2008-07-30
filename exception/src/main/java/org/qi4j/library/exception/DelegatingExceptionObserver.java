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

import java.util.Iterator;
import java.util.LinkedList;

public class DelegatingExceptionObserver
    implements ExceptionObserver
{
    private LinkedList<ExceptionObserver> observers;

    public DelegatingExceptionObserver( ExceptionObserver existing )
    {
        observers = new LinkedList<ExceptionObserver>();
        observers.add( existing );
    }

    public void notify( String message, Object source, Throwable exception )
    {
        Iterator<ExceptionObserver> iterator;
        synchronized( this )
        {
            iterator = observers.iterator();
        }
        while( iterator.hasNext() )
        {
            ExceptionObserver observer = iterator.next();
            try
            {
                observer.notify( message, source, exception );
            }
            catch( Throwable e )
            {
                System.err.println( "WARNING: ExceptionObserver " + observer + " threw an exception. See below." );
                e.printStackTrace( System.err );
            }
        }
    }

    void addExceptionObserver( ExceptionObserver observer )
    {
        synchronized( this )
        {
            LinkedList<ExceptionObserver> clone = new LinkedList<ExceptionObserver>();
            clone.addAll( observers );
            clone.add( observer );
            observers = clone;
        }
    }

    ExceptionObserver removeExceptionObserver( ExceptionObserver observer )
    {
        synchronized( this )
        {
            LinkedList<ExceptionObserver> clone = new LinkedList<ExceptionObserver>();
            clone.addAll( observers );
            clone.remove( observer );
            if( observers.size() == 1 )
            {
                ExceptionObserver last = observers.removeFirst();
                observers = null;
                return last;
            }
            observers = clone;
            return this;
        }
    }

}
