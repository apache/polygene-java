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

/**
 * The Service Provider Interface for an AlarmModel.
 * <p>
 * The <code>AlarmModelProvider</code> specifies the behavior of the standard state machine in the AlarmModel.
 * </p>
 * <p>
 * To implement a new AlarmModel, the minimum requirement is to implement this interface, plus any extra
 * AlarmEvents and AlarmStates.
 * </p>
 */
public interface AlarmModelProvider
{

    /**
     * Returns a new instance of the initial State.
     * This method is called when an Alarm is first created.
     *
     * @return a new instance of the initial State.
     */
    AlarmState createInitialState();

    /**
     * Execute the required changes upon an AlarmTrigger.
     *
     * <p>The AlarmService calls this method, for the AlarmState in the the Alarm to be updated, as well as an
     * AlarmEvent to be created.</p>
     *
     * @param source  the object that triggered the standard.
     * @param alarm   the Alarm object to be updated.
     * @param trigger the standard trigger that was used.
     *
     * @return the AlarmEvent resulting from executing the trigger.
     *
     * @throws AlarmTriggerException if the trigger can not be executed, such as the trigger given is not supported by the AlarmModel
     */
    AlarmEvent executeStateChange( Object source, Alarm alarm, String trigger )
        throws AlarmTriggerException;

    /**
     * Returns all the supported Alarm triggers.
     *
     * @return all the supported Alarm triggers.
     */
    String[] getAlarmTriggers();

    /**
     * Returns the Name of the AlarmModel. <p> This normally returns the human readable technical name of the
     * AlarmModel. </p>
     *
     * @return the name of this AlarmModelProvider.
     */
    String getName();

    /**
     * Returns a Description of the AlarmModel.
     * <p> This normally returns a full Description of the AlarmModel in the
     * Locale. If Locale is <code><b>null</b></code>, then the default Locale is used.
     * </p>
     *
     * @param locale the locale the description should be returned in. If null, the default locale will be used.
     *
     * @return a Description of the AlarmModel in the given locale.
     */
    String getDescription( Locale locale );

    /**
     * Computes a trigger if the standard condition changes. <p> This method must return a trigger if the condition
     * changes, otherwise it must return a null, which will not trigger an AlarmEvent. </p> <p> This method is called by
     * the Alarm in the <code>setCondition(boolean condition)</code> method. </p>
     *
     * @param state     the current state of the standard.
     * @param condition the standard condition, such as High Level.
     *
     * @return computed trigger from the AlarmState and the condition.
     */
    String computeTrigger( AlarmState state, boolean condition );

    /**
     * Computes the <i>condition</i> of an AlarmState in this AlarmModel. <p> This method must investigate the
     * AlarmState and determine if that is an standard condition ON or OFF. </p>
     *
     * @param state The AlarmState to determine the standard condition from.
     *
     * @return the condition of an Alarm with the given AlarmState.
     */
    boolean computeCondition( AlarmState state );
}
