/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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

package org.qi4j.logging.debug.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.qi4j.api.Qi4j;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.logging.debug.records.CompositeDebugRecordEntity;
import org.qi4j.logging.debug.records.DebugRecord;
import org.qi4j.logging.debug.records.EntityDebugRecordEntity;
import org.qi4j.logging.debug.records.ServiceDebugRecordEntity;

import static org.qi4j.functional.Iterables.first;

public class DebuggingServiceMixin
    implements DebuggingService
{
    @Structure private UnitOfWorkFactory uowf;
    @This private Configuration<DebugServiceConfiguration> configuration;

    @Override
    public int debugLevel()
    {
        return configuration.get().debugLevel().get();
    }

    @Override
    public void debug( Composite composite, String message )
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            List<Serializable> paramsList = new ArrayList<Serializable>();
            createDebugRecord( uow, composite, message, paramsList );
            uow.complete();
        }
        catch( ConcurrentEntityModificationException e )
        {
            // ignore for now. Perhaps discard() and try again.
        }
        catch( UnitOfWorkCompletionException e )
        {
            // ignore for now. Perhaps discard() and try again.
        }
    }

    @Override
    public void debug( Composite composite, String message, Serializable param1 )
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            List<Serializable> paramsList = new ArrayList<Serializable>();
            paramsList.add( param1 );
            createDebugRecord( uow, composite, message, paramsList );
            uow.complete();
        }
        catch( ConcurrentEntityModificationException e )
        {
            // ignore for now. Perhaps discard() and try again.
        }
        catch( UnitOfWorkCompletionException e )
        {
            // ignore for now. Perhaps discard() and try again.
        }
    }

    @Override
    public void debug( Composite composite, String message, Serializable param1, Serializable param2 )
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            List<Serializable> paramsList = new ArrayList<Serializable>();
            paramsList.add( param1 );
            paramsList.add( param2 );
            createDebugRecord( uow, composite, message, paramsList );
            uow.complete();
        }
        catch( ConcurrentEntityModificationException e )
        {
            // ignore for now. Perhaps discard() and try again.
        }
        catch( UnitOfWorkCompletionException e )
        {
            // ignore for now. Perhaps discard() and try again.
        }
    }

    @Override
    public void debug( Composite composite, String message, Serializable... params )
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            List<Serializable> paramsList = new ArrayList<Serializable>( Arrays.asList( params ) );
            createDebugRecord( uow, composite, message, paramsList );
            uow.complete();
        }
        catch( ConcurrentEntityModificationException e )
        {
            // ignore for now. Perhaps discard() and try again.
        }
        catch( UnitOfWorkCompletionException e )
        {
            // ignore for now. Perhaps discard() and try again.
        }
    }

    private void createDebugRecord( UnitOfWork uow, Composite composite, String message, List<Serializable> params )
    {
        if( composite instanceof ServiceComposite )
        {
            EntityBuilder<ServiceDebugRecordEntity> builder = uow.newEntityBuilder( ServiceDebugRecordEntity.class );
            ServiceDebugRecordEntity state = builder.instance();
            setStandardStuff( composite, message, state, params );
            state.source().set( ( (ServiceComposite) composite ).identity().get() );
            ServiceDebugRecordEntity slr = builder.newInstance();
        }
        else if( composite instanceof EntityComposite )
        {
            EntityBuilder<EntityDebugRecordEntity> builder = uow.newEntityBuilder( EntityDebugRecordEntity.class );
            EntityDebugRecordEntity state = builder.instance();
            setStandardStuff( composite, message, state, params );
            state.source().set( (EntityComposite) composite );
            EntityDebugRecordEntity elr = builder.newInstance();
        }
        else
        {
            EntityBuilder<CompositeDebugRecordEntity> builder = uow.newEntityBuilder( CompositeDebugRecordEntity.class );
            CompositeDebugRecordEntity state = builder.instance();
            setStandardStuff( composite, message, state, params );
            state.source().set( composite );
            CompositeDebugRecordEntity clr = builder.newInstance();
        }
    }

    private void setStandardStuff( Composite composite, String message, DebugRecord state, List<Serializable> params )
    {
        state.time().set( System.currentTimeMillis() );
        state.message().set( message );
        state.compositeTypeName().set( getCompositeName( composite ) );
        state.threadName().set( Thread.currentThread().getName() );
        state.parameters().set( params );
    }

    private String getCompositeName( Composite composite )
    {
        return first(Qi4j.FUNCTION_DESCRIPTOR_FOR.map( composite ).types()).getName();
    }
}
