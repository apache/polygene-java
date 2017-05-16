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

package org.apache.polygene.library.logging;

import java.util.stream.Stream;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.unitofwork.ConcurrentEntityModificationException;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.logging.debug.Debug;
import org.apache.polygene.library.logging.debug.DebugConcern;
import org.apache.polygene.library.logging.debug.records.ServiceDebugRecordEntity;
import org.apache.polygene.library.logging.debug.service.DebugServiceConfiguration;
import org.apache.polygene.library.logging.debug.service.DebuggingServiceComposite;
import org.apache.polygene.spi.entity.EntityState;
import org.apache.polygene.spi.entitystore.EntityStore;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DebuggingTest
    extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( DebuggingServiceComposite.class );
        new EntityTestAssembler().assemble( module );
        module.services( SomeService.class ).withMixins( Debug.class ).withConcerns( DebugConcern.class );
        module.entities( DebugServiceConfiguration.class );
        module.entities( ServiceDebugRecordEntity.class );
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
            Some service = serviceFinder.findService( Some.class ).get();
            String message = service.doSomething( "World!", 10 );
            assertEquals( message, "Hello!" );
            EntityStore es = serviceFinder.findService( EntityStore.class ).get();
            final Identity[] result = new Identity[1];
            try( Stream<EntityState> entityStates = es.entityStates( module ) )
            {
                entityStates
                    .forEach( entityState ->
                              {
                                  if( ServiceDebugRecordEntity.class.getName().equals(
                                      entityState.entityDescriptor().types().findFirst().get().getName() ) )
                                  {
                                      result[ 0 ] = entityState.entityReference().identity();
                                  }
                              } );
            }

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
