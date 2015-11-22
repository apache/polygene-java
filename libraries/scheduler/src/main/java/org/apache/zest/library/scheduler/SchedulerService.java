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

import java.util.Collection;
import org.apache.zest.api.ZestAPI;
import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.object.ObjectFactory;
import org.apache.zest.api.service.ServiceActivation;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.unitofwork.NoSuchEntityException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueBuilderFactory;
import org.apache.zest.spi.ZestSPI;
import org.apache.zest.spi.uuid.UuidIdentityGeneratorService;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.impl.SchedulerRepository;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.spi.JobFactory;
import org.quartz.spi.JobStore;

@Mixins( { SchedulerService.SchedulerActivationMixin.class } )
public interface SchedulerService extends SchedulerFactory, ServiceActivation, ServiceComposite
{
    ZestJobDetail createJobDetails(ZestJob jobEntity);

    abstract class SchedulerActivationMixin
        implements SchedulerService
    {

        @This
        private Configuration<SchedulerConfiguration> conf;

        @Service
        UuidIdentityGeneratorService uuid;

        @Service
        JobStore jobStore;

        @Structure
        ZestAPI api;

        @Structure
        ObjectFactory objectFactory;

        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        ValueBuilderFactory vbf;

        @Override
        public void activateService()
            throws Exception
        {
            initializeRoots();
            SchedulerConfiguration config = conf.get();
            DirectSchedulerFactory factory = DirectSchedulerFactory.getInstance();
            SimpleThreadPool threadPool = new SimpleThreadPool();
            threadPool.setThreadCount( config.threadCount().get() );
            threadPool.setThreadPriority( config.threadPriority().get() );
            threadPool.setInstanceName( identity().get() );
            threadPool.setThreadNamePrefix( identity().get() );
            factory.createScheduler( identity().get(), uuid.generate( Scheduler.class ), threadPool, jobStore );

            JobFactory jobFactory = objectFactory.newObject( ZestJobFactory.class );
            getScheduler().setJobFactory( jobFactory );
            getScheduler().start();
        }

        @Override
        public void passivateService()
            throws Exception
        {
            getScheduler().shutdown();
            SchedulerRepository schedRep = SchedulerRepository.getInstance();
            schedRep.remove( identity().get() );
        }

        @Override
        public Scheduler getScheduler()
            throws SchedulerException
        {
            SchedulerRepository schedRep = SchedulerRepository.getInstance();
            String schedulerIdentity = identity().get();
            return schedRep.lookup( schedulerIdentity );
        }

        @Override
        public Scheduler getScheduler( String schedName )
            throws SchedulerException
        {
            throw new UnsupportedOperationException( "Quartz in Apache Zest doesn't facilitate the Quartz Repository concept, as the Zest service mechanism can replace it that.\nRegister an additional SchedulerService in your bootstrap and look it up using normal Zest @Service injection or via ServiceFinder." );
        }

        @Override
        public Collection<Scheduler> getAllSchedulers()
            throws SchedulerException
        {
            throw new UnsupportedOperationException( "Quartz in Apache Zest doesn't facilitate the Quartz Repository concept, as the Zest service mechanism can replace it that.\nAll SchedulerServices can be looked up with @Service List<ServiceReference<SchedulerService>>" );
        }

        @Override
        public ZestJobDetail createJobDetails( ZestJob jobEntity )
        {
            ValueBuilder<ZestJobDetail> builder = vbf.newValueBuilder( ZestJobDetail.class );
            ZestJobDetail.State proto = builder.prototypeFor(ZestJobDetail.State.class);
            proto.description().set( jobEntity.description().get() );
            proto.jobClass().set( api.entityDescriptorFor( jobEntity ).primaryType().getName() );
            proto.jobIdentity().set( jobEntity.identity().get() );
            return builder.newInstance();
        }

        private void initializeRoots()
        {
            try( UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( "initialize quartz root entities" ) ) )
            {
                createJobsGroups();
                createTriggersGroups();
                createCalendars();
                uow.complete();
            }
        }

        private void createJobsGroups()
        {
            createContainer( JobsGroups.class );
        }

        private void createTriggersGroups()
        {
            createContainer( TriggersGroups.class );
        }

        private void createCalendars()
        {
            createContainer( Calendars.class );
        }

        private void createContainer( Class<? extends EntityComposite> type )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            try
            {
                uow.get( type, type.getName() );
            } catch( NoSuchEntityException e)
            {
                uow.newEntity( type, type.getName() );
            }
        }
    }
}
