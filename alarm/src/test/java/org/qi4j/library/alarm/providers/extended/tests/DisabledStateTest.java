/*
 * Copyright 2006 Niclas Hedhman.
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
package org.qi4j.library.alarm.providers.extended.tests;

import junit.framework.TestCase;
import org.qi4j.library.alarm.AlarmState;
import org.qi4j.library.alarm.providers.extended.DisabledState;
import java.util.Locale;

public class DisabledStateTest extends TestCase
{

    public void testName()
        throws Exception
    {
        AlarmState s = new DisabledState();
        assertEquals( "disabled", s.getName() );

        Locale english = new Locale( "en" );
        assertEquals( "disabled", s.getName( english ) );

        Locale swedish = new Locale( "sv" );
        assertEquals( "avst\u00E4ngt", s.getName( swedish ) );

        assertTrue( s.toString().startsWith( "state[disabled" ) );
    }

    public void testDescription()
        throws Exception
    {
        AlarmState s = new DisabledState();
        boolean test = s.getDescription().toLowerCase().indexOf( "disabled" ) >= 0;
        assertTrue( test );

        Locale english = new Locale( "en" );
        test = s.getDescription( english ).toLowerCase().indexOf( "disabled" ) >= 0;
        assertTrue( test );

        Locale swedish = new Locale( "sv" );
        test = s.getDescription( swedish ).toLowerCase().indexOf( "avst\u00E4ngt" ) >= 0;
        assertTrue( test );
    }

    public void testCreationTime()
        throws Exception
    {
        AlarmState s = new DisabledState();

        Thread.sleep( 15 );
        long now = System.currentTimeMillis();
        boolean test = now > s.getCreationDate().getTime() && ( now - s.getCreationDate().getTime() < 150 );
        assertTrue( "EventTime not accurate.", test );
    }

    public void testToString()
        throws Exception
    {
        AlarmState s = new DisabledState();
        String str = s.toString();
        assertTrue( str.startsWith( "state[disabled," ) );
    }
}
