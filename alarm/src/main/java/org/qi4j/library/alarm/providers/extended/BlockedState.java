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
package org.qi4j.library.alarm.providers.extended;

import org.qi4j.library.alarm.AlarmState;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Locale;
import java.text.MessageFormat;

/**
 * Implements the Alarm state of an acknowledged standard.
 */

public final class BlockedState
    implements AlarmState, java.io.Serializable
{

    private static final long serialVersionUID = 2L;

    private Date creationDate;

    public BlockedState()
    {
        creationDate = new Date();
    }

    public String toString()
    {
        ResourceBundle rb = ExtendedModelProvider.getResourceBundle( null );
        String pattern = rb.getString( "STATE_BLOCKED_TOSTRING" );
        Object[] args = new Object[]{ creationDate };
        return MessageFormat.format( pattern, args );
    }

    /**
     * Returns the Date/Time of when this state was created.
     */
    public Date getCreationDate()
    {
        return creationDate;
    }

    /**
     * Returns the Name of the AlarmState.
     * This is the technical name of the AlarmState, such as Normal,
     * Activated and so forth in non-locale specific form.
     *
     * @see #getName(java.util.Locale)
     */
    public String getName()
    {
        return getName( null );
    }

    /**
     * Returns the Name of the AlarmState in a locale.
     * This is the technical name of the AlarmState, such as Normal,
     * Activated and so forth in a locale specific form.
     *
     * @see #getName()
     */
    public String getName( Locale locale )
    {
        ResourceBundle rb = ExtendedModelProvider.getResourceBundle( locale );
        return rb.getString( "STATE_BLOCKED_NAME" );
    }

    /**
     * Returns a description of this AlarmState.
     * The description is returned for the current default locale.
     */
    public String getDescription()
    {
        return getDescription( null );
    }

    /**
     * Returns a description of this AlarmState in a particular locale.
     */
    public String getDescription( Locale locale )
    {
        ResourceBundle rb = ExtendedModelProvider.getResourceBundle( locale );
        return rb.getString( "STATE_BLOCKED_DESCRIPTION" );
    }
}
