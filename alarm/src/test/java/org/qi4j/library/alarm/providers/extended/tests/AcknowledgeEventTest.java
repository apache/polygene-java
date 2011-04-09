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
import junit.framework.Assert;
import org.qi4j.library.alarm.Alarm;
import org.qi4j.library.alarm.AlarmModelProvider;
import org.qi4j.library.alarm.AlarmModel;
import org.qi4j.library.alarm.AlarmEvent;
import org.qi4j.library.alarm.providers.extended.ExtendedModelProvider;
import org.qi4j.library.alarm.providers.extended.AcknowledgeEvent;
import org.qi4j.library.alarm.providers.extended.GenericAlarmEvent;
import org.qi4j.library.alarm.impl.AlarmModelImpl;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class AcknowledgeEventTest
    extends TestCase
{
    private Alarm underTest;

    public void setUp()
        throws Exception
    {
        AlarmModelProvider provider = new ExtendedModelProvider();
        AlarmModel model = new AlarmModelImpl( provider );
        underTest = model.createAlarm( "TestCase Alarm" );
    }

    public void testName()
        throws Exception
    {
        AlarmEvent e = new AcknowledgeEvent( this, underTest, null, null, System.currentTimeMillis() );
        assertEquals( "acknowledge", e.getName() );
        Locale english = new Locale( "en" );
        assertEquals( "acknowledge", e.getName( english ) );
        Locale swedish = new Locale( "sv" );
        assertEquals( "kvittens", e.getName( swedish ) );
    }

    public void testDescription()
        throws Exception
    {
        AlarmEvent e = new AcknowledgeEvent( this, underTest, null, null, System.currentTimeMillis() );
        boolean test = e.getDescription().toLowerCase().indexOf( "acknowledge" ) >= 0;
        assertTrue( test );
        Locale english = new Locale( "en" );
        test = e.getDescription( english ).toLowerCase().indexOf( "acknowledge" ) >= 0;
        assertTrue( test );
        Locale swedish = new Locale( "sv" );
        test = e.getDescription( swedish ).toLowerCase().indexOf( "kvittering" ) >= 0;
        assertTrue( test );
    }

    public void testTriggeredBy()
    {
        GenericAlarmEvent e = new AcknowledgeEvent( this, underTest, null, null, System.currentTimeMillis() );
        Assert.assertEquals( this, e.getTriggeredBy() );
    }

    public void testAlarm()
    {
        AlarmEvent e = new AcknowledgeEvent( this, underTest, null, null, System.currentTimeMillis() );
        assertEquals( underTest, e.getAlarm() );
    }

    public void testEventTime()
        throws Exception
    {
        AlarmEvent e = new AcknowledgeEvent( this, underTest, null, null, System.currentTimeMillis() );
        Thread.sleep( 15 );
        long now = System.currentTimeMillis();
        boolean test = now > e.getEventTime().getTime() && ( now - e.getEventTime().getTime() < 150 );
        assertTrue( "EventTime not accurate.", test );
    }

    public void testToString()
    {
        AlarmEvent e = new AcknowledgeEvent( this, underTest, null, null, System.currentTimeMillis() );
        String str = e.toString();
        String pattern =
            "^event\\[Alarm\\[TestCase Alarm : normal  : This is a default Locale description of a testcase Alarm.\\], time\\[[ 0-9:/APM]*\\], oldstate\\[null\\], newstate\\[null\\], acknowledge\\].*";
        Pattern p = Pattern.compile( pattern );
        Matcher m = p.matcher( str );
        assertTrue( m.matches() );
    }
}
