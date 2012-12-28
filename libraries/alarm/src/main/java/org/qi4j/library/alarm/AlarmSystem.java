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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

/**
 * Defines the AlarmSystem interface.
 *
 * <p>
 * The <code>AlarmSystem</code> is a central registry/handler for all
 * <code>AlarmPoint</code> objects. By registering <code>AlarmListener</code>s
 * to the AlarmSystem, objects are able to 'ignore' the fact that there
 * are many <code>AlarmPoint</code> objects in the system.
 * </p>
 * <p>
 * Also, new attributes registered with the AlarmSystem will propagate into
 * all existing and future <code>AlarmPoint</code>s, whereas attributes at <code>
 * AlarmPoint</code> level is individual to an <code>AlarmPoint</code>.
 * </p>
 * <p>
 * Many different AlarmModels can co-exist in the same application.
 * In fact, every AlarmPoint can have its own AlarmModel, and the AlarmModel
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
@Mixins( AlarmSystemService.AlarmSystemMixin.class )
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
    Query<AlarmPoint> alarmList();

    /**
     * Creates an AlarmPoint with the default AlarmModel.
     *
     * @param name the name of the AlarmPoint to be created.
     *
     * @param category The category the created AlarmPoint should belong to.
     *
     * @return the created AlarmPoint with the given name using the default AlarmModel.
     */
    AlarmPoint createAlarm( String name, AlarmCategory category );

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
     * Returns an immmutable list of all AlarmListeners registered to the service.
     *
     * @return a list of all AlarmListeners registered to the service.
     */
    List<AlarmListener> alarmListeners();

    /**
     * AlarmSystem implementation.
     */
    class AlarmSystemMixin
        implements AlarmSystem
    {
        @Service
        private Iterable<ServiceReference<AlarmModel>> models;

        private final CopyOnWriteArrayList<AlarmListener> alarmListeners;

        @Structure
        private UnitOfWorkFactory uowf;

        @Structure
        private ValueBuilderFactory vbf;

        @Structure
        private QueryBuilderFactory qbf;

        public AlarmSystemMixin()
        {
            alarmListeners = new CopyOnWriteArrayList<AlarmListener>();
        }

        /**
         * Returns all the AlarmModels that has been installed.
         */
        @Override
        public List<AlarmModelDescriptor> alarmModels()
        {
            List<AlarmModelDescriptor> descriptors = new ArrayList<AlarmModelDescriptor>();
            for( ServiceReference<AlarmModel> model : models )
            {
                descriptors.add( model.metaInfo( AlarmModelDescriptor.class ) );
            }
            return descriptors;
        }

        /**
         * Returns the default AlarmModel.
         */
        @Override
        public AlarmModel defaultAlarmModel()
        {
            AlarmModelDescriptor defaultDefault = null;
            for( AlarmModelDescriptor descriptor : alarmModels() )
            {
                if( descriptor.isDefaultModel() )
                {
                    return alarmModel( descriptor );
                }
                defaultDefault = descriptor;
            }
            return alarmModel( defaultDefault );
        }

        private AlarmModel alarmModel( AlarmModelDescriptor descriptor )
        {
            for( ServiceReference<AlarmModel> model : models )
            {
                if( model.metaInfo( AlarmModelDescriptor.class ).equals( descriptor ) )
                {
                    return model.get();
                }
            }
            return null;
        }

        /**
         * Creates an AlarmPoint with the default AlarmModel.
         * @param name The system name of the AlarmPoint.
         * @param category The AlarmPoint Category the created alarm should belong to.
         */
        @Override
        public AlarmPoint createAlarm( String name, AlarmCategory category )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<AlarmPoint> builder = uow.newEntityBuilder( AlarmPoint.class );
            builder.instance().category().set( category );
            AlarmPoint.AlarmState state = builder.instanceFor( AlarmPoint.AlarmState.class );
            state.systemName().set( name );
            state.currentStatus().set( createStatus( AlarmPoint.STATUS_NORMAL ) );
            return builder.newInstance();
        }

        private AlarmStatus createStatus( String status )
        {
            ValueBuilder<AlarmStatus> builder = vbf.newValueBuilder( AlarmStatus.class );
            AlarmStatus.State statePrototype = builder.prototypeFor( AlarmStatus.State.class );
            statePrototype.name().set( status );
            statePrototype.creationDate().set( new Date() );
            return builder.newInstance();
        }

        /**
         * Register AlarmListener to recieve <code>AlarmEvents</code> from all
         * <code>Alarms</code> managed by this <code>AlarmSystem</code>.
         */
        @Override
        public void addAlarmListener( AlarmListener listener )
        {
            alarmListeners.add( listener );
        }

        /**
         * Remove the <code>AlarmListener</code> from the <code>AlarmSystem</code>.
         */
        @Override
        public void removeAlarmListener( AlarmListener listener )
        {
            alarmListeners.remove( listener );
        }

        /**
         * Returns a list of all Alarms registered to the service.
         */
        @Override
        public Query<AlarmPoint> alarmList()
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            QueryBuilder<AlarmPoint> builder = qbf.newQueryBuilder( AlarmPoint.class );
            return uow.newQuery( builder );
        }

        @Override
        public List<AlarmListener> alarmListeners()
        {
            synchronized( alarmListeners )
            {
                return alarmListeners;
            }
        }

        public void alarmFired( AlarmEvent event )
        {
            Iterator list;
            //noinspection SynchronizeOnNonFinalField
            synchronized( alarmListeners )
            {
                list = alarmListeners.iterator();
            }
            while( list.hasNext() )
            {
                AlarmListener listener = (AlarmListener) list.next();
                try
                {
                    listener.alarmFired( event );
                }
                catch( Exception e )
                {
                    // TODO: Utilize a logger system instead.
                    System.err.println( "Exception in AlarmListener: " + listener );
                    e.printStackTrace();
                }
            }
        }
    }
}
