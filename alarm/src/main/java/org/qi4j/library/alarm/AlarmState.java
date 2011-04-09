/*
 * Copyright 1996-2005 Niclas Hedhman.
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

package org.qi4j.library.alarm;

import java.util.Date;
import java.util.Locale;

/**
 * State of an Alarm.
 *
 * @author Niclas Hedhman
 */
public interface AlarmState
{

    /**
     * Returns the Date/Time of when this state was created.
     *
     * @return the timestamp of when the state was created.
     */
    Date getCreationDate();

    /**
     * Returns the Name of the AlarmState.
     * This is the technical name of the AlarmState, such as Normal,
     * Activated and so forth in non-locale specific form.
     *
     * @return the name of the AlarmState in the default locale.
     *
     * @see #getName(Locale)
     */
    String getName();

    /**
     * Returns the Name of the AlarmState in a locale.
     * This is the technical name of the AlarmState, such as Normal,
     * Activated and so forth in a locale specific form.
     *
     * @param locale the locale to return the name in.
     *
     * @return the name of the AlarmState in the given locale.
     *
     * @see #getName()
     */
    String getName( Locale locale );

    /**
     * Returns a description of this AlarmState.
     * The description is returned for the current default locale.
     *
     * @return a description of this AlarmState in the default locale.
     */
    String getDescription();

    /**
     * Returns a description of this AlarmState in a particular locale.
     *
     * @param locale the locale to return the description in.
     *
     * @return a description of this AlarmState in the given locale.
     */
    String getDescription( Locale locale );
}
