/*
 * Copyright 2006 Niclas Hedhman.
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
package org.apache.zest.library.logging.log.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.zest.api.ZestAPI;
import org.apache.zest.api.composite.Composite;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.unitofwork.ConcurrentEntityModificationException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.library.logging.log.LogType;
import org.apache.zest.library.logging.log.records.CompositeLogRecord;
import org.apache.zest.library.logging.log.records.EntityLogRecord;
import org.apache.zest.library.logging.log.records.LogRecord;
import org.apache.zest.library.logging.log.records.ServiceLogRecord;

import static org.apache.zest.functional.Iterables.first;

public abstract class LoggingServiceMixin
    implements LoggingService
{
    @Structure private UnitOfWorkFactory uowf;

    public void log( LogType type, Composite composite, String category, String message )
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            List<Serializable> paramsList = new ArrayList<Serializable>();
            createLogRecord( uow, type, composite, category, message, paramsList );
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

    public void log( LogType type, Composite composite, String category, String message, Serializable param1 )
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            List<Serializable> paramsList = new ArrayList<Serializable>();
            paramsList.add( param1 );
            createLogRecord( uow, type, composite, category, message, paramsList );
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

    public void log( LogType type, Composite composite, String category, String message, Serializable param1, Serializable param2 )
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            List<Serializable> paramsList = new ArrayList<Serializable>();
            paramsList.add( param1 );
            paramsList.add( param2 );
            createLogRecord( uow, type, composite, category, message, paramsList );
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
    public void log( LogType type, Composite composite, String category, String message, Serializable... params )
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            List<Serializable> paramsList = new ArrayList<Serializable>( Arrays.asList( params ) );
            createLogRecord( uow, type, composite, category, message, paramsList );
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

    private void createLogRecord( UnitOfWork uow, LogType type, Composite composite, String category, String message, List<Serializable> params )
    {
        if( composite instanceof ServiceComposite )
        {
            EntityBuilder<ServiceLogRecord> builder = uow.newEntityBuilder( ServiceLogRecord.class );
            ServiceLogRecord state = builder.instance();
            setStandardStuff( type, composite, category, message, state, params );
            state.source().set( ( (ServiceComposite) composite ).identity().get() );
            ServiceLogRecord slr = builder.newInstance();
        }
        else if( composite instanceof EntityComposite )
        {
            EntityBuilder<EntityLogRecord> builder = uow.newEntityBuilder( EntityLogRecord.class );
            EntityLogRecord state = builder.instance();
            setStandardStuff( type, composite, category, message, state, params );
            state.source().set( (EntityComposite) composite );
            EntityLogRecord elr = builder.newInstance();
        }
        else
        {
            EntityBuilder<CompositeLogRecord> builder = uow.newEntityBuilder( CompositeLogRecord.class );
            CompositeLogRecord state = builder.instance();
            setStandardStuff( type, composite, category, message, state, params );
            state.source().set( composite );
            CompositeLogRecord clr = builder.newInstance();
        }
    }

    private void setStandardStuff( LogType type, Composite composite, String category, String message,
                                   LogRecord state, List<Serializable> params )
    {
        state.logtype().set( type );
        state.time().set( System.currentTimeMillis() );
        state.category().set( category );
        state.message().set( message );
        state.compositeTypeName().set( getCompositeName( composite ) );
        state.threadName().set( Thread.currentThread().getName() );
        state.parameters().set( params );
    }

    private String getCompositeName( Composite composite )
    {
        return first( ZestAPI.FUNCTION_DESCRIPTOR_FOR.apply( composite ).types()).getName();
    }
}
