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

package org.qi4j.tracing.internal;

import java.lang.reflect.Method;
import org.qi4j.composite.Composite;
import org.qi4j.entity.ConcurrentEntityModificationException;
import org.qi4j.entity.EntityBuilder;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.This;
import org.qi4j.tracing.internal.CompositeTraceRecord;
import org.qi4j.tracing.internal.EntityTraceRecord;
import org.qi4j.tracing.internal.ServiceTraceRecord;
import org.qi4j.tracing.internal.TraceRecord;
import org.qi4j.service.ServiceComposite;

public class TraceServiceMixin
    implements TraceService
{
    @Structure private UnitOfWorkFactory unitOfWorkFactory;
    private int traceLevel;
    private int counter = 0;
    @This TraceServiceConfiguration configuration;

    public int traceLevel()
    {
        if( counter % 100 == 0 )
        {
            counter = 0;
            traceLevel = configuration.traceLevel().get();
        }
        return traceLevel;
    }

    public void traceSuccess( Class compositeType, Composite object, Method method, Object[] args, Object result, long entryTime, long durationNano )
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            System.out.println( "traceSuccess()" );
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

    public void traceException( Class compositeType, Composite object, Method method, Object[] args, Throwable t, long entryTime, long durationNano )
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
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

    private void createTraceRecord( UnitOfWork uow, Class compositeType, Composite object, Method method, Object[] args, long entryTime, long durationNano, Throwable exception )
    {
        if( object instanceof EntityComposite )
        {
            EntityComposite entity = (EntityComposite) object;
            String identity = entity.identity().get();
            EntityComposite source = (EntityComposite) uow.getReference( identity, entity.type() );
            EntityBuilder<EntityTraceRecord> builder = uow.newEntityBuilder( EntityTraceRecord.class );
            EntityTraceRecord state = builder.stateOfComposite();
            setStandardStuff( compositeType, method, args, entryTime, durationNano, state, exception );
            state.source().set( source );
            EntityTraceRecord etr = builder.newInstance();  // Record is created.
        }
        else if( object instanceof ServiceComposite )
        {
            ServiceComposite service = (ServiceComposite) object;
            EntityBuilder<ServiceTraceRecord> builder = uow.newEntityBuilder( ServiceTraceRecord.class );
            ServiceTraceRecord state = builder.stateOfComposite();
            setStandardStuff( compositeType, method, args, entryTime, durationNano, state, exception );
            state.source().set( service );
            ServiceTraceRecord str = builder.newInstance();  // Record is created.
        }
        else
        {
            EntityBuilder<CompositeTraceRecord> builder = uow.newEntityBuilder( CompositeTraceRecord.class );
            CompositeTraceRecord state = builder.stateOfComposite();
            state.source().set( object );
            setStandardStuff( compositeType, method, args, entryTime, durationNano, state, exception );
            CompositeTraceRecord str = builder.newInstance();  // Record is created.
        }
    }

    private void setStandardStuff( Class compositeType, Method method, Object[] args, long entryTime, long durationNano, TraceRecord state, Throwable exception )
    {
        state.duration().set( durationNano );
        state.entryTime().set( entryTime );
        state.methodName().set( method.getName() );
        state.compositeTypeName().set( compositeType.getName() );
        state.arguments().set( convertArguments( args ) );
        state.threadName().set( Thread.currentThread().getName() );
        state.exception().set( exception );
    }

    private String[] convertArguments( Object[] args )
    {
        String[] result = new String[args.length];
        for( int i = 0; i < result.length; i++ )
        {
            Object arg = args[ i ];
            if( arg == null )
            {
                result[ i ] = null;
            }
            else
            {
                result[ i ] = arg.toString();
            }
        }
        return result;
    }
}
