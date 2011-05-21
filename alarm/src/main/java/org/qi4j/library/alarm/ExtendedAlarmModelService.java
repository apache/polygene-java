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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

/**
 * <p>
 * The Standard Model is centered around the Normal, Activated, Acknowledged
 * Deactivated, Reactivated, Blocked and Disabled states, and the triggers "activate", "deactivate",
 * "acknowledge", "block", "unblock", "enable" and "disable". The following matrix details the
 * resulting grid;
 * <p>
 * <table>
 * <tr><th>Initial State</th>* <th>Trigger</th><th>Resulting State</th><th>Event Generated</th></tr>
 * <tr><td>Normal</td><td>activate</td><td>Activated</td><td>activation</td></tr>
 * <tr><td>Normal</td><td>deactivate</td><td>Normal</td><td>-</td></tr>
 * <tr><td>Normal</td><td>acknowledge</td><td>Normal</td><td>-</td></tr>
 * <tr><td>Normal</td><td>block</td><td>Blocked</td><td>block</td></tr>
 * <tr><td>Normal</td><td>unblock</td><td>Normal</td><td>-</td></tr>
 * <tr><td>Activated</td><td>activate</td><td>Activated</td><td>-</td></tr>
 * <tr><td>Activated</td><td>deactivate</td><td>Deactivated</td><td>deactivation</td></tr>
 * <tr><td>Activated</td><td>acknowledge</td><td>Acknowledged</td><td>acknowledge</td></tr>
 * <tr><td>Activated</td><td>block</td><td>Blocked</td><td>block</td></tr>
 * <tr><td>Activated</td><td>unblock</td><td>Activated</td><td>-</td></tr>
 * <tr><td>Deactivated</td><td>activate</td><td>Activated</td><td>activation</td></tr>
 * <tr><td>Deactivated</td><td>deactivate</td><td>Deativated</td><td>-</td></tr>
 * <tr><td>Deactivated</td><td>acknowledge</td><td>Normal</td><td>acknowledge</td></tr>
 * <tr><td>Deactivated</td><td>block</td><td>Blocked</td><td>block</td></tr>
 * <tr><td>Deactivated</td><td>unblock</td><td>Deactivated</td><td>-</td></tr>
 * <tr><td>Acknowledged</td><td>activate</td><td>Acknowledged</td><td>-</td></tr>
 * <tr><td>Acknowledged</td><td>deactivate</td><td>Normal</td><td>deactivation</td></tr>
 * <tr><td>Acknowledged</td><td>acknowledge</td><td>Acknowledged</td><td>-</td></tr>
 * <tr><td>Acknowledged</td><td>block</td><td>Blocked</td><td>block</td></tr>
 * <tr><td>Acknowledged</td><td>unblock</td><td>Acknowledged</td><td>-</td></tr>
 * <tr><td>Blocked</td><td>activate</td><td>Blocked</td><td>-</td></tr>
 * <tr><td>Blocked</td><td>deactivate</td><td>Blocked</td><td>-</td></tr>
 * <tr><td>Blocked</td><td>acknowledge</td><td>Blocked</td><td>-</td></tr>
 * <tr><td>Blocked</td><td>block</td><td>Blocked</td><td>-</td></tr>
 * <tr><td>Blocked</td><td>unblock</td><td>Normal</td><td>unblock</td></tr>
 * <tr><td>Normal</td><td>disable</td><td>Disabled</td><td>disable</td></tr>
 * <tr><td>Blocked</td><td>disable</td><td>Disabled</td><td>disable</td></tr>
 * <tr><td>Deactivated</td><td>disable</td><td>Disabled</td><td>disable</td></tr>
 * <tr><td>Acknowledged</td><td>disable</td><td>Disabled</td><td>disable</td></tr>
 * <tr><td>Activated</td><td>disable</td><td>Disabled</td><td>disable</td></tr>
 * <tr><td>Reactivated</td><td>disable</td><td>Disabled</td><td>disable</td></tr>
 * <tr><td>Disabled</td><td>disable</td><td>Disabled</td><td>-</td></tr>
 * <tr><td>Normal</td><td>enable</td><td>Normal</td><td>-</td></tr>
 * <tr><td>Blocked</td><td>enable</td><td>Blocked</td><td>-</td></tr>
 * <tr><td>Deactivated</td><td>enable</td><td>Deactivated</td><td>-</td></tr>
 * <tr><td>Acknowledged</td><td>enable</td><td>Acknowledged</td><td>-</td></tr>
 * <tr><td>Activated</td><td>enable</td><td>Activated</td><td>-</td></tr>
 * <tr><td>Reactivated</td><td>enable</td><td>Reactivated</td><td>-</td></tr>
 * <tr><td>Disabled</td><td>enable</td><td>Normal</td><td>enable</td></tr>
 *
 * </table>
 */

@Mixins(ExtendedAlarmModelService.ExtendedAlarmModelMixin.class)
public interface ExtendedAlarmModelService
    extends AlarmModel, ServiceComposite
{
    class ExtendedAlarmModelMixin
        implements AlarmModel
    {

        private static String MODEL_BUNDLE_NAME = "org.qi4j.library.alarm.extended.AlarmResources";

        private final static List<String> TRIGGER_LIST;

        private static final List<String> STATUS_LIST;

        @Structure
        private ValueBuilderFactory vbf;

        static
        {
            List<String> list = new ArrayList<String>();
            list.add( Alarm.STATUS_NORMAL );
            list.add( Alarm.STATUS_ACTIVATED );
            list.add( Alarm.STATUS_DEACTIVATED );
            list.add( Alarm.STATUS_ACKNOWLEDGED );
            list.add( Alarm.STATUS_REACTIVATED );
            list.add( Alarm.STATUS_BLOCKED );
            list.add( Alarm.STATUS_DISABLED );
            STATUS_LIST = Collections.unmodifiableList( list );

            list.clear();
            list.add( Alarm.TRIGGER_ACTIVATE );
            list.add( Alarm.TRIGGER_DEACTIVATE );
            list.add( Alarm.TRIGGER_ACKNOWLEDGE );
            list.add( Alarm.TRIGGER_BLOCK );
            list.add( Alarm.TRIGGER_UNBLOCK );
            list.add(Alarm.TRIGGER_DISABLE);
            list.add(Alarm.TRIGGER_ENABLE );
            TRIGGER_LIST = Collections.unmodifiableList( list );
        }

        static ResourceBundle getResourceBundle( Locale locale )
        {
            if( locale == null )
            {
                locale = Locale.getDefault();
            }
            ClassLoader cl = ExtendedAlarmModelMixin.class.getClassLoader();
            return ResourceBundle.getBundle( MODEL_BUNDLE_NAME, locale, cl );
        }

        /**
         * Returns the Name of the AlarmModel.
         * This normally returns the human readable technical name of
         * the AlarmModel.
         *
         * @return The system name of this alarm model.
         */
        @Override
        public String modelName()
        {
            return "org.qi4j.library.alarm.model.extended";
        }

        /**
         * Returns a Description of the AlarmModel in the default Locale.
         * This normally returns a full Description of the AlarmModel in the
         * default Locale.
         *
         * @return the description of the ModelProvider, in the default locale.
         */
        @Override
        public String modelDescription()
        {
            return modelDescription( null );
        }

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
        @Override
        public String modelDescription( Locale locale )
        {
            ResourceBundle rb = getResourceBundle( locale );
            return rb.getString( "MODEL_DESCRIPTION" );
        }

        /**
         * Execute the required changes upon an AlarmTrigger.
         * The AlarmSystem calls this method, for the AlarmStatus
         * in the the Alarm to be updated, as well as an AlarmEvent
         * to be created.
         *
         * @param alarm   the Alarm object to be updated.
         * @param trigger the AlarmTrigger that was used.
         */
        @Override
        public AlarmEvent evaluate( Alarm alarm, String trigger )
        {
            if( trigger.equals( Alarm.TRIGGER_ACTIVATE ) )
            {
                return activation( alarm );
            }
            else if( trigger.equals( Alarm.TRIGGER_DEACTIVATE ) )
            {
                return deactivation( alarm );
            }
            else if( trigger.equals( Alarm.TRIGGER_ACKNOWLEDGE ) )
            {
                return acknowledge( alarm );
            }
            else if( trigger.equals( Alarm.TRIGGER_BLOCK ) )
            {
                return block( alarm );
            }
            else if( trigger.equals( Alarm.TRIGGER_UNBLOCK ) )
            {
                return unblock( alarm );
            }
            else if( trigger.equals( Alarm.TRIGGER_ENABLE ) )
            {
                return enable( alarm );
            }
            else if( trigger.equals( Alarm.TRIGGER_DISABLE ) )
            {
                return disable( alarm );
            }
            else
            {
                throw new IllegalArgumentException( "'" + trigger + "' is not supported by this AlarmModel." );
            }
        }

        /**
         * Returns all the supported Alarm triggers.
         *
         * @return The Alarm triggers that this AlarmModel supports.
         */
        public List<String> alarmTriggers()
        {
            return TRIGGER_LIST;
        }

        @Override
        public List<String> statusList()
        {
            return STATUS_LIST;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String computeTrigger( AlarmStatus status, boolean condition )
        {
            if( condition )
            {
                if( ( status.name().get().equals( Alarm.STATUS_DEACTIVATED ) ) ||
                    ( status.name().get().equals( Alarm.STATUS_NORMAL ) ) )
                {
                    return Alarm.TRIGGER_ACTIVATE;
                }
            }
            else
            {
                if( ( status.name().get().equals( Alarm.STATUS_ACTIVATED ) ) ||
                    ( status.name().get().equals( Alarm.STATUS_REACTIVATED ) ) ||
                    ( status.name().get().equals( Alarm.STATUS_ACKNOWLEDGED ) ) )
                {
                    return Alarm.TRIGGER_DEACTIVATE;
                }
            }
            return null;
        }

        public boolean computeCondition( AlarmStatus status )
        {
            return ( status.name().get().equals( Alarm.STATUS_ACTIVATED ) ) ||
                   ( status.name().get().equals( Alarm.STATUS_REACTIVATED ) ) ||
                   ( status.name().get().equals( Alarm.STATUS_ACKNOWLEDGED ) );
        }

        /**
         * StateMachine change for activate trigger.
         *
         * @param alarm the alarm that is being triggered.
         *
         * @return The event to be fired on activation.
         */
        private AlarmEvent activation( Alarm alarm )
        {
            AlarmStatus status = alarm.currentStatus();
            if( ( status.name().get().equals( Alarm.STATUS_NORMAL ) ) ||
                ( status.name().get().equals( Alarm.STATUS_DEACTIVATED ) ) )
            {
                AlarmStatus newStatus = createStatus( Alarm.STATUS_ACTIVATED );
                return createEvent( ( (Identity) alarm ), status, newStatus, Alarm.EVENT_ACTIVATION );
            }
            return null;
        }

        /**
         * StateMachine change for activate trigger.
         *
         * @param alarm the alarm that is being triggered.
         *
         * @return The event to be fired on deactivation.
         */
        private AlarmEvent deactivation( Alarm alarm )
        {
            AlarmStatus status = alarm.currentStatus();
            if( status.name().get().equals( Alarm.STATUS_ACKNOWLEDGED ) )
            {
                AlarmStatus newStatus = createStatus( Alarm.STATUS_NORMAL );
                return createEvent( ( (Identity) alarm ), status, newStatus, Alarm.EVENT_DEACTIVATION );
            }
            else if( status.name().get().equals( Alarm.STATUS_ACTIVATED ) )
            {
                AlarmStatus newStatus = createStatus( Alarm.STATUS_DEACTIVATED );
                return createEvent( ( (Identity) alarm ), status, newStatus, Alarm.EVENT_DEACTIVATION );
            }
            return null;
        }

        /**
         * StateMachine change for activate trigger.
         *
         * @param alarm the alarm that is being triggered.
         *
         * @return The event to be fired on acknowledge.
         */
        private AlarmEvent acknowledge( Alarm alarm )
        {
            AlarmStatus status = alarm.currentStatus();
            if( status.name().get().equals( Alarm.STATUS_DEACTIVATED ) )
            {
                AlarmStatus newStatus = createStatus( Alarm.STATUS_NORMAL );
                return createEvent( ( (Identity) alarm ), status, newStatus, Alarm.EVENT_ACKNOWLEDGEMENT );
            }
            else if( status.name().get().equals( Alarm.STATUS_ACTIVATED ) )
            {
                AlarmStatus newStatus = createStatus( Alarm.STATUS_ACKNOWLEDGED );
                return createEvent( ( (Identity) alarm ), status, newStatus, Alarm.EVENT_ACKNOWLEDGEMENT );
            }
            return null;
        }

        /**
         * StateMachine change for block trigger.
         *
         * @param alarm the alarm that is being triggered.
         *
         * @return The event to be fired on acknowledge.
         */
        private AlarmEvent block( Alarm alarm )
        {
            AlarmStatus status = alarm.currentStatus();
            if( status.name().get().equals( Alarm.STATUS_BLOCKED ) ||
                status.name().get().equals( Alarm.STATUS_DISABLED ) )
            {
                return null;
            }
            AlarmStatus newStatus = createStatus( Alarm.STATUS_BLOCKED );
            return createEvent( ( (Identity) alarm ), status, newStatus, Alarm.EVENT_BLOCKING );
        }

        /**
         * StateMachine change for unblock trigger.
         *
         * @param alarm the alarm that is being triggered.
         *
         * @return The event to be fired on acknowledge.
         */
        private AlarmEvent unblock( Alarm alarm )
        {
            AlarmStatus status = alarm.currentStatus();
            if( status.name().get().equals( Alarm.STATUS_BLOCKED ) )
            {
                AlarmStatus newStatus = createStatus( Alarm.STATUS_NORMAL );
                return createEvent( ( (Identity) alarm ), status, newStatus, Alarm.EVENT_UNBLOCKING );
            }
            return null;
        }

        /**
         * StateMachine change for disable trigger.
         *
         * @param alarm the alarm that is being triggered.
         *
         * @return The event to be fired on acknowledge.
         */
        private AlarmEvent disable( Alarm alarm )
        {
            AlarmStatus status = alarm.currentStatus();
            if( status.name().get().equals( Alarm.STATUS_DISABLED ) )
            {
                return null;
            }
            AlarmStatus newStatus = createStatus( Alarm.STATUS_DISABLED );
            return createEvent( ( (Identity) alarm ), status, newStatus, Alarm.EVENT_DISABLING );
        }

        /**
         * StateMachine change for unblock trigger.
         *
         * @param alarm the alarm that is being triggered.
         *
         * @return The event to be fired on acknowledge.
         */
        private AlarmEvent enable( Alarm alarm )
        {
            AlarmStatus status = alarm.currentStatus();
            if( status.name().get().equals( Alarm.STATUS_DISABLED ) )
            {
                AlarmStatus newStatus = createStatus( Alarm.STATUS_NORMAL );
                return createEvent( ( (Identity) alarm ), status, newStatus, Alarm.EVENT_ENABLING );
            }
            return null;
        }

        private AlarmStatus createStatus( String status )
        {
            ValueBuilder<AlarmStatus> builder = vbf.newValueBuilder( AlarmStatus.class );
            AlarmStatus prototype = builder.prototype();
            prototype.name().set( status );
            prototype.creationDate().set( new Date() );
            return builder.newInstance();
        }

        private AlarmEvent createEvent( Identity alarmId,
                                        AlarmStatus oldStatus,
                                        AlarmStatus newStatus,
                                        String eventSystemName
        )
        {
            ValueBuilder<AlarmEvent> builder = vbf.newValueBuilder( AlarmEvent.class );
            AlarmEvent prototype = builder.prototype();
            prototype.alarmIdentity().set( alarmId.identity().get() );
            prototype.eventTime().set( new Date() );
            prototype.newStatus().set( newStatus );
            prototype.oldStatus().set( oldStatus );
            prototype.systemName().set( eventSystemName );
            return builder.newInstance();
        }
    }
}
