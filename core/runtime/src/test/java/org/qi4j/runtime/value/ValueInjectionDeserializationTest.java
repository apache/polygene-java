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

package org.qi4j.runtime.value;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

public class ValueInjectionDeserializationTest
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( Niclas.class );
        module.values( SomeValue.class );
        module.services( DummyService.class );
        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void testThatServiceAndStructureInjectionWorkForValueWhenEntityRetrievedFromStore()
        throws Exception
    {
        UnitOfWork uow = null;
        try
        {
            ValueBuilder<Some> builder = module.newValueBuilder( Some.class );
            builder.prototype().data().set( "Niclas" );
            Some value = builder.newInstance();

            uow = module.newUnitOfWork();
            EntityBuilder<Niclas> eb = uow.newEntityBuilder( Niclas.class );
            eb.instance().value().set( value );
            Niclas niclas1 = eb.newInstance();
            String id = niclas1.identity().get();
            uow.complete();

            uow = module.newUnitOfWork();
            Niclas niclas2 = uow.get( Niclas.class, id );
            Some someValue = niclas2.value().get();
            Assert.assertEquals( someValue.data().get(), "Niclas" );
            Assert.assertNotNull( someValue.module() );
            Assert.assertNotNull( someValue.service() );
        }
        finally
        {
            if( uow != null )
            {
                uow.discard();
            }
        }
    }

    public interface Niclas
        extends EntityComposite
    {
        Property<Some> value();
    }

    public interface Some
    {
        DummyService service();

        Module module();

        Property<String> data();
    }

    @Mixins( SomeMixin.class )
    public interface SomeValue
        extends Some, ValueComposite
    {
    }

    public static abstract class SomeMixin
        implements Some
    {
        @Service
        DummyService service;
        @Structure
        Module module;

        public DummyService service()
        {
            return service;
        }

        public Module module()
        {
            return module;
        }
    }

    public interface DummyService
        extends ServiceComposite
    {
    }
}
