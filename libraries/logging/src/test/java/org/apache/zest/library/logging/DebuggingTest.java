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

package org.apache.zest.library.logging;

import java.util.function.Function;
import org.junit.Test;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.unitofwork.ConcurrentEntityModificationException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.io.Outputs;
import org.apache.zest.io.Transforms;
import org.apache.zest.library.logging.debug.Debug;
import org.apache.zest.library.logging.debug.DebugConcern;
import org.apache.zest.library.logging.debug.records.ServiceDebugRecordEntity;
import org.apache.zest.library.logging.debug.service.DebugServiceConfiguration;
import org.apache.zest.library.logging.debug.service.DebuggingServiceComposite;
import org.apache.zest.spi.entity.EntityState;
import org.apache.zest.spi.entitystore.EntityStore;
import org.apache.zest.spi.uuid.UuidIdentityGeneratorService;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

import static org.junit.Assert.assertEquals;

public class DebuggingTest
    extends AbstractZestTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( DebuggingServiceComposite.class );
        new EntityTestAssembler().assemble( module );
        module.services( SomeService.class ).withMixins( Debug.class ).withConcerns( DebugConcern.class );
        module.entities( DebugServiceConfiguration.class );
        module.entities( ServiceDebugRecordEntity.class );
        module.services( UuidIdentityGeneratorService.class );
    }

    @Test
    public void whenCallingMethodThenExpectDebugEntityCreated()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            // There is no Query capability available for Libraries, since that sits in Extensions.
            // Obtaining the EntityStore directly is a very ugly hack to get around this problem, and only related
            // to the test sitting in libraries/ source repository.

//            QueryBuilder<DebugRecord> builder = module.newQueryBuilder( DebugRecord.class );
//            Query<DebugRecord> query = builder.newQuery( uow );
//            assertEquals( 0, query.count() );
            Some service = (Some) serviceFinder.findService( Some.class ).get();
            String message = service.doSomething( "World!", 10 );
            assertEquals( message, "Hello!" );
            EntityStore es = (EntityStore) serviceFinder.findService( EntityStore.class ).get();
            final String[] result = new String[1];
            es.entityStates( module ).transferTo( Transforms.map( new Function<EntityState, EntityState>()
                    {
                        public EntityState apply( EntityState entityState )
                        {
                            if( ServiceDebugRecordEntity.class.getName()
                                    .equals( entityState.entityDescriptor().types().findFirst().get().getName() ) )
                            {
                                result[0] = entityState.identity().identity();
                            }

                            return entityState;
                        }
                    }, Outputs.<EntityState>noop() ));

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
