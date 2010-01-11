/*
 * Copyright 2009 Niclas Hedhman.
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

package org.qi4j.logging;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Ignore;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.mixin.NoopMixin;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.logging.debug.Debug;
import org.qi4j.logging.debug.DebugConcern;
import org.qi4j.logging.debug.records.ServiceDebugRecordEntity;
import org.qi4j.logging.debug.service.DebugServiceConfiguration;
import org.qi4j.logging.debug.service.DebuggingServiceComposite;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;

public class DebuggingTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addServices( DebuggingServiceComposite.class );
        module.addServices( MemoryEntityStoreService.class );
        module.addServices( SomeService.class ).withMixins( Debug.class ).withConcerns( DebugConcern.class );
        module.addEntities( DebugServiceConfiguration.class );
        module.addEntities( ServiceDebugRecordEntity.class );
        module.addServices( UuidIdentityGeneratorService.class );
    }

    @Test
    @Ignore( "Needs QI-230 to be resolved." )
    public void whenCallingMethodThenExpectDebugEntityCreated()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            // There is no Query capability available for Libraries, since that sits in Extensions.
            // Obtaining the EntityStore directly is a very ugly hack to get around this problem, and only related
            // to the test sitting in qi4j-libraries source repository.

//            QueryBuilder<DebugRecord> builder = queryBuilderFactory.newQueryBuilder( DebugRecord.class );
//            Query<DebugRecord> query = builder.newQuery( uow );
//            assertEquals( 0, query.count() );
            Some service = (Some) serviceLocator.findService( Some.class ).get();
            String message = service.doSomething( "World!", 10 );
            assertEquals( message, "Hello!" );
            EntityStore es = (EntityStore) serviceLocator.findService( EntityStore.class ).get();
            final String[] result = new String[1];
            es.visitEntityStates( new EntityStore.EntityStateVisitor()
            {
                public void visitEntityState( EntityState entityState )
                {
                    if( ServiceDebugRecordEntity.class.getName()
                        .equals( entityState.entityDescriptor().entityType().type().name() ) )
                    {
                        result[ 0 ] = entityState.identity().identity();
                    }
                }
            }, moduleInstance );
            ServiceDebugRecordEntity debugEntry = uow.get( ServiceDebugRecordEntity.class, result[ 0 ] );
            String mess = debugEntry.message().get();
            System.out.println( mess );
            assertEquals( "some message.", mess );
            uow.complete();
        }
        catch( ConcurrentEntityModificationException e )
        {
            e.printStackTrace();
            uow.discard();
        }
        catch( UnitOfWorkCompletionException e )
        {
            e.printStackTrace();
            uow.discard();
        }
        finally
        {
            if( uow.isOpen() )
            {
                uow.discard();
            }
        }
    }

    public interface Some
    {
        String doSomething( String value1, Integer value2 );
    }

    @Mixins( { SomeMixin.class } )
    public interface SomeService
        extends Some, ServiceComposite
    {
    }

    public static class SomeMixin
        implements Some
    {
        @This
        private Debug debug;

        public String doSomething( String value1, Integer value2 )
        {
            System.out.println( "DebugLevel: " + debug.debugLevel() );
            debug.debug( 99, "some lower priority message not being stored." );
            debug.debug( 100, "some message." );
            return "Hello!";
        }
    }
}
