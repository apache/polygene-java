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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.qi4j.library.alarm.Alarm;
import org.qi4j.library.alarm.AlarmEvent;
import org.qi4j.library.alarm.AlarmModel;
import org.qi4j.library.alarm.AlarmModelProvider;
import org.qi4j.library.alarm.impl.AlarmModelImpl;
import org.qi4j.library.alarm.providers.standard.ActivationEvent;
import org.qi4j.library.alarm.providers.standard.GenericAlarmEvent;
import org.qi4j.library.alarm.providers.standard.StandardModelProvider;

public class ActivationEventTest extends TestCase
{
    private Alarm underTest;

    public void setUp()
        throws Exception
    {
        AlarmModelProvider provider = new StandardModelProvider();
        AlarmModel model = new AlarmModelImpl( provider );
        underTest = model.createAlarm( "TestCase Alarm" );
    }

    public void testName()
        throws Exception
    {
        AlarmEvent e = new ActivationEvent( this, underTest, null, null, System.currentTimeMillis() );
        assertEquals( "activation", e.nameInDefaultLocale() );

        Locale english = new Locale( "en" );
        assertEquals( "activation", e.name( english ) );

        Locale swedish = new Locale( "sv" );
        assertEquals( "utl\u00F6sning", e.name( swedish ) );
    }

    public void testDescription()
        throws Exception
    {
        AlarmEvent e = new ActivationEvent( this, underTest, null, null, System.currentTimeMillis() );
        boolean test = e.descriptionInDefaultLocale().toLowerCase().indexOf( "triggered" ) >= 0;
        assertTrue( test );

        Locale english = new Locale( "en" );
        test = e.description( english ).toLowerCase().indexOf( "triggered" ) >= 0;
        assertTrue( test );

        Locale swedish = new Locale( "sv" );
        test = e.description( swedish ).toLowerCase().indexOf( "utl\u00F6sning" ) >= 0;
        assertTrue( test );
    }

    public void testTriggeredBy()
    {
        GenericAlarmEvent e = new ActivationEvent( this, underTest, null, null, System.currentTimeMillis() );

        assertEquals( this, e.triggeredBy() );
    }

    public void testAlarm()
    {
        AlarmEvent e = new ActivationEvent( this, underTest, null, null, System.currentTimeMillis() );

        assertEquals( underTest, e.alarm() );
    }

    public void testEventTime()
        throws Exception
    {
        AlarmEvent e = new ActivationEvent( this, underTest, null, null, System.currentTimeMillis() );

        Thread.sleep( 15 );
        long now = System.currentTimeMillis();
        boolean test = now > e.eventTime().getTime() && ( now - e.eventTime().getTime() < 150 );
        assertTrue( "EventTime not accurate.", test );
    }

    public void testResourceHead()
    {
        GenericAlarmEvent e = new ActivationEvent( this, underTest, null, null, System.currentTimeMillis() );

        assertEquals( "EVENT_ACTIVATION", e.getResourceHead() );
    }

    public void testToString()
    {
        AlarmEvent e = new ActivationEvent( this, underTest, null, null, System.currentTimeMillis() );

        String str = e.toString();
        String pattern =
            "^event\\[Alarm\\[TestCase Alarm : normal  : This is a default Locale description of a testcase Alarm.\\], time\\[[ 0-9:/APM]*\\], oldstate\\[null\\], newstate\\[null\\], activation\\].*";
        Pattern p = Pattern.compile( pattern );
        Matcher m = p.matcher( str );
        assertTrue( m.matches() );
    }
}
