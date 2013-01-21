/*
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;

/**
 * JAVADOC
 */
public class CircuitBreakerTest
{

    private CircuitBreaker cb;

    @Before
    public void createCircuitBreaker()
    {
        // START SNIPPET: direct
        // Create a CircuitBreaker with a threshold of 3, a 250ms timeout, allowing IllegalArgumentExceptions
        CircuitBreaker cb = new CircuitBreaker( 3, 250, CircuitBreakers.in( IllegalArgumentException.class ) );

        // END SNIPPET: direct

        cb.addPropertyChangeListener( new PropertyChangeListener()
        {

            public void propertyChange( PropertyChangeEvent evt )
            {
                System.out.println( evt.getSource() + ":" + evt.getPropertyName() + "=" + evt.getOldValue() + " -> " + evt.getNewValue() );
            }

        } );

        this.cb = cb;
    }

    @Test
    public void GivenCBWhenTripWithExceptionsAndTurnOnThenStatusIsOn()
            throws PropertyVetoException
    {

        assertThat( cb.status(), CoreMatchers.equalTo( CircuitBreaker.Status.on ) );

        // START SNIPPET: direct
        // Service levels goes down but does not cause a trip
        cb.throwable( new IOException() );

        // END SNIPPET: direct

        assertThat( cb.status(), CoreMatchers.equalTo( CircuitBreaker.Status.on ) );

        // START SNIPPET: direct
        // Service level goes down and causes a trip
        cb.throwable( new IOException() );
        cb.throwable( new IOException() );

        // END SNIPPET: direct

        assertThat( cb.status(), CoreMatchers.equalTo( CircuitBreaker.Status.off ) );

        // START SNIPPET: direct
        // Turn on the CB again
        cb.turnOn();

        // END SNIPPET: direct

        assertThat( cb.status(), CoreMatchers.equalTo( CircuitBreaker.Status.on ) );
    }

    @Test
    public void GivenCBWhenAllowedExceptionsThenServiceLevelIsNormal()
            throws PropertyVetoException
    {

        assertThat( cb.status(), CoreMatchers.equalTo( CircuitBreaker.Status.on ) );

        // Service level goes down
        cb.throwable( new IOException() );

        // Service levels goes up
        cb.throwable( new IllegalArgumentException() );

        assertThat( cb.serviceLevel(), CoreMatchers.equalTo( 1.0 ) );
    }

    @Test
    public void GivenCBWhenTripCBWithExceptionsAndTimeoutThenStatusIsOn()
            throws PropertyVetoException
    {
        assertThat( cb.status(), CoreMatchers.equalTo( CircuitBreaker.Status.on ) );

        // START SNIPPET: direct
        // Service levels goes down and causes a trip
        cb.throwable( new IOException() );
        cb.throwable( new IOException() );
        cb.throwable( new IOException() );

        // END SNIPPET: direct

        assertThat( cb.status(), CoreMatchers.equalTo( CircuitBreaker.Status.off ) );

        // START SNIPPET: direct
        // Wait until timeout

        // END SNIPPET: direct
        assertThat( cb.status(), CoreMatchers.equalTo( CircuitBreaker.Status.off ) );
        try {
            System.out.println( "Wait..." );
            Thread.sleep( 300 );
        } catch ( InterruptedException e ) {
            // Ignore
        }

        // START SNIPPET: direct
        // CircuitBreaker is back on
        
        // END SNIPPET: direct
        assertThat( cb.status(), CoreMatchers.equalTo( CircuitBreaker.Status.on ) );
    }

    @Test
    public void GivenCBWhenExceptionsAndSuccessesThenStatusIsOn()
            throws PropertyVetoException
    {
        assertThat( cb.status(), CoreMatchers.equalTo( CircuitBreaker.Status.on ) );

        // Service levels goes down and causes a trip
        cb.throwable( new IOException() );
        cb.throwable( new IOException() );
        cb.success();
        cb.success();
        cb.throwable( new IOException() );
        cb.throwable( new IOException() );

        assertThat( cb.status(), CoreMatchers.equalTo( CircuitBreaker.Status.on ) );
    }

    @Test
    public void GivenCBWhenTripCBWithExceptionsAndSuccessesThenStatusIsOff()
            throws PropertyVetoException
    {
        assertThat( cb.status(), CoreMatchers.equalTo( CircuitBreaker.Status.on ) );

        // Service levels goes down and causes a trip
        cb.throwable( new IOException() );
        cb.throwable( new IOException() );
        cb.throwable( new IOException() );
        cb.success();

        assertThat( cb.status(), CoreMatchers.equalTo( CircuitBreaker.Status.off ) );
    }

    @Test
    public void GivenCBWhenTripCBWithExceptionsAndGetStatusWithFailureThenStatusIsOff()
            throws PropertyVetoException
    {
        assertThat( cb.status(), CoreMatchers.equalTo( CircuitBreaker.Status.on ) );

        cb.addVetoableChangeListener( new VetoableChangeListener()
        {

            public void vetoableChange( PropertyChangeEvent evt )
                    throws PropertyVetoException
            {
                if ( evt.getNewValue() == CircuitBreaker.Status.on ) {
                    throw new PropertyVetoException( "Service is down", evt );
                }
            }

        } );

        // Service levels goes down and causes a trip
        cb.throwable( new IOException() );
        cb.throwable( new IOException() );
        cb.throwable( new IOException() );

        try {
            System.out.println( "Wait..." );
            Thread.sleep( 300 );
        } catch ( InterruptedException e ) {
            // Ignore
        }

        assertThat( cb.status(), CoreMatchers.equalTo( CircuitBreaker.Status.off ) );
        assertThat( cb.lastThrowable().getMessage(), CoreMatchers.equalTo( "Service is down" ) );
    }

}
