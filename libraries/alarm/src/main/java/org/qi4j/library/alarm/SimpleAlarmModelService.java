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
     * The Simple AlarmPoint Model is centered around the Normal and Activated.
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
        private static final List<String> ALARM_TRIGGERS;
        private static final List<String> ALARM_STATUSES;

        static
        {
            List<String> list1 = new ArrayList<String>();

            list1.add( AlarmPoint.TRIGGER_ACTIVATE );
            list1.add( AlarmPoint.TRIGGER_DEACTIVATE );
            ALARM_TRIGGERS = Collections.unmodifiableList( list1 );

            List<String> list2 = new ArrayList<String>();
            list2.add( AlarmPoint.STATUS_NORMAL );
            list2.add( AlarmPoint.STATUS_ACTIVATED );
            ALARM_STATUSES = Collections.unmodifiableList( list2 );
        }

        @Structure
        private ValueBuilderFactory vbf;

        static ResourceBundle getResourceBundle( Locale locale )
        {
            if( locale == null )
            {
                locale = Locale.getDefault();
            }
            ClassLoader cl = SimpleAlarmModelMixin.class.getClassLoader();
            return ResourceBundle.getBundle( MODEL_BUNDLE_NAME, locale, cl );
        }


        /**
         * Returns the Name of the AlarmModel.
         * This normally returns the human readable technical name of
         * the AlarmModel.
         *
         * @return The system name of this AlarmPoint Model.
         */
        @Override
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
         * @param locale The locale for which the description is wanted.
         *
         * @return the description of th
         */
        @Override
        public String modelDescription( Locale locale )
        {
            ResourceBundle rb = getResourceBundle( locale );
            return rb.getString( "MODEL_DESCRIPTION_SIMPLE" );
        }

        @Override
        public List<String> statusList()
        {
            return ALARM_STATUSES;
        }

        /**
         * Execute the required changes upon an AlarmTrigger.
         * The AlarmSystem calls this method, for the AlarmStatus
         * in the the AlarmPoint to be updated, as well as an AlarmEvent
         * to be created.
         *
         * @param trigger the AlarmTrigger that was used.
         */
        @Override
        public AlarmEvent evaluate( AlarmPoint alarm, String trigger )
        {
            if( trigger.equals( AlarmPoint.TRIGGER_ACTIVATE ) )
            {
                return activation( alarm );
            }
            else if( trigger.equals( AlarmPoint.TRIGGER_DEACTIVATE ) )
            {
                return deactivation( alarm );
            }
            else
            {
                throw new IllegalArgumentException( "'" + trigger + "' is not supported by this AlarmModel." );
            }
        }

        /**
         * Returns all the supported AlarmPoint triggers.
         */
        @Override
        public List<String> alarmTriggers()
        {
            return ALARM_TRIGGERS;
        }

        @Override
        public String computeTrigger( AlarmStatus status, boolean condition )
        {
            if( condition )
            {
                if( AlarmPoint.STATUS_NORMAL.equals( status.name(null) ) )
                {
                    return AlarmPoint.TRIGGER_ACTIVATE;
                }
            }
            else
            {
                if( AlarmPoint.STATUS_ACTIVATED.equals( status.name(null) ) )
                {
                    return AlarmPoint.TRIGGER_DEACTIVATE;
                }
            }
            return null;
        }

        @Override
        public boolean computeCondition( AlarmStatus status )
        {
            return status.name(null).equals( AlarmPoint.STATUS_ACTIVATED );
        }

        /**
         * StateMachine change for activate trigger.
         *
         * @param alarm the AlarmPoint that the activation is done on.
         *
         * @return the resulting AlarmEvent of the activation, or null if no change.
         */
        private AlarmEvent activation( AlarmPoint alarm )
        {
            AlarmStatus oldStatus = alarm.currentStatus();
            if( oldStatus.name(null).equals( AlarmPoint.STATUS_NORMAL ) )
            {
                AlarmStatus newStatus = createStatus( AlarmPoint.STATUS_ACTIVATED );
                return createEvent( (Identity) alarm, oldStatus, newStatus, AlarmPoint.EVENT_ACTIVATION );
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
        private AlarmEvent deactivation( AlarmPoint alarm )
        {
            AlarmStatus oldStatus = alarm.currentStatus();
            if( oldStatus.name(null).equals( AlarmPoint.STATUS_ACTIVATED ) )
            {
                AlarmStatus newStatus = createStatus( AlarmPoint.STATUS_NORMAL );
                return createEvent( (Identity) alarm, oldStatus, newStatus, AlarmPoint.EVENT_DEACTIVATION );
            }
            return null;
        }

        private AlarmStatus createStatus( String status )
        {
            ValueBuilder<AlarmStatus> builder = vbf.newValueBuilder( AlarmStatus.class );
            AlarmStatus.State prototype = builder.prototypeFor(AlarmStatus.State.class);
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
