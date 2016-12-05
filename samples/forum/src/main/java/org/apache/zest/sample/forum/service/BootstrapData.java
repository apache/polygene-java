/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.sample.forum.service;

import org.apache.zest.api.activation.ActivatorAdapter;
import org.apache.zest.api.activation.Activators;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.api.unitofwork.NoSuchEntityException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.sample.forum.data.entity.Forums;
import org.apache.zest.sample.forum.data.entity.Users;

/**
 * TODO
 */
@Mixins( BootstrapData.Mixin.class )
@Activators( BootstrapData.Activator.class )
public interface BootstrapData
    extends ServiceComposite
{
    void insertInitialData()
        throws Exception;

    class Activator
        extends ActivatorAdapter<ServiceReference<BootstrapData>>
    {
        @Override
        public void afterActivation( ServiceReference<BootstrapData> activated )
            throws Exception
        {
            activated.get().insertInitialData();
        }
    }

    abstract class Mixin
        implements BootstrapData
    {
        @Structure
        private UnitOfWorkFactory module;

        @Override
        public void insertInitialData()
            throws Exception
        {
            UnitOfWork unitOfWork = module.newUnitOfWork();

            try
            {
                unitOfWork.get( Forums.class, Forums.FORUMS_ID );
            }
            catch( NoSuchEntityException e )
            {
                unitOfWork.newEntity( Forums.class, Forums.FORUMS_ID );
            }

            try
            {
                unitOfWork.get( Users.class, Users.USERS_ID );
            }
            catch( NoSuchEntityException e )
            {
                unitOfWork.newEntity( Users.class, Users.USERS_ID );
            }

            unitOfWork.complete();
        }
    }
}
