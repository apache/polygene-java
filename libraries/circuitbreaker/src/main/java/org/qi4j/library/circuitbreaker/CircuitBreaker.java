/**
 *
 * Copyright 2009-2010 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.circuitbreaker;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.Date;
import org.qi4j.functional.Specification;
import org.qi4j.functional.Specifications;

import static org.qi4j.functional.Specifications.not;

/**
 * Implementation of CircuitBreaker pattern
 */
public class CircuitBreaker
{
    public enum Status
    {
        off,
        on
    }

    private int threshold;
    private long timeout;
    private Specification<Throwable> allowedThrowables;

    private int countDown;
    private long trippedOn = -1;
    private long enableOn = -1;

    private Status status = Status.on;

    private Throwable lastThrowable;

    PropertyChangeSupport pcs = new PropertyChangeSupport( this );
    VetoableChangeSupport vcs = new VetoableChangeSupport( this );

    public CircuitBreaker( int threshold, long timeout, Specification<Throwable> allowedThrowables )
    {
        this.threshold = threshold;
        this.countDown = threshold;
        this.timeout = timeout;
        this.allowedThrowables = allowedThrowables;
    }

    public CircuitBreaker( int threshold, long timeout )
    {
        this( threshold, timeout, not( Specifications.<Throwable>TRUE() ) ); // Trip on all exceptions as default
    }

    public CircuitBreaker()
    {
        this( 1, 1000 * 60 * 5 ); // 5 minute timeout as default
    }

    public synchronized void trip()
    {
        if( status == Status.on )
        {
            if( countDown != 0 )
            {
                // If this was invoked manually, then set countDown to zero automatically
                int oldCountDown = countDown;
                countDown = 0;
                pcs.firePropertyChange( "serviceLevel", ( oldCountDown ) / ( (double) threshold ), countDown / ( (double) threshold ) );
            }

            status = Status.off;
            pcs.firePropertyChange( "status", Status.on, Status.off );

            trippedOn = System.currentTimeMillis();
            enableOn = trippedOn + timeout;
        }
    }

    public synchronized void turnOn()
        throws PropertyVetoException
    {
        if( status == Status.off )
        {
            try
            {
                vcs.fireVetoableChange( "status", Status.off, Status.on );
                status = Status.on;
                countDown = threshold;
                trippedOn = -1;
                enableOn = -1;
                lastThrowable = null;

                pcs.firePropertyChange( "status", Status.off, Status.on );
            }
            catch( PropertyVetoException e )
            {
                // Reset timeout
                enableOn = System.currentTimeMillis() + timeout;

                if( e.getCause() != null )
                {
                    lastThrowable = e.getCause();
                }
                else
                {
                    lastThrowable = e;
                }
                throw e;
            }
        }
    }

    public int threshold()
    {
        return threshold;
    }

    public synchronized Throwable lastThrowable()
    {
        return lastThrowable;
    }

    public synchronized double serviceLevel()
    {
        return countDown / ( (double) threshold );
    }

    public synchronized Status status()
    {
        if( status == Status.off )
        {
            if( System.currentTimeMillis() > enableOn )
            {
                try
                {
                    turnOn();
                }
                catch( PropertyVetoException e )
                {
                    if( e.getCause() != null )
                    {
                        lastThrowable = e.getCause();
                    }
                    else
                    {
                        lastThrowable = e;
                    }
                }
            }
        }

        return status;
    }

    public Date trippedOn()
    {
        return trippedOn == -1 ? null : new Date( trippedOn );
    }

    public Date enabledOn()
    {
        return enableOn == -1 ? null : new Date( enableOn );
    }

    public boolean isOn()
    {
        return status().equals( Status.on );
    }

    public synchronized void throwable( Throwable throwable )
    {
        if( status == Status.on )
        {
            if( allowedThrowables.satisfiedBy( throwable ) )
            {
                // Allowed throwable, so counts as success
                success();
            }
            else
            {
                countDown--;

                lastThrowable = throwable;

                pcs.firePropertyChange( "serviceLevel", ( countDown + 1 ) / ( (double) threshold ), countDown / ( (double) threshold ) );

                if( countDown == 0 )
                {
                    trip();
                }
            }
        }
    }

    public synchronized void success()
    {
        if( status == Status.on && countDown < threshold )
        {
            countDown++;

            pcs.firePropertyChange( "serviceLevel", ( countDown - 1 ) / ( (double) threshold ), countDown / ( (double) threshold ) );
        }
    }

    public void addVetoableChangeListener( VetoableChangeListener vcl )
    {
        vcs.addVetoableChangeListener( vcl );
    }

    public void removeVetoableChangeListener( VetoableChangeListener vcl )
    {
        vcs.removeVetoableChangeListener( vcl );
    }

    public void addPropertyChangeListener( PropertyChangeListener pcl )
    {
        pcs.addPropertyChangeListener( pcl );
    }

    public void removePropertyChangeListener( PropertyChangeListener pcl )
    {
        pcs.removePropertyChangeListener( pcl );
    }
}
