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

package org.apache.zest.library.logging.trace.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.apache.zest.api.ZestAPI;
import org.apache.zest.api.composite.Composite;
import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.unitofwork.ConcurrentEntityModificationException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.library.logging.trace.records.CompositeTraceRecordEntity;
import org.apache.zest.library.logging.trace.records.EntityTraceRecordEntity;
import org.apache.zest.library.logging.trace.records.ServiceTraceRecordEntity;
import org.apache.zest.library.logging.trace.records.TraceRecord;

public class TraceServiceMixin
    implements TraceService
{
    @Structure
    private UnitOfWorkFactory uowf;
    @This
    private Configuration<TraceServiceConfiguration> configuration;
    private int counter;
    private Integer traceLevel;

    @Override
    public int traceLevel()
    {
        if( counter++ % 100 == 0 )
        {
            counter = 0;
            traceLevel = configuration.get().traceLevel().get();
        }
        return traceLevel;
    }

    @Override
    public void traceSuccess( Class compositeType,
                              Composite object,
                              Method method,
                              Object[] args,
                              Object result,
                              long entryTime,
                              long durationNano
    )
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            createTraceRecord( uow, compositeType, object, method, args, entryTime, durationNano, null );
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
    public void traceException( Class compositeType,
                                Composite object,
                                Method method,
                                Object[] args,
                                Throwable t,
                                long entryTime,
                                long durationNano
    )
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            createTraceRecord( uow, compositeType, object, method, args, entryTime, durationNano, t );
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

    private void createTraceRecord( UnitOfWork uow,
                                    Class compositeType,
                                    Composite object,
                                    Method method,
                                    Object[] args,
                                    long entryTime,
                                    long durationNano,
                                    Throwable exception
    )
    {
        if( object instanceof EntityComposite )
        {
            EntityComposite entity = (EntityComposite) object;
            String identity = entity.identity().get();
            EntityComposite source = (EntityComposite) uow.get(
                ZestAPI.FUNCTION_DESCRIPTOR_FOR.apply( entity )
                    .types()
                    .findFirst()
                    .orElse( null ), identity );
            EntityBuilder<EntityTraceRecordEntity> builder = uow.newEntityBuilder( EntityTraceRecordEntity.class );
            EntityTraceRecordEntity state = builder.instance();
            setStandardStuff( compositeType, method, args, entryTime, durationNano, state, exception );
            state.source().set( source );
            EntityTraceRecordEntity etr = builder.newInstance();  // Record is created.
        }
        else if( object instanceof ServiceComposite )
        {
            ServiceComposite service = (ServiceComposite) object;
            EntityBuilder<ServiceTraceRecordEntity> builder = uow.newEntityBuilder( ServiceTraceRecordEntity.class );
            ServiceTraceRecordEntity state = builder.instance();
            setStandardStuff( compositeType, method, args, entryTime, durationNano, state, exception );
            state.source().set( service.toString() );
            ServiceTraceRecordEntity str = builder.newInstance();  // Record is created.
        }
        else
        {
            EntityBuilder<CompositeTraceRecordEntity> builder = uow.newEntityBuilder( CompositeTraceRecordEntity.class );
            CompositeTraceRecordEntity state = builder.instance();
            state.source().set( object );
            setStandardStuff( compositeType, method, args, entryTime, durationNano, state, exception );
            CompositeTraceRecordEntity ctr = builder.newInstance();  // Record is created.
        }
    }

    private void setStandardStuff( Class compositeType,
                                   Method method,
                                   Object[] args,
                                   long entryTime,
                                   long durationNano,
                                   TraceRecord state,
                                   Throwable exception
    )
    {
        state.duration().set( durationNano );
        state.entryTime().set( entryTime );
        state.methodName().set( method.getName() );
        state.compositeTypeName().set( compositeType.getName() );
        state.arguments().set( convertArguments( args ) );
        state.threadName().set( Thread.currentThread().getName() );
        state.exception().set( exception );
    }

    private List<String> convertArguments( Object[] args )
    {
        if( args == null )
        {
            return new ArrayList<>( 0 );
        }
        List<String> result = new ArrayList<>( args.length );
        for( Object arg : args )
        {
            if( arg == null )
            {
                result.add( null );
            }
            else
            {
                result.add( arg.toString() );
            }
        }
        return result;
    }
}
