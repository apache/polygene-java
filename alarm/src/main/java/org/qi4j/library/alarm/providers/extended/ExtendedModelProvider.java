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

import java.util.Locale;
import java.util.ResourceBundle;
import org.qi4j.library.alarm.Alarm;
import org.qi4j.library.alarm.AlarmEvent;
import org.qi4j.library.alarm.AlarmModelProvider;
import org.qi4j.library.alarm.AlarmState;
import org.qi4j.library.alarm.AlarmTriggerException;

public class ExtendedModelProvider
    implements AlarmModelProvider
{

    private static String MODEL_BUNDLE_NAME = "org.qi4j.library.alarm.providers.extended.AlarmResources";

    private final String[] ALARM_TRIGGERS =
        {
            Alarm.TRIGGER_ACTIVATION, Alarm.TRIGGER_DEACTIVATION, Alarm.TRIGGER_ACKNOWLEDGE, Alarm.TRIGGER_BLOCK,
            Alarm.TRIGGER_UNBLOCK, Alarm.TRIGGER_DISABLE, Alarm.TRIGGER_ENABLE 
        };

    static ResourceBundle getResourceBundle( Locale locale )
    {
        if( locale == null )
        {
            locale = Locale.getDefault();
        }
        ClassLoader cl = ExtendedModelProvider.class.getClassLoader();
        return ResourceBundle.getBundle( MODEL_BUNDLE_NAME, locale, cl );
    }

    /**
     * Returns the Name of the AlarmModel.
     * This normally returns the human readable technical name of
     * the AlarmModel.
     */
    public String getName()
    {
        return "org.qi4j.library.alarm.model.extended";
    }

    /**
     * Returns a Description of the AlarmModel in the default Locale.
     * This normally returns a full Description of the AlarmModel in the
     * default Locale.
     *
     * @return the description of the ModelProvider.
     */
    public String getDescription()
    {
        return getDescription( null );
    }

    /**
     * Returns a Description of the AlarmModel.
     * This normally returns a full Description of the AlarmModel in the
     * Locale. If Locale is <code><b>null</b></code>, then the
     * default Locale is used.
     */
    public String getDescription( Locale locale )
    {
        ResourceBundle rb = getResourceBundle( locale );
        return rb.getString( "MODEL_DESCRIPTION" );
    }

    public AlarmState createInitialState()
    {
        return new NormalState();
    }

    /**
     * Execute the required changes upon an AlarmTrigger.
     * The AlarmService calls this method, for the AlarmState
     * in the the Alarm to be updated, as well as an AlarmEvent
     * to be created.
     *
     * @param source  the object that triggered the standard.
     * @param alarm   the Alarm object to be updated.
     * @param trigger the AlarmTrigger that was used.
     */
    public AlarmEvent executeStateChange( Object source, Alarm alarm, String trigger )
        throws AlarmTriggerException
    {
        if( trigger.equals( Alarm.TRIGGER_ACTIVATION ) )
        {
            return activation( source, alarm );
        }
        else if( trigger.equals( Alarm.TRIGGER_DEACTIVATION ) )
        {
            return deactivation( source, alarm );
        }
        else if( trigger.equals( Alarm.TRIGGER_ACKNOWLEDGE ) )
        {
            return acknowledge( source, alarm );
        }
        else if( trigger.equals( Alarm.TRIGGER_BLOCK ) )
        {
            return block( source, alarm );
        }
        else if( trigger.equals( Alarm.TRIGGER_UNBLOCK ) )
        {
            return unblock( source, alarm );
        }
        else if( trigger.equals( Alarm.TRIGGER_ENABLE ) )
        {
            return enable( source, alarm );
        }
        else if( trigger.equals( Alarm.TRIGGER_DISABLE ) )
        {
            return disable( source, alarm );
        }
        else
        {
            throw new AlarmTriggerException( "'" + trigger + "' is not supported by this AlarmModel." );
        }
    }

    /**
     * Returns all the supported Alarm triggers.
     */
    public String[] getAlarmTriggers()
    {
        return ALARM_TRIGGERS;
    }

    public String computeTrigger( AlarmState state, boolean condition )
    {
        if( condition )
        {
            if( ( state instanceof DeactivatedState ) ||
                ( state instanceof NormalState ) )
            {
                return Alarm.TRIGGER_ACTIVATION;
            }
        }
        else
        {
            if( ( state instanceof ActivatedState ) ||
                ( state instanceof ReactivatedState ) ||
                ( state instanceof AcknowledgedState ) )
            {
                return Alarm.TRIGGER_DEACTIVATION;
            }
        }
        return null;
    }

    public boolean computeCondition( AlarmState state )
    {
        return ( state instanceof ActivatedState ) ||
               ( state instanceof ReactivatedState ) ||
               ( state instanceof AcknowledgedState );
    }

    /**
     * StateMachine change for activate trigger.
     *
     * @param source the object that is triggering the activation.
     * @param alarm  the alarm that is being triggered.
     *
     * @return The event to be fired on activation.
     */
    private AlarmEvent activation( Object source, Alarm alarm )
    {
        AlarmState oldState = alarm.getState();
        if( ( oldState instanceof NormalState ) ||
            ( oldState instanceof DeactivatedState ) )
        {
            AlarmState newState = new ActivatedState();
            long time = System.currentTimeMillis();
            return new ActivationEvent( source, alarm, oldState, newState, time );
        }
        return null;
    }

    /**
     * StateMachine change for activate trigger.
     *
     * @param source the object that is triggering the deactivation.
     * @param alarm  the alarm that is being triggered.
     *
     * @return The event to be fired on deactivation.
     */
    private AlarmEvent deactivation( Object source, Alarm alarm )
    {
        AlarmState oldState = alarm.getState();
        long time = System.currentTimeMillis();
        if( oldState instanceof AcknowledgedState )
        {
            AlarmState newState = new NormalState();
            return new DeactivationEvent( source, alarm, oldState, newState, time );
        }
        else if( oldState instanceof ActivatedState )
        {
            AlarmState newState = new DeactivatedState();
            return new DeactivationEvent( source, alarm, oldState, newState, time );
        }
        return null;
    }

    /**
     * StateMachine change for activate trigger.
     *
     * @param source the object that is triggering the acknowledge.
     * @param alarm  the alarm that is being triggered.
     *
     * @return The event to be fired on acknowledge.
     */
    private AlarmEvent acknowledge( Object source, Alarm alarm )
    {
        AlarmState oldState = alarm.getState();
        long time = System.currentTimeMillis();
        if( oldState instanceof DeactivatedState )
        {
            AlarmState newState = new NormalState();
            return new AcknowledgeEvent( source, alarm, oldState, newState, time );
        }
        else if( oldState instanceof ActivatedState )
        {
            AlarmState newState = new AcknowledgedState();
            return new AcknowledgeEvent( source, alarm, oldState, newState, time );
        }
        return null;
    }

    /**
     * StateMachine change for block trigger.
     *
     * @param source the object that is triggering the acknowledge.
     * @param alarm  the alarm that is being triggered.
     *
     * @return The event to be fired on acknowledge.
     */
    private AlarmEvent block( Object source, Alarm alarm )
    {
        AlarmState oldState = alarm.getState();
        long time = System.currentTimeMillis();
        if( oldState instanceof BlockedState ||
            oldState instanceof DisabledState )
        {
            return null;
        }
        AlarmState newState = new BlockedState();
        return new BlockEvent( source, alarm, oldState, newState, time );
    }

    /**
     * StateMachine change for unblock trigger.
     *
     * @param source the object that is triggering the acknowledge.
     * @param alarm  the alarm that is being triggered.
     *
     * @return The event to be fired on acknowledge.
     */
    private AlarmEvent unblock( Object source, Alarm alarm )
    {
        AlarmState oldState = alarm.getState();
        long time = System.currentTimeMillis();
        if( oldState instanceof BlockedState )
        {
            AlarmState newState = new NormalState();
            return new UnblockEvent( source, alarm, oldState, newState, time );
        }
        return null;
    }

    /**
     * StateMachine change for disable trigger.
     *
     * @param source the object that is triggering the acknowledge.
     * @param alarm  the alarm that is being triggered.
     *
     * @return The event to be fired on acknowledge.
     */
    private AlarmEvent disable( Object source, Alarm alarm )
    {
        AlarmState oldState = alarm.getState();
        long time = System.currentTimeMillis();
        if( oldState instanceof DisabledState )
        {
            return null;
        }
        AlarmState newState = new DisabledState();
        return new DisableEvent( source, alarm, oldState, newState, time );
    }

    /**
     * StateMachine change for unblock trigger.
     *
     * @param source the object that is triggering the acknowledge.
     * @param alarm  the alarm that is being triggered.
     *
     * @return The event to be fired on acknowledge.
     */
    private AlarmEvent enable( Object source, Alarm alarm )
    {
        AlarmState oldState = alarm.getState();
        long time = System.currentTimeMillis();
        if( oldState instanceof DisabledState )
        {
            AlarmState newState = new NormalState();
            return new EnableEvent( source, alarm, oldState, newState, time );
        }
        return null;
    }
}
