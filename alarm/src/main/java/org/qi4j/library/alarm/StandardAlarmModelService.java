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
 * The Standard Alarm Model is centered around the Normal, Activated, Acknowledged
 * and Deactivated states, and the triggers "activate", "deactivate",
 * and "acknowledge". The following matrix details the resulting grid;
 * <p>
 * <table>
 * <tr><th>Initial State</th><th>Trigger</th><th>Resulting State</th><th>Event Generated</th></tr>
 * <tr><td>Normal</td><td>activate</td><td>Activated</td><td>activation</td></tr>
 * <tr><td>Normal</td><td>deactivate</td><td>Normal</td><td>-</td></tr>
 * <tr><td>Normal</td><td>acknowledge</td><td>Normal</td><td>-</td></tr>
 * <tr><td>Activated</td><td>activate</td><td>Activated</td><td>-</td></tr>
 * <tr><td>Activated</td><td>deactivate</td><td>Deactivated</td><td>deactivation</td></tr>
 * <tr><td>Activated</td><td>acknowledge</td><td>Acknowledged</td><td>acknowledge</td></tr>
 * <tr><td>Deactivated</td><td>activate</td><td>Activated</td><td>activation</td></tr>
 * <tr><td>Deactivated</td><td>deactivate</td><td>Deativated</td><td>-</td></tr>
 * <tr><td>Deactivated</td><td>acknowledge</td><td>Normal</td><td>acknowledge</td></tr>
 * <tr><td>Acknowledged</td><td>activate</td><td>Acknowledged</td><td>-</td></tr>
 * <tr><td>Acknowledged</td><td>deactivate</td><td>Normal</td><td>deactivation</td></tr>
 * <tr><td>Acknowledged</td><td>acknowledge</td><td>Acknowledged</td><td>-</td></tr>
 * </table>
 */

@Mixins(StandardAlarmModelService.StandardAlarmModelMixin.class)
public interface StandardAlarmModelService extends AlarmModel, ServiceComposite
{
    class StandardAlarmModelMixin
        implements AlarmModel
    {

        static String MODEL_BUNDLE_NAME = "org.qi4j.library.alarm.standard.AlarmResources";

        private static final List<String> TRIGGER_LIST;

        private static final List<String> STATUS_LIST;

        static
        {
            List<String> list1 = new ArrayList<String>();
            list1.add( Alarm.STATUS_NORMAL );
            list1.add( Alarm.STATUS_ACTIVATED );
            list1.add( Alarm.STATUS_DEACTIVATED );
            list1.add( Alarm.STATUS_ACKNOWLEDGED );
            STATUS_LIST = Collections.unmodifiableList( list1 );

            List<String> list2 = new ArrayList<String>();
            list2.add( Alarm.TRIGGER_ACTIVATE );
            list2.add( Alarm.TRIGGER_DEACTIVATE );
            list2.add( Alarm.TRIGGER_ACKNOWLEDGE );
            TRIGGER_LIST = Collections.unmodifiableList( list2 );
        }

        @Structure
        private ValueBuilderFactory vbf;

        static ResourceBundle getResourceBundle( Locale locale )
        {
            if( locale == null )
            {
                locale = Locale.getDefault();
            }
            ClassLoader cl = StandardAlarmModelMixin.class.getClassLoader();
            return ResourceBundle.getBundle( MODEL_BUNDLE_NAME, locale, cl );
        }

        /**
         * Returns the Name of the AlarmModel.
         * This normally returns the human readable technical name of
         * the AlarmModel.
         */
        public String modelName()
        {
            return "org.qi4j.library.alarm.model.standard";
        }

        /**
         * Returns a Description of the AlarmModel in the default Locale.
         * This normally returns a full Description of the AlarmModel in the
         * default Locale.
         *
         * @return the description of the ModelProvider.
         */
        public String modelDescription()
        {
            return modelDescription( null );
        }

        /**
         * Returns a Description of the AlarmModel.
         * This normally returns a full Description of the AlarmModel in the
         * Locale. If Locale is <code><b>null</b></code>, then the
         * default Locale is used.
         */
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
            else
            {
                throw new IllegalArgumentException( "'" + trigger + "' is not supported by this AlarmModel." );
            }
        }

        /**
         * Returns all the supported Alarm triggers.
         */
        public List<String> alarmTriggers()
        {
            return TRIGGER_LIST;
        }

        @Override
        public List<String> statusList()
        {
            return STATUS_LIST;
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
            AlarmStatus oldStatus = alarm.currentStatus();
            if( ( oldStatus.name().get().equals( Alarm.STATUS_NORMAL ) ) ||
                ( oldStatus.name().get().equals( Alarm.STATUS_DEACTIVATED ) ) )
            {
                AlarmStatus newStatus = createStatus( Alarm.STATUS_ACTIVATED );
                return createEvent( ( (Identity) alarm ), oldStatus, newStatus, Alarm.EVENT_ACTIVATION );
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
            AlarmStatus oldStatus = alarm.currentStatus();
            if( oldStatus.name().get().equals( Alarm.STATUS_ACKNOWLEDGED ) )
            {
                AlarmStatus newStatus = createStatus( Alarm.STATUS_NORMAL );
                return createEvent( ( (Identity) alarm ), oldStatus, newStatus, Alarm.EVENT_DEACTIVATION );
            }
            else if( oldStatus.name().get().equals( Alarm.STATUS_ACTIVATED ) )
            {
                AlarmStatus newStatus = createStatus( Alarm.STATUS_DEACTIVATED );
                return createEvent( ( (Identity) alarm ), oldStatus, newStatus, Alarm.EVENT_DEACTIVATION );
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
            AlarmStatus oldStatus = alarm.currentStatus();
            if( oldStatus.name().get().equals( Alarm.STATUS_DEACTIVATED ) )
            {
                AlarmStatus newStatus = createStatus( Alarm.STATUS_NORMAL );
                return createEvent( ( (Identity) alarm ), oldStatus, newStatus, Alarm.EVENT_ACKNOWLEDGEMENT );
            }
            else if( oldStatus.name().get().equals( Alarm.STATUS_ACTIVATED ) )
            {
                AlarmStatus newStatus = createStatus( Alarm.STATUS_ACKNOWLEDGED );
                return createEvent( ( (Identity) alarm ), oldStatus, newStatus, Alarm.EVENT_ACKNOWLEDGEMENT );
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
