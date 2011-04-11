/*
 * Copyright 2005 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.alarm.providers.standard.tests;

import java.util.Locale;
import junit.framework.TestCase;
import org.qi4j.library.alarm.AlarmState;
import org.qi4j.library.alarm.providers.standard.AcknowledgedState;

public class AcknowledgedStateTest extends TestCase
{

    public void testName()
        throws Exception
    {
        AlarmState s = new AcknowledgedState();
        assertEquals( "acknowledged", s.getName() );

        Locale english = new Locale( "en" );
        assertEquals( "acknowledged", s.getName( english ) );

        Locale swedish = new Locale( "sv" );
        assertEquals( "kvitterat", s.getName( swedish ) );

        assertTrue( s.toString().startsWith( "state[acknowledge" ) );
    }

    public void testDescription()
        throws Exception
    {
        AlarmState s = new AcknowledgedState();
        boolean test = s.getDescription().toLowerCase().indexOf( "acknowledged" ) >= 0;
        assertTrue( test );

        Locale english = new Locale( "en" );
        test = s.getDescription( english ).toLowerCase().indexOf( "acknowledged" ) >= 0;
        assertTrue( test );

        Locale swedish = new Locale( "sv" );
        test = s.getDescription( swedish ).toLowerCase().indexOf( "kvitterat" ) >= 0;
        assertTrue( test );
    }

    public void testCreationTime()
        throws Exception
    {
        AlarmState s = new AcknowledgedState();

        Thread.sleep( 15 );
        long now = System.currentTimeMillis();
        boolean test = now > s.creationDate().getTime() && ( now - s.creationDate().getTime() < 150 );
        assertTrue( "EventTime not accurate.", test );
    }

    public void testToString()
        throws Exception
    {
        AlarmState s = new AcknowledgedState();
        String str = s.toString();
        assertTrue( str.startsWith( "state[acknowledged," ) );
    }
}
