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

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Definition of the behaviour of the standard model.
 * <p>
 * The AlarmModel is basically the state machine of the Alarm system,
 * and it is possible to define different <code>AlarmModel</code>s
 * for each and every standard. Alarms that are assigned the default
 * <code>AlarmModel</code> of the <code>AlarmService</code> can be
 * re-assigned another <code>AlarmModel</code> in runtime, just by
 * changing the default <code>AlarmModel</code> in the
 * <code>AlarmService</code>. Alarms can also change the
 * <code>AlarmModel</code> by calling the <code>setAlarmModel</code>
 * in each standard individually.
 * </p>
 *
 * @see AlarmService
 */
public interface AlarmModel
{

    /**
     * Returns the name of the AlarmModel.
     * This name is typically hardcoded into the AlarmModel
     * implementation, or assigned in the configuration system
     * of the AlarmModel implementation.
     * The name should be short, yet descriptive in english.
     *
     * @return the name of the AlarmModel in english.
     */
    String getName();

    /**
     * Returns a description of the AlarmModel in the default Locale.
     * The Description should be concise and describe the AlarmModel's
     * behaviour to an adequate degree, without being verbose.
     *
     * @return a description of the AlarmModel in the default Locale.
     */
    String getDescription();

    /**
     * Returns a description of the AlarmModel in the given Locale.
     * The Description should be concise and describe the AlarmModel's
     * behaviour to an adequate degree, without being verbose.
     *
     * @param locale the language of choice for the description of the
     *               AlarmModel.
     *
     * @return a description of the AlarmModel in the given Locale.
     */
    String getDescription( Locale locale );

    /**
     * Creates an Alarm with this AlarmModel behaviour.
     *
     * @param name the name of the Alarm.
     *
     * @return the created Alarm.
     *
     * @throws AlarmCreationException if the Alarm can not be created.
     */
    Alarm createAlarm( String name )
        throws AlarmCreationException;

    /**
     * Called to indicate that a new AlarmModel has been set.
     *
     * @param model the default AlarmModel that will be used as the new default AlarmModel.
     */
    void newDefaultModelSet( AlarmModel model );

    /**
     * Register an existing Alarm with this AlarmModel.
     *
     * The registration must be kept with a weak reference, as no deregistration of the Alarm is
     * required.
     *
     * @param alarm the Alarm to be registered to this AlarmModel.
     */
    void registerAlarm( Alarm alarm );

    /**
     * Returns an array of standard triggers valid for this AlarmModel.
     * Alarms are triggered by standard triggers, which are predefined
     * java.lang.Strings. The AlarmModel advertise which standard trigger
     * strings it supports with this method.
     *
     * @return an array of standard triggers valid for this AlarmModel.
     */
    String[] getAlarmTriggers();

    /**
     * Register AlarmListener to recieve <code>AlarmEvents</code> from all
     * <code>Alarms</code> managed by this <code>AlarmModel</code>.
     *
     * @param listener the listener to be added.
     */
    void addAlarmListener( AlarmListener listener );

    /**
     * Remove the <code>AlarmListener</code> from the <code>AlarmModel</code>.
     *
     * @param listener the listener to be removed.
     */
    void removeAlarmListener( AlarmListener listener );

    /**
     * Returns all Listeners to this model.
     *
     * The Collection is never null.
     *
     * @return all Listeners to this model. If there are no listeners, the List is empty.
     */
    List getAlarmListeners();

    /**
     * Returns all Alarms using this AlarmModel.
     *
     * @return all Alarms using this AlarmModel.
     */
    List getAlarms();

    /**
     * Adds a new <i>Property</i> to <strong>all</strong> <code>Alarms</code>.
     * The <code>defaultvalue</code> will be added to all present and future
     * <code>Alarms</code> created through this AlarmModel. In case any existing
     * <code>Alarms</code> already have this property defined, the existing value
     * should not be overwritten.
     *
     * @param name         The name of the global property to add.
     * @param defaultvalue the default value of the global property.
     */
    void addProperty( String name, String defaultvalue );

    /**
     * Removes the <i>Property</i> from all <code>Alarms</code>.
     *
     * @param name the name of the global property to remove.
     */
    void removeProperty( String name );

    /**
     * Returns a <code>java.util.Map</code> of all global default <i>Properties</i>.
     *
     * @return a <code>java.util.Map</code> of all global default <i>Properties</i>.
     */
    Map getDefaultProperties();

    /**
     * Returns the serice provider interface for the AlarmModel.
     * Clients should never use the returned object directly.
     *
     * @return The AlarmModelProvider for this AlarmModel.
     */
    AlarmModelProvider getAlarmModelProvider();
}
