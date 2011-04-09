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

import java.util.Locale;
import java.util.Map;

/**
 * Defines the basic Alarm interface.
 * <p>
 * This is the basic interface for the whole Alarm System. The Alarm
 * is created by calling <code>createAlarm()</code> method in the
 * <code>AlarmService</code> or the <code>AlarmModel</code>.
 * </p>
 * <ul>
 * <li>All alarms carries a set of properties, runtime extendable.</li>
 * <li>All alarms can be activated (on), deactivated (off) and acknowledged.</li>
 * <li>All alarms have an AlarmState.</li>
 * <li>All alarms generates AlarmEvents.</li>
 * <li>The behaviour of the Alarm is defined by an AlarmModel.</li>
 * <li>Every Alarm can have its own AlarmModel.</li>
 * <li>Any number of AlarmStates and AlarmEvents can be defined in an AlarmModel.</li>
 * </ul>
 * <p>
 * Alarms can be triggered by an standard trigger, which are java.lang.Strings.
 * 3 triggers are pre-defined; <i>activate</i>,<i>deactivate</i> and
 * <i>acknowledge</i> and must be present in all standard systems and standard impl.
 * </p>
 * <p>
 * The basic usage looks like this;<code><pre>
 *     // Creation
 *     Alarm ala1 = alarmService.createAlarm( "My Alarm" );
 *       :
 *       :
 *     // use
 *     if( alarmcondition )  // The condition should only detect transitions.
 *         ala1.trigger( this, "activate" );
 * </pre></code>
 * <p>
 * It is important to know that every call to <code>trigger()</code>
 * will generate an AlarmEvent, so the <code>trigger()</code> should
 * only be called when the standard condition changes. For this purpose, there is
 * a convenience method, that will create/generate the <code>trigger</code>
 * method calls when a boolean standard condition changes. The usage is fairly
 * simple. Example;<code><pre>
 *     ala1.setCondition( value > highlimit );
 * </pre></code>
 * It is possible to mix and match the usage of <code>setCondition()</code>
 * and <code>trigger()</code> methods without any concerns.
 * </p>
 * <p>
 * To create alarms with different AlarmModels, other than the
 * default as shown above, you need to retrieve the AlarmModel that
 * fulfill the needs required. This can be done in the following manner;
 * <code><pre>
 *     AlarmModel[] impl = alarmService.getAlarmModelsAvailable();
 *     // selection algorithm
 *     Alarm ala2 = impl[selected].createAlarm( "My Alarm" );
 * </pre></code>
 * </p>
 * <p>
 * The default AlarmModel can be changed by a call to the
 * <code>AlarmService.setDefaultAlarmModel()</code> and
 * ALL ALARMS that has the old AlarmModel assigned to it, will be
 * transferred to the new default AlarmModel. It is important to
 * understand that this is done irregardless of whether the Alarm was
 * created from the <code>AlarmService.createAlarm()</code> method or
 * the <code>AlarmModel.createAlarm()</code> method. If distinct different
 * behaviours are required for certain Alarms, and yet want to allow
 * users to freely select AlarmModel for all other Alarms, one need
 * to create two instances of the same AlarmModels, one used solely
 * for the pre-defined Alarm behaviours, and the others for the rest of
 * the Alarms.
 * </p>
 *
 * @author Niclas Hedhman
 */
public interface Alarm
{

    public static final String TRIGGER_ACTIVATION = "activation";
    public static final String TRIGGER_DEACTIVATION = "deactivation";
    public static final String TRIGGER_ACKNOWLEDGE = "acknowledge";
    public static final String TRIGGER_BLOCK = "block";
    public static final String TRIGGER_UNBLOCK = "unblock";
    public static final String TRIGGER_ENABLE = "enable";
    public static final String TRIGGER_DISABLE = "disable";

    /**
     * Returns the AlarmModel for the Alarm.
     * If the Alarm does not have a specific AlarmModel specified
     * this method will return null, and the default AlarmModel is
     * used.
     *
     * @return the AlarmModel used for this Alarm.
     */
    AlarmModel getAlarmModel();

    /**
     * Sets the AlarmModel for the Alarm.
     *
     * @param model the AlarmModel to use on this Alarm.
     */
    void setAlarmModel( AlarmModel model );

    /**
     * Trigger a state change.
     * <p>
     * When the Alarm object receives a trigger, it must consult the
     * AlarmModel provider and figure out if there is an actual state change
     * occuring and if any AlarmEvents should be fired.
     * </p>
     *
     * @param source  The object that is trigging the Alarm.
     * @param trigger The trigger to execute if existing in the AlarmModel.
     *
     * @throws AlarmTriggerException if a trigger could not be executed, typically that the AlarmModel does not specify the trigger.
     */
    void trigger( Object source, String trigger )
        throws AlarmTriggerException;

    /**
     * Activates an Alarm.
     * <p>
     * Convinience method for:<pre>
     *       trigger( source, "activate" );
     *   </pre>
     * </p>
     *
     * @param source The object that is trigging the Alarm.
     */
    void activate( Object source );

    /**
     * Deactivates an Alarm.
     * Convinience method for:<pre>
     *     trigger( source, "deactivate" );
     * </pre>
     *
     * @param source The object that is trigging the Alarm.
     */
    void deactivate( Object source );

    /**
     * Acknowledges an Alarm.
     * Convinience method for:<pre>
     *     trigger( source, "acknowledge" );
     * </pre>
     *
     * @param source The object that is trigging the Alarm.
     */
    void acknowledge( Object source );

    /**
     * Get Alarm condition.
     * To reduce Alarm condition calculations for Implementors, each Alarm should
     * be able to work with a "true/false" trigger. Only changes to this trigger
     * will cause an event.
     *
     * @return The condition of the Alarm, which is used to simplify trigging of activate and deactivate.
     */
    boolean getCondition();

    /**
     * Set Alarm condition.
     * To reduce Alarm condition calculations for Implementors, each Alarm should
     * be able to work with a "true/false" trigger. Only changes to this trigger
     * will cause an event.
     * Causes an Activation or Deactivation if state of condition changes.
     *
     * @param condition Sets the Alarm condition.
     */
    void setCondition( boolean condition );

    /**
     * Returns the current state of the standard.
     *
     * @return The AlarmState (interface) object
     */
    AlarmState getState();

    /**
     * Returns the AlarmHistory of the standard.
     *
     * @return The AlarmHistory object, or null if AlarmHistory is not supported.
     */
    AlarmHistory getHistory();

    /**
     * Return all Properties
     *
     * @return the properties of this Alarm.
     */
    Map getProperties();

    /**
     * Return the Property of the given name.
     *
     * @param name The name of the property to return.
     *
     * @return the named property of this Alarm.
     */
    Object getProperty( String name );

    /**
     * Sets the Property of the given name.
     *
     * @param name  The name of the property to set.
     * @param value The value to set the named property to.
     */
    void setProperty( String name, Object value );

    /**
     * Returns the Name of the Alarm.
     * This normally returns the human readable technical name of the Alarm.
     *
     * @return the name of the Alarm.
     */
    String getName();

    /**
     * Returns a Description of the Alarm.
     * This normally returns a full Description of the Alarm in the
     * default Locale.
     *
     * @return a human-readable description of the Alarm in the default locale.
     */
    String getDescription();

    /**
     * Returns a Description of the Alarm.
     * This normally returns a full Description of the Alarm in the
     * Locale. If Locale is <code><b>null</b></code>, then the
     * default Locale is used.
     *
     * @param locale The locale to return the description in, or null to use default locale.
     *
     * @return a human-readable description of the Alarm in the given locale.
     */
    String getDescription( Locale locale );

    /**
     * Adds an AlarmListener to this Alarm.
     * This method is only used in exceptional cases, where
     * only particular alarms are of interest.
     *
     * @param listener The listener to be added.
     *
     * @see AlarmService#addAlarmListener(AlarmListener)
     */
    void addAlarmListener( AlarmListener listener );

    /**
     * Removes AlarmListener from this Alarm.
     * Only AlarmListeners that are directly registered to
     * this Alarm can be removed by this method. AlarmListeners
     * that have been registered at the AlarmService will
     * still receive AlarmEvents from this method, even if
     * this method is called.
     *
     * @param listener The listener to be removed.
     *
     * @see AlarmService#removeAlarmListener(AlarmListener)
     */
    void removeAlarmListener( AlarmListener listener );
}
