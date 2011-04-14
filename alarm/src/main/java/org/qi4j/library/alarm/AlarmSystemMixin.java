/*
 * Copyright 1996-2005 Niclas Hedhman.
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
package org.qi4j.library.alarm;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

/**
 * AlarmSystem implementation.
 */
public class AlarmSystemMixin
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
        List<AlarmModelDescriptor> descriptors = new ArrayList<AlarmModelDescriptor>();
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
     * Creates an Alarm with the default AlarmModel.
     */
    public Alarm createAlarm( String name )
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        EntityBuilder<Alarm> builder = uow.newEntityBuilder( Alarm.class );
        AlarmState state = builder.instanceFor( AlarmState.class );
        state.systemName().set( name );
        state.currentStatus().set(createStatus(Alarm.STATUS_NORMAL));
        return builder.newInstance();
    }

    private AlarmStatus createStatus( String status )
    {
        ValueBuilder<AlarmStatus> builder = vbf.newValueBuilder(AlarmStatus.class);
        builder.prototype().name().set(status);
        builder.prototype().creationDate().set( new Date() );
        return builder.newInstance();
    }

    /**
     * Register AlarmListener to recieve <code>AlarmEvents</code> from all
     * <code>Alarms</code> managed by this <code>AlarmSystem</code>.
     */
    public void addAlarmListener( AlarmListener listener )
    {
        alarmListeners.add( listener );
    }

    /**
     * Remove the <code>AlarmListener</code> from the <code>AlarmSystem</code>.
     */
    public void removeAlarmListener( AlarmListener listener )
    {
        alarmListeners.remove( listener );
    }

    /**
     * Returns a list of all Alarms registered to the service.
     */
    public Query<Alarm> alarmList()
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        QueryBuilder<Alarm> builder = qbf.newQueryBuilder( Alarm.class );
        return builder.newQuery( uow );
    }

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
