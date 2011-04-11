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

/**
 * Defines the AlarmService interface.
 *
 * <p>
 * The <code>AlarmService</code> is a central registry/handler for all
 * <code>Alarm</code> objects. By registering <code>AlarmListener</code>s
 * to the AlarmService, objects are able to 'ignore' the fact that there
 * are many <code>Alarm</code> objects in the system.
 * </p>
 * <p>
 * Also, new properties registered with the AlarmService will propagate into
 * all existing and future <code>Alarm</code>s, whereas properties at <code>
 * Alarm</code> level is individual to an <code>Alarm</code>.
 * </p>
 * <p>
 * Many different AlarmModels can co-exist in the same application.
 * In fact, every Alarm can have its own AlarmModel, and the AlarmModel
 * can be changed in runtime, for unrivaled flexibility. However, typically
 * the AlarmModel is set in the AlarmService only, and all Alarms will
 * use the default model.
 * </p>
 *
 * @author Niclas Hedhman
 */
public interface AlarmService
{

    /**
     * Returns all the AlarmModels that has been installed.
     *
     * @return all the AlarmModels that has been installed.
     */
    AlarmModel[] alarmModels();

    /**
     * Returns the default AlarmModel.
     *
     * @return the default AlarmModel in this AlarmService.
     */
    AlarmModel defaultAlarmModel();

    /**
     * Sets the default AlarmModel.
     * Changes the default AlarmModel. All Alarms belonging
     * to the old default AlarmModel will be re-assigned to the
     * new default AlarmModel, for easy change of AlarmModels.
     * <p>When the AlarmModel is changed, the Alarms will receieve
     * an event indicating that this has happened, and will
     * reset their states to NormalState, to avoid illegal states.</p>
     *
     * @param model the new default AlarmModel to be used in this service.
     */
    void setDefaultAlarmModel( AlarmModel model );

    /**
     * Returns a list of all Alarms registered to the service.
     * <p>
     * The returned Collection may not be modified in any way. The
     * implementation is free to return a clone, but not required to do
     * so, and may decide to terminate if the collection is modified.
     * </p>
     *
     * @return a list of all Alarms registered to the service.
     */
    List<Alarm> alarmList();

    /**
     * Creates an Alarm with the default AlarmModel.
     *
     * @param name the name of the Alarm to be created.
     *
     * @return the created Alarm with the given name using the default AlarmModel.
     *
     * @throws AlarmCreationException If the alarm could not be created with that name.
     */
    Alarm createAlarm( String name )
        throws AlarmCreationException;

    /**
     * Register AlarmListener to recieve <code>AlarmEvents</code> from all
     * <code>Alarms</code> managed by this <code>AlarmService</code>.
     *
     * @param listener the global listener to be added.
     */
    void addAlarmListener( AlarmListener listener );

    /**
     * Remove the <code>AlarmListener</code> from the <code>AlarmService</code>.
     *
     * @param listener the global listener to be removed.
     */
    void removeAlarmListener( AlarmListener listener );

    /**
     * Returns a list of all AlarmListeners registered to the service.
     *
     * @return a list of all AlarmListeners registered to the service.
     */
    List alarmListeners();

    /**
     * Adds an AlarmModel to this service.
     *
     * @param model the AlarmModel to add to the AlarmService.
     */
    void addAlarmModel( AlarmModel model );

    /**
     * Removes an AlarmModel from this service.
     *
     * @param model the AlarmModel to remove from the AlarmService.
     */
    void removeAlarmModel( AlarmModel model );
}
