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

import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

public class ZestJobFactory
    implements JobFactory
{
    @Structure
    private UnitOfWorkFactory uowf;

    @Override
    public Job newJob( TriggerFiredBundle bundle, Scheduler scheduler )
        throws SchedulerException
    {
        JobDetail jobDetail = bundle.getJobDetail();
        Class<? extends Job> jobType = jobDetail.getJobClass();
        if( ZestJob.class.isAssignableFrom( jobType ) )
        {
            JobKey jobKey = jobDetail.getKey();
            String jobId = "job://" + jobKey.getGroup() + "." + jobKey.getName();

            @SuppressWarnings( "unchecked" )
            ZestJob job = createJob( (Class<? extends ZestJob>) jobType, jobId );
            return job;
        }
        return null;
    }

    private <T extends ZestJob> T createJob( Class<T> jobType, String jobId )
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        return uow.get( jobType, jobId );
    }
}
