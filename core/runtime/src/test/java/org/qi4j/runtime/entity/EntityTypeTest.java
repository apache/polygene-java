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

package org.qi4j.runtime.entity;

import org.junit.Test;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

public class EntityTypeTest
    extends AbstractQi4jTest
{
    @Test
    public void givenSubclassedEntityWhenRequestingSuperclassExpectResolutionToWork()
        throws Exception
    {
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            EntityBuilder<Rst> builder3 = uow.newEntityBuilder( Rst.class, "123" );
            EntityBuilder<Def> builder2 = uow.newEntityBuilder( Def.class, "456" );
            EntityBuilder<Abc> builder1 = uow.newEntityBuilder( Abc.class, "789" );
        }
        finally
        {
            uow.discard();
        }
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( Rst.class );
        new EntityTestAssembler().assemble( module );
    }

    public interface Abc
        extends EntityComposite
    {
    }

    public interface Def
        extends Abc
    {
    }

    public interface Rst
        extends Def, EntityComposite
    {
    }
}
