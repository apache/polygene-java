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
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;

/**
 * Defines the AlarmSystem interface.
 *
 * <p>
 * The <code>AlarmSystem</code> is a central registry/handler for all
 * <code>Alarm</code> objects. By registering <code>AlarmListener</code>s
 * to the AlarmSystem, objects are able to 'ignore' the fact that there
 * are many <code>Alarm</code> objects in the system.
 * </p>
 * <p>
 * Also, new attributes registered with the AlarmSystem will propagate into
 * all existing and future <code>Alarm</code>s, whereas attributes at <code>
 * Alarm</code> level is individual to an <code>Alarm</code>.
 * </p>
 * <p>
 * Many different AlarmModels can co-exist in the same application.
 * In fact, every Alarm can have its own AlarmModel, and the AlarmModel
 * can be changed in runtime, for unrivaled flexibility. However, typically
 * the AlarmModel is set in the AlarmSystem only, and all Alarms will
 * use the default model.
 * </p>
 * <p>The default alarm model is a service set by during assembly;
 * </p>
 * <pre><code>
 *   public void assemble( ModuleAssembly module )
 *       throws AssemblyException
 *   {
 *       module.addServices( AlarmSystemService.class );
 *       module.addServices( StandardAlarmModelService.class );
 *   }
 * </code></pre>
 *
 * @author Niclas Hedhman
 */
@Mixins(AlarmSystemMixin.class)
public interface AlarmSystem
{

    /**
     * Returns all the AlarmModels that has been installed.
     *
     * @return all the AlarmModels that has been installed.
     */
    List<AlarmModelDescriptor> alarmModels();

    /**
     * Returns the default AlarmModel.
     *
     * @return the default AlarmModel in this AlarmSystem.
     */
    AlarmModel defaultAlarmModel();

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
    Query<Alarm> alarmList();

    /**
     * Creates an Alarm with the default AlarmModel.
     *
     * @param name the name of the Alarm to be created.
     *
     * @return the created Alarm with the given name using the default AlarmModel.
     */
    Alarm createAlarm( String name );

    /**
     * Register AlarmListener to recieve <code>AlarmEvents</code> from all
     * <code>Alarms</code> managed by this <code>AlarmSystem</code>.
     *
     * @param listener the global listener to be added.
     */
    void addAlarmListener( AlarmListener listener );

    /**
     * Remove the <code>AlarmListener</code> from the <code>AlarmSystem</code>.
     *
     * @param listener the global listener to be removed.
     */
    void removeAlarmListener( AlarmListener listener );

    /**
     * Returns a list of all AlarmListeners registered to the service.
     *
     * @return a list of all AlarmListeners registered to the service.
     */
    List<AlarmListener> alarmListeners();
}
