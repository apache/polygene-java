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

@Mixins( SimpleAlarmModelService.SimpleAlarmModelMixin.class)
public interface SimpleAlarmModelService extends AlarmModel, ServiceComposite
{
    /**
     * <p>
     * The Simple Alarm Model is centered around the Normal and Activated.
     * The triggers "activate" and "deactivate". The following matrix details the
     * resulting grid;
     * <p>
     * <table>
     * <tr><th>Initial State</th><th>Trigger</th><th>Resulting State</th><th>Event Generated</th></tr>
     * <tr><td>Normal</td><td>activate</td><td>Activated</td><td>activation</td></tr>
     * <tr><td>Normal</td><td>deactivate</td><td>Normal</td><td>-</td></tr>
     * <tr><td>Activated</td><td>activate</td><td>Activated</td><td>-</td></tr>
     * <tr><td>Activated</td><td>deactivate</td><td>Deactivated</td><td>deactivation</td></tr>
     * </table>
     */
    class SimpleAlarmModelMixin
        implements AlarmModel
    {
        static String MODEL_BUNDLE_NAME = "org.qi4j.library.alarm.simple.AlarmResources";

        private static final List<String> ALARM_TRIGGERS;
        private static final List<String> ALARM_STATUSES;

        static
        {
            List<String> list = new ArrayList<String>();

            list.add( Alarm.TRIGGER_ACTIVATE );
            list.add( Alarm.TRIGGER_DEACTIVATE );
            ALARM_TRIGGERS = Collections.unmodifiableList( list );
            list.clear();

            list.add( Alarm.STATUS_NORMAL );
            list.add( Alarm.STATUS_ACTIVATED );
            ALARM_STATUSES = Collections.unmodifiableList( list );
        }

        @Structure
        private ValueBuilderFactory vbf;

        /**
         * Returns the Name of the AlarmModel.
         * This normally returns the human readable technical name of
         * the AlarmModel.
         *
         * @return The system name of this Alarm Model.
         */
        public String modelName()
        {
            return "org.qi4j.library.alarm.model.simple";
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
         *
         * @param locale The locale for which the description is wanted.
         *
         * @return the description of th
         */
        public String modelDescription( Locale locale )
        {
            ResourceBundle rb = ResourceBundle.getBundle( getClass().getName(), locale );
            return rb.getString( "MODEL_DESCRIPTION" );
        }

        @Override
        public List<String> statusList()
        {
            return ALARM_STATUSES;
        }

        /**
         * Execute the required changes upon an AlarmTrigger.
         * The AlarmSystem calls this method, for the AlarmStatus
         * in the the Alarm to be updated, as well as an AlarmEvent
         * to be created.
         *
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
            return ALARM_TRIGGERS;
        }

        @Override
        public String computeTrigger( AlarmStatus status, boolean condition )
        {
            if( condition )
            {
                if( Alarm.STATUS_NORMAL.equals( status.name().get() ) )
                {
                    return Alarm.TRIGGER_ACTIVATE;
                }
            }
            else
            {
                if( Alarm.STATUS_ACTIVATED.equals( status.name().get() ) )
                {
                    return Alarm.TRIGGER_DEACTIVATE;
                }
            }
            return null;
        }

        @Override
        public boolean computeCondition( AlarmStatus status )
        {
            return status.name().get().equals( Alarm.STATUS_ACTIVATED );
        }

        /**
         * StateMachine change for activate trigger.
         *
         * @param alarm the Alarm that the activation is done on.
         *
         * @return the resulting AlarmEvent of the activation, or null if no change.
         */
        private AlarmEvent activation( Alarm alarm )
        {
            AlarmStatus oldStatus = alarm.currentStatus();
            if( oldStatus.name().get().equals( Alarm.STATUS_NORMAL ) )
            {
                AlarmStatus newStatus = createStatus( Alarm.STATUS_ACTIVATED );
                return createEvent( (Identity) alarm, oldStatus, newStatus, Alarm.EVENT_ACTIVATION );
            }
            return null;
        }

        /**
         * StateMachine change for activate trigger.
         *
         * @param alarm the alarm that is causing the activation.
         *
         * @return the resulting AlarmEvent of the activation.
         */
        private AlarmEvent deactivation( Alarm alarm )
        {
            AlarmStatus oldStatus = alarm.currentStatus();
            if( oldStatus.name().get().equals( Alarm.STATUS_ACTIVATED ) )
            {
                AlarmStatus newStatus = createStatus( Alarm.STATUS_NORMAL );
                return createEvent( (Identity) alarm, oldStatus, newStatus, Alarm.EVENT_DEACTIVATION );
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
                                        AlarmStatus newStatus, String eventSystemName
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
