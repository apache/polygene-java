/*
 * Copyright 1996-2011 Niclas Hedhman.
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

import java.util.List;
import java.util.Locale;
import org.qi4j.api.common.Optional;

/**
 * Definition of the behaviour of the alarm model.
 * <p>
 * The AlarmModel is basically the state machine of the AlarmPoint system,
 * and it is possible to define different <code>AlarmModel</code>s
 * for each and every alarm. Alarms that are assigned the default
 * <code>AlarmModel</code> of the <code>AlarmSystem</code> can be
 * re-assigned another <code>AlarmModel</code> in runtime, just by
 * changing the default <code>AlarmModel</code> in the
 * <code>AlarmSystem</code>. Alarms can also change the
 * <code>AlarmModel</code> by calling the <code>setAlarmModel</code>
 * in each alarm individually.
 * </p>
 *
 * @see AlarmSystem
 */
public interface AlarmModel
{
    String MODEL_BUNDLE_NAME = "org.qi4j.library.alarm.AlarmResources";

    /**
     * Execute the required changes upon an AlarmTrigger.
     * The AlarmSystem calls this method. The AlarmModel must NOT update the AlarmPoint itself, and only return the
     * resulting event, and the AlarmSystem will update the AlarmStatus accordingly.
     *
     * @param alarm   The AlarmPoint the trigger is for.
     * @param trigger the AlarmTrigger that was used.
     *
     * @return An AlarmEvent representing the state change for the given trigger.
     *
     * @throws IllegalArgumentException If the trigger given is not supported by this alarm model.
     */
    AlarmEvent evaluate( AlarmPoint alarm, String trigger )
        throws IllegalArgumentException;

    List<String> statusList();

    /**
     * Returns an array of alarm triggers valid for this AlarmModel.
     * Alarms are triggered by alarm triggers, which are predefined
     * java.lang.Strings. The AlarmModel advertise which alarm trigger
     * strings it supports with this method.
     *
     * @return an array of alarm triggers valid for this AlarmModel.
     */
    List<String> alarmTriggers();

//    /**
//     * Adds a new <i>Property</i> to <strong>all</strong> <code>Alarms</code>.
//     * The <code>defaultvalue</code> will be added to all present and future
//     * <code>Alarms</code> created through this AlarmModel. In case any existing
//     * <code>Alarms</code> already have this property defined, the existing value
//     * should not be overwritten.
//     *
//     * @param name         The name of the global property to add.
//     * @param defaultvalue the default value of the global property.
//     */
//    void addProperty( String name, String defaultvalue );
//
//    /**
//     * Removes the <i>Property</i> from all <code>Alarms</code>.
//     *
//     * @param name the name of the global property to remove.
//     */
//    void removeProperty( String name );
//
//    /**
//     * Returns a <code>java.util.Map</code> of all global default <i>Properties</i>.
//     *
//     * @return a <code>java.util.Map</code> of all global default <i>Properties</i>.
//     */
//    Map<String,String> defaultProperties();

    String computeTrigger( AlarmStatus status, boolean condition );

    boolean computeCondition( AlarmStatus status );

    /**
     * Returns the Name of the AlarmModel.
     * This normally returns the human readable technical name of
     * the AlarmModel.
     *
     * @return The system name of this alarm model.
     */
    String modelName();

    /**
     * Returns a Description of the AlarmModel in the default Locale.
     * This normally returns a full Description of the AlarmModel in the
     * default Locale.
     *
     * @return the description of the ModelProvider, in the default locale.
     */
    String modelDescription();

    /**
     * Returns a Description of the AlarmModel.
     * This normally returns a full Description of the AlarmModel in the
     * Locale. If Locale is <code><b>null</b></code>, then the
     * default Locale is used.
     *
     * @param locale The locale that should be used for the description, or null for the default locale.
     *
     * @return The human readable, in the given locale, description of this alarm model.
     */
    String modelDescription( @Optional Locale locale );
}
