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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.library.alarm.AlarmEvent;
import org.qi4j.library.alarm.AlarmHistory;

public class TestAlarmHistory implements AlarmHistory
{

    public AlarmEvent lastEvent()
    {
        return null;
    }

    public AlarmEvent firstEvent()
    {
        return null;
    }

    public AlarmEvent eventAt( int position )
    {
        return null;
    }

    public AlarmEvent eventAtEnd( int position )
    {
        return null;
    }

    public List<AlarmEvent> getAllAlarmEvents()
    {
        return new ArrayList();
    }

    public void setMaxSize( int size )
    {
    }

    public int maxSize()
    {
        return 0;
    }

    public Map<String, Integer> counters()
    {
        return new HashMap();
    }

    public void resetAllCounters()
    {
    }

    public int activateCounter()
    {
        return 0;
    }

    public void resetActivateCounter()
    {
    }
}
