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

package org.apache.polygene.runtime.unitofwork;

import java.time.Instant;
import java.util.Stack;
import org.apache.polygene.api.composite.CompositeInstance;
import org.apache.polygene.api.composite.TransientBuilderFactory;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.metrics.MetricsProvider;
import org.apache.polygene.api.time.SystemTime;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.usecase.Usecase;
import org.apache.polygene.runtime.entity.EntityInstance;
import org.apache.polygene.spi.module.ModuleSpi;

public class UnitOfWorkFactoryMixin
    implements UnitOfWorkFactory
{
    @Structure
    private TransientBuilderFactory tbf;

    @Structure
    private ModuleSpi module;

    // Implementation of UnitOfWorkFactory
    @Override
    public UnitOfWork newUnitOfWork()
    {
        return newUnitOfWork( Usecase.DEFAULT );
    }

    @Override
    public UnitOfWork newUnitOfWork(Instant currentTime )
    {
        return newUnitOfWork( Usecase.DEFAULT, currentTime );
    }

    @Override
    public UnitOfWork newUnitOfWork( Usecase usecase )
    {
        return newUnitOfWork( usecase == null ? Usecase.DEFAULT : usecase, SystemTime.now() );
    }

    @Override
    public UnitOfWork newUnitOfWork( Usecase usecase, Instant currentTime )
    {
        UnitOfWorkInstance unitOfWorkInstance = new UnitOfWorkInstance( module, usecase, currentTime, metricsProvider() );
        return tbf.newTransient( UnitOfWork.class, unitOfWorkInstance );
    }

    private MetricsProvider metricsProvider()
    {
        return module.metricsProvider();
    }

    @Override
    public boolean isUnitOfWorkActive()
    {
        Stack<UnitOfWorkInstance> stack = UnitOfWorkInstance.getCurrent();
        return !stack.isEmpty();
    }

    @Override
    public UnitOfWork currentUnitOfWork()
    {
        Stack<UnitOfWorkInstance> stack = UnitOfWorkInstance.getCurrent();
        if( stack.size() == 0 )
        {
            throw new IllegalStateException( "No current UnitOfWork active" );
        }
        return tbf.newTransient( UnitOfWork.class, stack.peek() );
    }

    @Override
    public UnitOfWork getUnitOfWork( EntityComposite entity )
    {
        EntityInstance instance = (EntityInstance) CompositeInstance.compositeInstanceOf( entity );
        return instance.unitOfWork();
    }
}
