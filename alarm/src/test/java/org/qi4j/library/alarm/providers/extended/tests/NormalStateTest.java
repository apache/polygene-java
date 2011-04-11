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
import org.qi4j.library.alarm.providers.extended.NormalState;
import java.util.Locale;

public class NormalStateTest extends TestCase
{

    public void testName()
        throws Exception
    {
        AlarmState s = new NormalState();
        assertEquals( "normal", s.getName() );

        Locale english = new Locale( "en" );
        assertEquals( "normal", s.getName( english ) );

        Locale swedish = new Locale( "sv" );
        assertEquals( "normal", s.getName( swedish ) );
    }

    public void testDescription()
        throws Exception
    {
        AlarmState s = new NormalState();
        boolean test = s.getDescription().toLowerCase().indexOf( "normal" ) >= 0;
        assertTrue( test );

        Locale english = new Locale( "en" );
        test = s.getDescription( english ).toLowerCase().indexOf( "normal" ) >= 0;
        assertTrue( test );

        Locale swedish = new Locale( "sv" );
        test = s.getDescription( swedish ).toLowerCase().indexOf( "normal" ) >= 0;
        assertTrue( test );
    }

    public void testCreationTime()
        throws Exception
    {
        AlarmState s = new NormalState();

        Thread.sleep( 15 );
        long now = System.currentTimeMillis();
        boolean test = now > s.creationDate().getTime() && ( now - s.creationDate().getTime() < 50 );
        assertTrue( "EventTime not accurate.", test );
    }

    public void testToString()
        throws Exception
    {
        AlarmState s = new NormalState();
        String str = s.toString();
        assertTrue( str.startsWith( "state[normal," ) );
    }
}
