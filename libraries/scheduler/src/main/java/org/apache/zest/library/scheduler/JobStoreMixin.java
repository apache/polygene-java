/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.zest.library.scheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.LifecycleException;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.query.Query;
import org.apache.zest.api.query.QueryBuilder;
import org.apache.zest.api.query.QueryBuilderFactory;
import org.apache.zest.api.unitofwork.EntityTypeNotFoundException;
import org.apache.zest.api.unitofwork.NoSuchEntityException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.unitofwork.concern.UnitOfWorkPropagation;
import org.quartz.Calendar;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobPersistenceException;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.JobStore;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.SchedulerSignaler;
import org.quartz.spi.TriggerFiredResult;

import static org.apache.zest.api.query.QueryExpressions.and;
import static org.apache.zest.api.query.QueryExpressions.eq;
import static org.apache.zest.api.query.QueryExpressions.lt;
import static org.apache.zest.api.query.QueryExpressions.not;
import static org.apache.zest.api.query.QueryExpressions.templateFor;

public class JobStoreMixin
    implements JobStore
{
    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    QueryBuilderFactory qbf;
    private String instanceId;
    private String instanceName;
    private int threadPoolSize;

    @Override
    @UnitOfWorkPropagation( usecase = "store quartz job" )
    public void storeJob( JobDetail newJob, boolean replaceExisting )
        throws ObjectAlreadyExistsException
    {
        JobKey key = newJob.getKey();
        JobsGroup group = getJobsGroup( key );
        JobWrapper job = group.jobs().get( key.getName() );
        if( job != null )
        {
            if( !replaceExisting )
            {
                throw new ObjectAlreadyExistsException( newJob );
            }
            job.jobDetail().set( newJob );
        }
        else
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<JobWrapper> builder = uow.newEntityBuilder( JobWrapper.class );
            builder.instance().jobDetail().set( newJob );
            job = builder.newInstance();
            group.jobs().put( key.getName(), job );
        }
    }

    @Override
    public void storeJobsAndTriggers( Map<JobDetail, Set<? extends Trigger>> triggersAndJobs, boolean replace )
        throws ObjectAlreadyExistsException, JobPersistenceException
    {
        // make sure there are no collisions...
        if( !replace )
        {
            for( Map.Entry<JobDetail, Set<? extends Trigger>> e : triggersAndJobs.entrySet() )
            {
                if( checkExists( e.getKey().getKey() ) )
                {
                    throw new ObjectAlreadyExistsException( e.getKey() );
                }
                for( Trigger trigger : e.getValue() )
                {
                    if( checkExists( trigger.getKey() ) )
                    {
                        throw new ObjectAlreadyExistsException( trigger );
                    }
                }
            }
        }
        // do bulk add...
        for( Map.Entry<JobDetail, Set<? extends Trigger>> e : triggersAndJobs.entrySet() )
        {
            storeJob( e.getKey(), true );
            for( Trigger trigger : e.getValue() )
            {
                storeTrigger( (OperableTrigger) trigger, true );
            }
        }
    }

    @Override
    @UnitOfWorkPropagation( usecase = "store quartz trigger" )
    public void storeTrigger( OperableTrigger newTrigger, boolean replaceExisting )
        throws ObjectAlreadyExistsException
    {
        TriggerKey key = newTrigger.getKey();
        TriggersGroup group = getTriggersGroup( key );
        TriggerWrapper trigger = group.triggers().get( key.getName() );
        if( trigger != null )
        {
            if( !replaceExisting )
            {
                throw new ObjectAlreadyExistsException( newTrigger );
            }
            trigger.trigger().set( newTrigger );
        }
        else
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<TriggerWrapper> builder = uow.newEntityBuilder( TriggerWrapper.class );
            builder.instance().trigger().set( newTrigger );
            trigger = builder.newInstance();
            group.triggers().put( key.getName(), trigger );
        }
    }

    @Override
    @UnitOfWorkPropagation( usecase = "store quartz calendar" )
    public void storeCalendar( String name, Calendar newCalendar, boolean replaceExisting, boolean updateTriggers )
        throws ObjectAlreadyExistsException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        Calendars calendars = uow.get( Calendars.class, Calendars.class.getName() );
        try
        {
            CalendarWrapper calendar = calendars.calendars().get( name );
            if( !replaceExisting )
            {
                throw new ObjectAlreadyExistsException( "Calendar " + name + " already exists." );
            }
            calendar.calendar().set( newCalendar );
        }
        catch( NoSuchEntityException e )
        {
            EntityBuilder<CalendarWrapper> builder = uow.newEntityBuilder( CalendarWrapper.class );
            builder.instance().calendar().set( newCalendar );
            CalendarWrapper calendar = builder.newInstance();
            calendars.calendars().put( name, calendar );
        }
    }

    @Override
    @UnitOfWorkPropagation( usecase = "remove quartz job" )
    public boolean removeJob( JobKey jobKey )
        throws JobPersistenceException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        JobsGroups groups = uow.get( JobsGroups.class, JobsGroups.class.getName() );
        JobsGroup group = groups.groups().get( jobKey.getGroup() );
        group.jobs().remove( jobKey.getName() );
        return remove( JobWrapper.class, getIdentity( jobKey ) );
    }

    @Override
    public boolean removeJobs( List<JobKey> jobKeys )
        throws JobPersistenceException
    {
        return jobKeys.stream().map( this::removeJobWithSuccessIndicator ).allMatch( v -> v );
    }

    private Boolean removeJobWithSuccessIndicator( JobKey key )
    {
        try
        {
            return removeJob( key );
        }
        catch( JobPersistenceException e )
        {
            return false;
        }
    }

    @Override
    public JobDetail retrieveJob( JobKey key )
        throws JobPersistenceException
    {
        JobsGroup group = getJobsGroup( key );
        JobWrapper job = group.jobs().get( key.getName() );
        if( job != null )
        {
            return job.jobDetail().get();
        }
        return null;
    }

    @Override
    @UnitOfWorkPropagation( usecase = "remove quartz trigger" )
    public boolean removeTrigger( TriggerKey triggerKey )
        throws JobPersistenceException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        TriggersGroups groups = uow.get( TriggersGroups.class, TriggersGroups.class.getName() );
        TriggersGroup group = groups.groups().get( triggerKey.getGroup() );
        group.triggers().remove( triggerKey.getName() );
        return remove( TriggerWrapper.class, getIdentity( triggerKey ) );
    }

    @Override
    public boolean removeTriggers( List<TriggerKey> triggerKeys )
        throws JobPersistenceException
    {
        return triggerKeys.stream().map( this::removeTriggerWithSuccessIndicator ).allMatch( v -> v );
    }

    private boolean removeTriggerWithSuccessIndicator( TriggerKey triggerKey )
    {
        try
        {
            return removeTrigger( triggerKey );
        }
        catch( JobPersistenceException e )
        {
            return false;
        }
    }

    @Override
    public boolean replaceTrigger( TriggerKey triggerKey, OperableTrigger newTrigger )
        throws JobPersistenceException
    {
        TriggerWrapper existing = (TriggerWrapper) retrieveTrigger( triggerKey );
        if( existing.trigger().get().getJobKey().equals( newTrigger.getJobKey() ))
        {
            removeTrigger( triggerKey );
            storeTrigger( newTrigger, true );
        }
        return false;
    }

    private Stream<TriggerWrapper> allTriggers()
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        TriggersGroups groups = uow.get( TriggersGroups.class, TriggersGroups.class.getName() );
        return groups.groups().toMap().values().stream().flatMap( group -> group.triggers().toMap().values().stream() );
    }

    @Override
    public OperableTrigger retrieveTrigger( TriggerKey key )
        throws JobPersistenceException
    {
        TriggersGroup group = getTriggersGroup( key );
        return group.triggers().get( key.getName() );
    }

    @Override
    public boolean checkExists( JobKey key )
        throws JobPersistenceException
    {
        JobsGroup group = getJobsGroup( key );
        JobWrapper job = group.jobs().get( key.getName() );
        return job != null;
    }

    @Override
    public boolean checkExists( TriggerKey key )
        throws JobPersistenceException
    {
        TriggersGroup group = getTriggersGroup( key );
        return group.triggers().get( key.getName() ) != null;
    }

    @Override
    public void clearAllSchedulingData()
        throws JobPersistenceException
    {
        throw new UnsupportedOperationException( "This operation is not yet supported." );
    }

    @Override
    @UnitOfWorkPropagation( usecase = "remove quartz calendar" )
    public boolean removeCalendar( String calName )
        throws JobPersistenceException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        Calendars calendars = uow.get( Calendars.class, Calendars.class.getName() );
        calendars.calendars().remove( calName );
        return remove( CalendarWrapper.class, calName );
    }

    @Override
    public Calendar retrieveCalendar( String calName )
        throws JobPersistenceException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        Calendars calendars = uow.get( Calendars.class, Calendars.class.getName() );
        return calendars.calendars().get( calName ).calendar().get();
    }

    @Override
    @UnitOfWorkPropagation( usecase = "get number of quartz calendars" )
    public int getNumberOfCalendars()
        throws JobPersistenceException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        Calendars calendars = uow.get( Calendars.class, Calendars.class.getName() );
        return calendars.calendars().count();
    }

    @Override
    public Set<JobKey> getJobKeys( GroupMatcher<JobKey> matcher )
        throws JobPersistenceException
    {
        throw new UnsupportedOperationException( "This operation is not yet supported." );
    }

    @Override
    public Set<TriggerKey> getTriggerKeys( GroupMatcher<TriggerKey> matcher )
        throws JobPersistenceException
    {
        throw new UnsupportedOperationException( "This operation is not yet supported." );
    }

    @Override
    @UnitOfWorkPropagation( usecase = "get number of quartz triggers" )
    public int getNumberOfTriggers()
        throws JobPersistenceException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        TriggersGroups groups = uow.get( TriggersGroups.class, TriggersGroups.class.getName() );
        return (int) groups
            .groups()
            .toMap()
            .values()
            .stream()
            .flatMap(
                triggers -> triggers
                    .triggers()
                    .toMap()
                    .values()
                    .stream()
            ).count();
    }

    @Override
    @UnitOfWorkPropagation( usecase = "get number of quartz jobs" )
    public int getNumberOfJobs()
        throws JobPersistenceException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        JobsGroup jobs = uow.get( JobsGroup.class, JobsGroup.class.getName() );
        return jobs.jobs().count();
    }

    @Override
    @UnitOfWorkPropagation( usecase = "get quartz job group names" )
    public List<String> getJobGroupNames()
        throws JobPersistenceException
    {
        List<String> result = new ArrayList<>();
        UnitOfWork uow = uowf.currentUnitOfWork();
        JobsGroup jobs = uow.get( JobsGroup.class, JobsGroup.class.getName() );
        jobs.jobs().forEach( name -> result.add( getGroupName( name ) ) );
        return result;
    }

    @Override
    @UnitOfWorkPropagation( usecase = "get quartz trigger group names" )
    public List<String> getTriggerGroupNames()
        throws JobPersistenceException
    {
        List<String> result = new ArrayList<>();
        UnitOfWork uow = uowf.currentUnitOfWork();
        TriggersGroup triggersGroup = uow.get( TriggersGroup.class, TriggersGroup.class.getName() );
        triggersGroup.triggers().forEach( name -> result.add( getGroupName( name ) ) );
        return result;
    }

    @Override
    @UnitOfWorkPropagation( usecase = "get quartz calendar names" )
    public List<String> getCalendarNames()
        throws JobPersistenceException
    {
        List<String> result = new ArrayList<>();
        UnitOfWork uow = uowf.currentUnitOfWork();
        Calendars calendars = uow.get( Calendars.class, Calendars.class.getName() );
        calendars.calendars().forEach( result::add );
        return result;
    }

    @Override
    @UnitOfWorkPropagation( usecase = "get quartz triggers for job" )
    public List<OperableTrigger> getTriggersForJob( JobKey jobKey )
        throws JobPersistenceException
    {
        List<OperableTrigger> result = new ArrayList<>();
        UnitOfWork uow = uowf.currentUnitOfWork();
        TriggersGroup triggersGroup = uow.get( TriggersGroup.class, TriggersGroup.class.getName() );
        triggersGroup.triggers().toMap().values().stream()
            .map( triggerWrapper -> (OperableTrigger) triggerWrapper.trigger().get() )
            .filter( trigger -> trigger.getJobKey().equals( jobKey ) )
            .forEach( result::add );
        return result;
    }

    @Override
    public Trigger.TriggerState getTriggerState( TriggerKey triggerKey )
        throws JobPersistenceException
    {
        throw new UnsupportedOperationException( "This operation is not yet supported." );
    }

    @Override
    public void pauseTrigger( TriggerKey triggerKey )
        throws JobPersistenceException
    {
        throw new UnsupportedOperationException( "This operation is not yet supported." );
    }

    @Override
    public Collection<String> pauseTriggers( GroupMatcher<TriggerKey> matcher )
        throws JobPersistenceException
    {
        throw new UnsupportedOperationException( "This operation is not yet supported." );
    }

    @Override
    public void pauseJob( JobKey jobKey )
        throws JobPersistenceException
    {
        throw new UnsupportedOperationException( "This operation is not yet supported." );
    }

    @Override
    public Collection<String> pauseJobs( GroupMatcher<JobKey> groupMatcher )
        throws JobPersistenceException
    {
        throw new UnsupportedOperationException( "This operation is not yet supported." );
    }

    @Override
    public void resumeTrigger( TriggerKey triggerKey )
        throws JobPersistenceException
    {
        throw new UnsupportedOperationException( "This operation is not yet supported." );
    }

    @Override
    public Collection<String> resumeTriggers( GroupMatcher<TriggerKey> matcher )
        throws JobPersistenceException
    {
        throw new UnsupportedOperationException( "This operation is not yet supported." );
    }

    @Override
    public Set<String> getPausedTriggerGroups()
        throws JobPersistenceException
    {
        throw new UnsupportedOperationException( "This operation is not yet supported." );
    }

    @Override
    public void resumeJob( JobKey jobKey )
        throws JobPersistenceException
    {
        throw new UnsupportedOperationException( "This operation is not yet supported." );
    }

    @Override
    public Collection<String> resumeJobs( GroupMatcher<JobKey> matcher )
        throws JobPersistenceException
    {
        throw new UnsupportedOperationException( "This operation is not yet supported." );
    }

    @Override
    public void pauseAll()
        throws JobPersistenceException
    {
        throw new UnsupportedOperationException( "This operation is not yet supported." );
    }

    @Override
    public void resumeAll()
        throws JobPersistenceException
    {
        throw new UnsupportedOperationException( "This operation is not yet supported." );
    }

    @Override
    public List<OperableTrigger> acquireNextTriggers( long noLaterThan, int maxCount, long timeWindow )
        throws JobPersistenceException
    {
        List<OperableTrigger> result = new ArrayList<>();
        Query<TriggerWrapper> query = createNextTriggersQuery( noLaterThan, timeWindow );
        query.maxResults( maxCount );
        for( TriggerWrapper wrapper : query )
        {
            wrapper.state().set( TriggerWrapper.State.aquired );
            result.add( wrapper );
        }
        return result;
    }

    private Query<TriggerWrapper> createNextTriggersQuery( long noLaterThan, long timeWindow )
    {
        QueryBuilder<TriggerWrapper> qb = qbf.newQueryBuilder( TriggerWrapper.class );
        TriggerWrapper template = templateFor( TriggerWrapper.class );
        qb = qb.where(
            and(
                lt( template.nextTime(), noLaterThan + timeWindow ),
                not( eq( template.state(), TriggerWrapper.State.aquired ) )
            )
        );
        return uowf.currentUnitOfWork().newQuery( qb );
    }

    @Override
    public void releaseAcquiredTrigger( OperableTrigger trigger )
    {
        ( (TriggerWrapper) trigger ).state().set( TriggerWrapper.State.waiting );
    }

    @Override
    public List<TriggerFiredResult> triggersFired( List<OperableTrigger> triggers )
        throws JobPersistenceException
    {
        throw new UnsupportedOperationException( "This operation is not yet supported." );
    }

    @Override
    public void triggeredJobComplete( OperableTrigger trigger,
                                      JobDetail jobDetail,
                                      Trigger.CompletedExecutionInstruction triggerInstCode
    )
    {
        System.out.println("Job completed: " + jobDetail);
    }

    @Override
    public void setInstanceId( String schedInstId )
    {
        instanceId = schedInstId;
    }

    @Override
    public void setInstanceName( String schedName )
    {
        instanceName = schedName;
    }

    @Override
    public void setThreadPoolSize( int poolSize )
    {
        threadPoolSize = poolSize;
    }

    @Override
    public void initialize( ClassLoadHelper loadHelper, SchedulerSignaler signaler )
        throws SchedulerConfigException
    {

    }

    @Override
    public void schedulerStarted()
        throws SchedulerException
    {

    }

    @Override
    public void schedulerPaused()
    {

    }

    @Override
    public void schedulerResumed()
    {

    }

    @Override
    public void shutdown()
    {

    }

    @Override
    public boolean supportsPersistence()
    {
        return true;
    }

    @Override
    public long getEstimatedTimeToReleaseAndAcquireTrigger()
    {
        return 0;
    }

    @Override
    public boolean isClustered()
    {
        return false;
    }

    @Override
    public void storeJobAndTrigger( JobDetail newJob, OperableTrigger newTrigger )
        throws ObjectAlreadyExistsException, JobPersistenceException
    {
        storeJob( newJob, false );
        storeTrigger( newTrigger, false );
    }

    private String getIdentity( JobKey key )
    {
        return key.getGroup() + ":" + key.getName();
    }

    private String getIdentity( TriggerKey key )
    {
        return key.getGroup() + ":" + key.getName();
    }

    private String getGroupName( String name )
    {
        return name.split( ":" )[ 0 ];
    }

    private JobsGroup getJobsGroup( JobKey key )
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        JobsGroups groups = uow.get( JobsGroups.class, JobsGroups.class.getName() );
        JobsGroup jobsGroup = groups.groups().get( jobsGroupIdentity( key ) );
        if( jobsGroup == null )
        {
            jobsGroup = uow.newEntity( JobsGroup.class, jobsGroupIdentity( key ) );
        }
        return jobsGroup;
    }

    private String jobsGroupIdentity( JobKey key )
    {
        return "jobsGroup://" + key.getGroup() + "." + key.getName();
    }

    private TriggersGroup getTriggersGroup( TriggerKey key )
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        TriggersGroups groups = uow.get( TriggersGroups.class, TriggersGroups.class.getName() );
        TriggersGroup triggersGroup = groups.groups().get( triggersGroupIdentity( key ) );
        if( triggersGroup == null )
        {
            triggersGroup = uow.newEntity( TriggersGroup.class, triggersGroupIdentity( key ) );
        }
        return triggersGroup;
    }

    private String triggersGroupIdentity( TriggerKey key )
    {
        return "triggersGroup://" + key.getGroup() + "." + key.getName();
    }

    private <T> boolean remove( Class<T> type, String identity )
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        try
        {
            T wrapper = uow.get( type, identity );
            uow.remove( wrapper );
            return true;
        }
        catch( EntityTypeNotFoundException | NoSuchEntityException | LifecycleException e )
        {
            return false;
        }
    }
}
