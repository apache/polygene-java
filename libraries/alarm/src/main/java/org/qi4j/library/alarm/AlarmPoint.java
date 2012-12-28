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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.Property;

/**
 * Defines the basic AlarmPoint interface.
 * <p>
 * This is the basic interface for the whole AlarmPoint System. The AlarmPoint
 * is created by calling <code>createAlarm()</code> method in the
 * <code>AlarmSystem</code> or the <code>AlarmModel</code>.
 * </p>
 * <ul>
 * <li>All alarms carries a set of attributes, runtime extendable.</li>
 * <li>All alarms can be activated (on), deactivated (off) and acknowledged.</li>
 * <li>All alarms have an AlarmStatus.</li>
 * <li>All alarms generates AlarmEvents.</li>
 * <li>The behaviour of the AlarmPoint is defined by an AlarmModel.</li>
 * <li>Every AlarmPoint can have its own AlarmModel.</li>
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
 *     AlarmPoint ala1 = alarmService.createAlarm( "My AlarmPoint" );
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
 *     ala1.updateCondition( value > highlimit );
 * </pre></code>
 * It is possible to mix and match the usage of <code>updateCondition()</code>
 * and <code>trigger()</code> methods without any concerns.
 * </p>
 * <p>
 * To create alarms with different AlarmModels, other than the
 * default as shown above, you need to retrieve the AlarmModel that
 * fulfill the needs required. This can be done in the following manner;
 * <code><pre>
 *     AlarmModel[] impl = alarmService.getAlarmModelsAvailable();
 *     // selection algorithm
 *     AlarmPoint ala2 = impl[selected].createAlarm( "My AlarmPoint" );
 * </pre></code>
 * </p>
 * <p>
 * The default AlarmModel can be changed by a call to the
 * <code>AlarmSystem.setDefaultAlarmModel()</code> and
 * ALL ALARMS that has the old AlarmModel assigned to it, will be
 * transferred to the new default AlarmModel. It is important to
 * understand that this is done irregardless of whether the AlarmPoint was
 * created from the <code>AlarmSystem.createAlarm()</code> method or
 * the <code>AlarmModel.createAlarm()</code> method. If distinct different
 * behaviours are required for certain Alarms, and yet want to allow
 * users to freely select AlarmModel for all other Alarms, one need
 * to create two instances of the same AlarmModels, one used solely
 * for the pre-defined AlarmPoint behaviours, and the others for the rest of
 * the Alarms.
 * </p>
 *
 * @author Niclas Hedhman
 */
public interface AlarmPoint
{

    String STATUS_NORMAL = "Normal";
    String STATUS_ACTIVATED = "Activated";
    String STATUS_DEACTIVATED = "Deactivated";
    String STATUS_REACTIVATED = "Reactivated";
    String STATUS_ACKNOWLEDGED = "Acknowledged";
    String STATUS_DISABLED = "Disabled";
    String STATUS_BLOCKED = "Blocked";

    String EVENT_ENABLING = "enabled";
    String EVENT_DISABLING = "disabled";
    String EVENT_BLOCKING = "blocked";
    String EVENT_UNBLOCKING = "unblocked";
    String EVENT_ACTIVATION = "activation";
    String EVENT_DEACTIVATION = "deactivation";
    String EVENT_ACKNOWLEDGEMENT = "acknowledgement";

    String TRIGGER_ACTIVATE = "activate";
    String TRIGGER_DEACTIVATE = "deactivate";
    String TRIGGER_ACKNOWLEDGE = "acknowledge";
    String TRIGGER_BLOCK = "block";
    String TRIGGER_UNBLOCK = "unblock";
    String TRIGGER_ENABLE = "enable";
    String TRIGGER_DISABLE = "disable";

    /**
     * Trigger a state change.
     * <p>
     * When the AlarmPoint object receives a trigger, it must consult the
     * AlarmModel and figure out if there is an actual state change
     * occurring and if any AlarmEvents should be fired.
     * </p>
     *
     * @param trigger The trigger to execute if existing in the AlarmModel.
     *
     * @throws IllegalArgumentException if a trigger is not a known one.
     */
    void trigger( String trigger )
        throws IllegalArgumentException;

    /**
     * Activates an AlarmPoint.
     * <p>
     * Convinience method for:<pre>
     *       trigger( "activate" );
     *   </pre>
     * </p>
     */
    void activate();

    /**
     * Deactivates an AlarmPoint.
     * Convinience method for:<pre>
     *     trigger( "deactivate" );
     * </pre>
     */
    void deactivate();

    /**
     * Acknowledges an AlarmPoint.
     * Convinience method for:<pre>
     *     trigger( source, "acknowledge" );
     * </pre>
     */
    void acknowledge();

    /**
     * Get AlarmPoint condition.
     * To reduce AlarmPoint condition calculations for Implementors, each AlarmPoint should
     * be able to work with a "true/false" trigger. Only changes to this trigger
     * will cause an event.
     *
     * @return The condition of the AlarmPoint, which is used to simplify trigging of activate and deactivate.
     */
    boolean currentCondition();

    /**
     * Set AlarmPoint condition.
     * To reduce AlarmPoint condition calculations for Implementors, each AlarmPoint should
     * be able to work with a "true/false" trigger. Only changes to this trigger
     * will cause an event.
     * Causes an Activation or Deactivation if state of condition changes.
     *
     * @param condition Sets the AlarmPoint condition.
     */
    void updateCondition( boolean condition );

    /**
     * Returns the current state of the standard.
     *
     * @return The AlarmStatus (interface) object
     */
    AlarmStatus currentStatus();

    /**
     * Returns the AlarmHistory of the standard.
     *
     * @return The AlarmHistory object, or null if AlarmHistory is not supported.
     */
    AlarmHistory history();

    /**
     * Return all attribute names
     *
     * @return the names of the attributes of this AlarmPoint.
     */
    List<String> attributeNames();

    /**
     * Return the attribute of the given name.
     *
     * @param name The name of the attribute to return.
     *
     * @return the named attribute of this AlarmPoint.
     */
    String attribute( String name );

    /**
     * Sets the attribute of the given name.
     *
     * @param name  The name of the attribute to set.
     * @param value The value to set the named attribute to.
     */
    void setAttribute( String name, @Optional String value );

    /**
     * Returns the Name of the AlarmPoint.
     * This normally returns the human readable technical name of the AlarmPoint.
     *
     * @return the name of the AlarmPoint.
     */
    String name();

    /**
     * Returns a Description of the AlarmPoint.
     * This normally returns a full Description of the AlarmPoint in the
     * default Locale.
     *
     * @return a human-readable description of the AlarmPoint in the default locale.
     */
    String descriptionInDefaultLocale();

    /**
     * Returns a Description of the AlarmPoint.
     * This normally returns a full Description of the AlarmPoint in the
     * Locale. If Locale is <code><b>null</b></code>, then the
     * default Locale is used.
     *
     * @param locale The locale to return the description in, or null to use default locale.
     *
     * @return a human-readable description of the AlarmPoint in the given locale.
     */
    String description( Locale locale );

    /**
     * The {@link AlarmClass} of the AlarmPoint.
     *
     * The {@link AlarmClass} indicates the urgency of {@link AlarmEvent}s emitted from the AlarmPoint. The property
     * has {@link UseDefaults} annotation so that the application developer can set the default during assembly.
     * The default {@link org.qi4j.bootstrap.Assembler} in this library defaults alarms to {@link AlarmClass} <b>B</b>.
     *
     * @return the property instance that contains the {@link AlarmClass}.
     */
    @UseDefaults
    Property<AlarmClass> alarmClass();

    /**
     * The {@link AlarmCategory} of this AlarmPoint.
     *
     * AlarmCategory is used to group Alarms together, which can be used to forward {@link AlarmEvent} to different
     * destinations, produce reports for different target audiences or separation of aggregation.
     *
     * @return the property instance that contains the {@link AlarmCategory}.
     */
    Property<AlarmCategory> category();

    /**
     * The AlarmState is an internal type, used inside the AlarmPoint for all state that needs to be persisted on disk
     * and/or transferred across networks.
     */
    interface AlarmState
    {
        @AlarmNameFormat
        Property<String> systemName();

        @Optional
        Property<String> description();

        @UseDefaults
        Property<Map<String, String>> attributes();

        Property<AlarmStatus> currentStatus();
    }

    abstract class AlarmPointMixin
        implements AlarmPoint
    {
        @Service
        private AlarmModel model;

        @Service
        private AlarmSystem alarmSystem;

        @This
        private AlarmPoint me;

        @This
        private AlarmState state;

        @This
        private AlarmHistory history;

        @Override
        public void setAttribute( String name, String value )
        {
            Map<String, String> properties = state.attributes().get();
            if( value == null )
            {
                properties.remove( name );
            }
            else
            {
                properties.put( name, value );
            }
            state.attributes().set( properties );
        }

        @Override
        public String attribute( String name )
        {
            return state.attributes().get().get( name );
        }

        @Override
        public List<String> attributeNames()
        {
            ArrayList<String> result = new ArrayList<String>();
            for( String name : state.attributes().get().keySet() )
            {
                result.add( name );
            }
            return result;
        }

        private void fireAlarm( AlarmEvent event )
        {
            // TODO: Should possibly just be delegated to AlarmSystem
            for( AlarmListener listener : alarmSystem.alarmListeners() )
            {
                try
                {
                    listener.alarmFired( event );
                }
                catch( Exception e )
                {

                    // ignore.
                }
            }
        }

        @Override
        public String toString()
        {
            return "AlarmPoint[" + name() + " : " + state.currentStatus().get().name(null)
                   + "  : " + descriptionInDefaultLocale() + "]";
        }

        @Override
        public void trigger( String trigger )
        {
            AlarmEvent event;
            synchronized( this )
            {
                event = model.evaluate( me, trigger );
                if( event == null )
                {
                    return;
                }
                state.currentStatus().set( event.newStatus().get() );
                history.addEvent( event, trigger );
            }
            fireAlarm( event );
        }

        @Override
        public void activate()
        {
            trigger( AlarmPoint.TRIGGER_ACTIVATE );
        }

        @Override
        public void deactivate()
        {
            trigger( AlarmPoint.TRIGGER_DEACTIVATE );
        }

        @Override
        public void acknowledge()
        {
            trigger( AlarmPoint.TRIGGER_ACKNOWLEDGE );
        }

        @Override
        public AlarmHistory history()
        {
            return history;
        }

        @Override
        public AlarmStatus currentStatus()
        {
            return state.currentStatus().get();
        }

        /**
         * Returns the Name of the AlarmPoint.
         * This normally returns the human readable technical name of the AlarmPoint.
         */
        @Override
        public String name()
        {
            return state.systemName().get();
        }

        /**
         * Returns a Description of the AlarmPoint.
         * This normally returns a full Description of the AlarmPoint in the
         * default Locale.
         */
        @Override
        public String descriptionInDefaultLocale()
        {
            return description( null );
        }

        /**
         * Returns a Description of the AlarmPoint.
         * This normally returns a full Description of the AlarmPoint in the
         * Locale. If Locale is <code><b>null</b></code>, then the
         * default Locale is used.
         */
        @Override
        public String description( Locale locale )
        {
            if( locale == null )
            {
                locale = Locale.getDefault();
            }
            ResourceBundle rb = ResourceBundle.getBundle( "org.qi4j.library.alarm.user.AlarmDescriptions", locale );
            return rb.getString( name() );
        }

        @Override
        public void updateCondition( boolean condition )
        {
            String trig = model.computeTrigger( state.currentStatus().get(), condition );
            if( trig != null )
            {
                trigger( trig );
            }
        }

        @Override
        public boolean currentCondition()
        {
            return model.computeCondition( state.currentStatus().get() );
        }
    }
}
