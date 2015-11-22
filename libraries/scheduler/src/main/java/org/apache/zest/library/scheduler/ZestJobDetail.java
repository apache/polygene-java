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

import java.lang.reflect.UndeclaredThrowableException;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;

@Mixins( ZestJobDetail.ZestJobDetailMixin.class )
public interface ZestJobDetail extends JobDetail
{

    interface State
    {
        Property<String> jobIdentity();

        @Optional
        Property<String> description();

        Property<String> jobClass();
    }

    class ZestJobDetailMixin implements JobDetail
    {

        @This
        State state;

        @Override
        public JobKey getKey()
        {
            String id = state.jobIdentity().get();
            id = id.substring( "job://".length() );
            String[] split = id.split( "\\." );
            return JobKey.jobKey( split[ 1 ], split[ 0 ] );
        }

        @Override
        public String getDescription()
        {
            return state.description().get();
        }

        @Override
        public Class<? extends Job> getJobClass()
        {
            String classname = state.jobClass().get();
            try
            {
                @SuppressWarnings( "unchecked" )
                Class<? extends Job> jobClass = (Class<? extends Job>) getClass().getClassLoader()
                    .loadClass( classname );
                return jobClass;
            }
            catch( ClassNotFoundException e )
            {
                throw new UndeclaredThrowableException( e );
            }
        }

        @Override
        public JobDataMap getJobDataMap()
        {
            return null;
        }

        @Override
        public boolean isDurable()
        {
            return true;
        }

        @Override
        public boolean isPersistJobDataAfterExecution()
        {
            return false;
        }

        @Override
        public boolean isConcurrentExectionDisallowed()
        {
            return false;
        }

        @Override
        public boolean requestsRecovery()
        {
            return false;
        }

        @Override
        public JobBuilder getJobBuilder()
        {
            throw new UnsupportedOperationException( "Quartz JobBuilder is not supported in Apache Zest. Use EntityBuilders." );
        }

        @Override
        public Object clone()
        {
            throw new UnsupportedOperationException( "clone() should not be needed as JobDetail is a ValueComposite." );
        }
    }
}
